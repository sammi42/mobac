package tac.program.atlascreators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tac.exceptions.MapCreationException;
import tac.mapsources.mapspace.MercatorPower2MapSpace;
import tac.program.interfaces.MapInterface;
import tac.program.tiledatawriter.TileImageJpegDataWriter;
import tac.utilities.Utilities;
import tac.utilities.stream.ArrayOutputStream;
import tac.utilities.tar.TarIndex;

public class GarminCustom extends AtlasCreator {

	/**
	 * Each jpeg should be less than 3MB.
	 * https://forums.garmin.com/showthread.php?t=2646
	 */
	private static final int MAX_FILE_SIZE = 3 * 1024 * 1024;

	protected File mapDir;
	protected String mapName;
	protected String imageFileName;

	protected ZipOutputStream kmzOutputStream = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return (mapSource.getMapSpace() instanceof MercatorPower2MapSpace);
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		mapDir = new File(atlasDir, map.getLayer().getName());
		mapName = map.getName();
		imageFileName = "files/" + mapName + ".jpg";
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		File kmzFile = null;
		try {
			Utilities.mkDir(mapDir);
			kmzFile = new File(mapDir, mapName + ".kmz");
			kmzOutputStream = new ZipOutputStream(new FileOutputStream(kmzFile));
			kmzOutputStream.setLevel(Deflater.NO_COMPRESSION);
			createTiles();
			buildKmzFile();
			kmzOutputStream.close();
			kmzFile = null;
		} catch (InterruptedException e) {
			throw e;
		} catch (MapCreationException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(e);
		} finally {
			Utilities.closeStream(kmzOutputStream);
			if (kmzFile != null)
				// There was an error - delete the file
				kmzFile.delete();
		}
	}

	@Override
	protected void createTiles() throws InterruptedException, MapCreationException {

		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		int mapWidth = (xMax - xMin + 1) * tileSize;
		int mapHeight = (yMax - yMin + 1) * tileSize;

		int imageWidth = Math.min(1024, mapWidth);
		int imageHeight = Math.min(1024, mapHeight);

		int len = Math.max(mapWidth, mapHeight);
		double scaleFactor = 1.0;
		if (len > 1024) {
			scaleFactor = 1024d / len;
			if (imageWidth != imageHeight) {
				// Map is not rectangle -> adapt height or width
				if (imageWidth > imageHeight)
					imageHeight = (int) (scaleFactor * mapHeight);
				else
					imageWidth = (int) (scaleFactor * mapWidth);
			}
		}
		BufferedImage tileImage = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = tileImage.createGraphics();
		try {
			if (len > 1024)
				graphics.setTransform(AffineTransform.getScaleInstance(scaleFactor, scaleFactor));
			int lineY = 0;
			for (int y = yMin; y <= yMax; y++) {
				int lineX = 0;
				for (int x = xMin; x <= xMax; x++) {
					checkUserAbort();
					atlasProgress.incMapCreationProgress();
					try {
						byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
						if (sourceTileData != null) {
							BufferedImage tile = ImageIO.read(new ByteArrayInputStream(
									sourceTileData));
							graphics.drawImage(tile, lineX, lineY, Color.WHITE, null);
						}
					} catch (IOException e) {
						log.error("", e);
					}
					lineX += tileSize;
				}
				lineY += tileSize;
			}
		} finally {
			graphics.dispose();
		}
		try {
			TileImageJpegDataWriter writer = new TileImageJpegDataWriter(1.0);
			writer.initialize();
			// The maximum file size for the jpg image is 3 MB
			// This OutputStream will fail if the resulting image is larger than
			// 3 MB - then we retry using a higher JPEG compression level
			ArrayOutputStream buf = new ArrayOutputStream(MAX_FILE_SIZE);
			byte[] data = null;
			for (int c = 99; c > 50; c -= 5) {
				buf.reset();
				try {
					writer.processImage(tileImage, buf);
					data = buf.toByteArray();
					break;
				} catch (IOException e) {
					log.trace("Image size too large, increasing compression to 0." + c);
				}
				writer.setJpegCompressionLevel(c / 100f);
			}
			if (data == null)
				throw new MapCreationException("Unable to create an image with less than 3 MB!");
			kmzOutputStream.putNextEntry(new ZipEntry(imageFileName));
			kmzOutputStream.write(data);
			kmzOutputStream.closeEntry();
		} catch (IOException e) {
			throw new MapCreationException(e);
		}
	}

	private void buildKmzFile() throws ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException, IOException {
		MapSpace mapSpace = mapSource.getMapSpace();

		NumberFormat df = Utilities.FORMAT_6_DEC_ENG;

		String longitudeMin = df.format(mapSpace.cXToLon(xMin * tileSize, zoom));
		String longitudeMax = df.format(mapSpace.cXToLon((xMax + 1) * tileSize, zoom));
		String latitudeMin = df.format(mapSpace.cYToLat((yMax + 1) * tileSize, zoom));
		String latitudeMax = df.format(mapSpace.cYToLat(yMin * tileSize, zoom));

		DocumentBuilder builder;
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();

		Element kml = doc.createElementNS("http://www.opengis.net/kml/2.2", "kml");
		Element go = doc.createElement("GroundOverlay");
		Element ico = doc.createElement("Icon");
		Element href = doc.createElement("href");
		Element drawOrder = doc.createElement("DrawOrder");
		Element latLonBox = doc.createElement("LatLonBox");
		Element north = doc.createElement("north");
		Element south = doc.createElement("south");
		Element east = doc.createElement("east");
		Element west = doc.createElement("west");
		Element rotation = doc.createElement("rotation");

		href.setTextContent(imageFileName);
		drawOrder.setTextContent("0");

		north.setTextContent(latitudeMax);
		south.setTextContent(latitudeMin);
		west.setTextContent(longitudeMin);
		east.setTextContent(longitudeMax);
		rotation.setTextContent("0.0");

		doc.appendChild(kml);
		kml.appendChild(go);
		go.appendChild(ico);
		go.appendChild(latLonBox);
		ico.appendChild(href);
		ico.appendChild(drawOrder);

		latLonBox.appendChild(north);
		latLonBox.appendChild(south);
		latLonBox.appendChild(east);
		latLonBox.appendChild(west);
		latLonBox.appendChild(rotation);

		Transformer serializer;
		serializer = TransformerFactory.newInstance().newTransformer();
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		kmzOutputStream.putNextEntry(new ZipEntry("doc.kml"));
		serializer.transform(new DOMSource(doc), new StreamResult(kmzOutputStream));
		kmzOutputStream.closeEntry();
	}

}
