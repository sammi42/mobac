package mobac.program.atlascreators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.zip.CRC32;
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

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.TileImageFormat;
import mobac.program.tiledatawriter.TileImageJpegDataWriter;
import mobac.utilities.Utilities;
import mobac.utilities.stream.ArrayOutputStream;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


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
	private CRC32 crc = new CRC32();

	private Document kmlDoc = null;
	private Element groundOverlayRoot = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return (mapSource.getMapSpace() instanceof MercatorPower2MapSpace);
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas) throws IOException, InterruptedException,
			AtlasTestException {
		super.startAtlasCreation(atlas);
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				if (map.getParameters() == null)
					continue;
				TileImageFormat format = map.getParameters().getFormat();
				if (!(format.getDataWriter() instanceof TileImageJpegDataWriter))
					throw new AtlasTestException(
							"Only JPEG tile format is supported by this atlas format!", map);
			}
		}
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		mapDir = new File(atlasDir, map.getLayer().getName());
		mapName = map.getName();
		mapName = mapName.replaceAll("[[^\\p{Alnum}-_]]+", "_");
		mapName = mapName.replaceAll("_{2,}", "_");
		if (mapName.endsWith("_"))
			mapName = mapName.substring(0, mapName.length() - 1);
		if (mapName.startsWith("_"))
			mapName = mapName.substring(1, mapName.length());
		imageFileName = "files/" + mapName + ".jpg";
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		File kmzFile = null;
		try {
			Utilities.mkDir(mapDir);
			kmzFile = new File(mapDir, mapName + ".kmz");
			kmzOutputStream = new ZipOutputStream(new FileOutputStream(kmzFile));
			kmzOutputStream.setMethod(Deflater.NO_COMPRESSION);
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
			TileImageJpegDataWriter writer;
			if (parameters == null) {
				writer = new TileImageJpegDataWriter(1.0);
			} else {
				TileImageFormat format = parameters.getFormat();
				writer = new TileImageJpegDataWriter((TileImageJpegDataWriter) format
						.getDataWriter());
			}
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
			writeStoredEntry(imageFileName, data);
		} catch (IOException e) {
			throw new MapCreationException(e);
		}
	}

	private void writeStoredEntry(String name, byte[] data) throws IOException {
		ZipEntry ze = new ZipEntry(name);
		ze.setMethod(ZipEntry.STORED);
		ze.setCompressedSize(data.length);
		ze.setSize(data.length);
		crc.reset();
		crc.update(data);
		ze.setCrc(crc.getValue());
		kmzOutputStream.putNextEntry(ze);
		kmzOutputStream.write(data);
		kmzOutputStream.closeEntry();
	}

	private void buildKmzFile() throws ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException, IOException {
		int startX = xMin * tileSize;
		int endX = (xMax + 1) * tileSize;
		int startY = yMin * tileSize;
		int endY = (yMax + 1) * tileSize;
		initKmlDoc();
		addKmlEntry(startX, startY, endX - startX, endY - startY);
		writeKmlToZip();
	}

	private void initKmlDoc() throws ParserConfigurationException {

		DocumentBuilder builder;
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		kmlDoc = builder.newDocument();

		boolean folder = true;
		Element kml = kmlDoc.createElementNS("http://www.opengis.net/kml/2.2", "kml");
		kmlDoc.appendChild(kml);
		groundOverlayRoot = kml;
		if (folder) {
			groundOverlayRoot = kmlDoc.createElement("Folder");
			kml.appendChild(groundOverlayRoot);
			Element name = kmlDoc.createElement("name");
			name.setTextContent(map.getLayer().getName());
			Element open = kmlDoc.createElement("open");
			open.setTextContent("1");
			groundOverlayRoot.appendChild(name);
			groundOverlayRoot.appendChild(open);
		}

	}

	private void addKmlEntry(int startX, int startY, int width, int height) {
		Element go = kmlDoc.createElement("GroundOverlay");
		Element name = kmlDoc.createElement("name");
		Element ico = kmlDoc.createElement("Icon");
		Element href = kmlDoc.createElement("href");
		Element drawOrder = kmlDoc.createElement("drawOrder");
		Element latLonBox = kmlDoc.createElement("LatLonBox");
		Element north = kmlDoc.createElement("north");
		Element south = kmlDoc.createElement("south");
		Element east = kmlDoc.createElement("east");
		Element west = kmlDoc.createElement("west");
		Element rotation = kmlDoc.createElement("rotation");

		name.setTextContent(map.getName());
		href.setTextContent(imageFileName);
		drawOrder.setTextContent("0");

		MapSpace mapSpace = mapSource.getMapSpace();
		NumberFormat df = Utilities.FORMAT_6_DEC_ENG;

		String longitudeMin = df.format(mapSpace.cXToLon(startX, zoom));
		String longitudeMax = df.format(mapSpace.cXToLon(startX + width, zoom));
		String latitudeMin = df.format(mapSpace.cYToLat(startY + height, zoom));
		String latitudeMax = df.format(mapSpace.cYToLat(startY, zoom));

		north.setTextContent(latitudeMax);
		south.setTextContent(latitudeMin);
		west.setTextContent(longitudeMin);
		east.setTextContent(longitudeMax);
		rotation.setTextContent("0.0");

		groundOverlayRoot.appendChild(go);
		go.appendChild(name);
		go.appendChild(ico);
		go.appendChild(latLonBox);
		ico.appendChild(href);
		ico.appendChild(drawOrder);

		latLonBox.appendChild(north);
		latLonBox.appendChild(south);
		latLonBox.appendChild(east);
		latLonBox.appendChild(west);
		latLonBox.appendChild(rotation);
	}

	private void writeKmlToZip() throws TransformerFactoryConfigurationError, TransformerException,
			IOException {
		Transformer serializer;
		serializer = TransformerFactory.newInstance().newTransformer();
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		ByteArrayOutputStream bos = new ByteArrayOutputStream(16000);
		serializer.transform(new DOMSource(kmlDoc), new StreamResult(bos));
		writeStoredEntry("doc.kml", bos.toByteArray());
		kmlDoc = null;
		groundOverlayRoot = null;
	}
}
