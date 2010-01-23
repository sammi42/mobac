package mobac.program.atlascreators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import mobac.program.atlascreators.impl.MapTileBuilder;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.CacheTileProvider;
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
	// protected String mapName;
	protected String cleanedMapName;

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
		cleanedMapName = map.getName();
		cleanedMapName = cleanedMapName.replaceAll("[[^\\p{Alnum}-_]]+", "_");
		cleanedMapName = cleanedMapName.replaceAll("_{2,}", "_");
		if (cleanedMapName.endsWith("_"))
			cleanedMapName = cleanedMapName.substring(0, cleanedMapName.length() - 1);
		if (cleanedMapName.startsWith("_"))
			cleanedMapName = cleanedMapName.substring(1, cleanedMapName.length());
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		File kmzFile = null;
		try {
			Utilities.mkDir(mapDir);
			kmzFile = new File(mapDir, cleanedMapName + ".kmz");
			kmzOutputStream = new ZipOutputStream(new FileOutputStream(kmzFile));
			kmzOutputStream.setMethod(Deflater.NO_COMPRESSION);
			if (parameters == null) {
				String fileName = "files/" + cleanedMapName + ".jpg";
				createImage(fileName);
				buildKmzFile(fileName);
			} else {
				initKmlDoc();
				createTiledImage();
				writeKmlToZip();
			}
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

	protected void createTiledImage() throws InterruptedException, MapCreationException {
		mapDlTileProvider = new CacheTileProvider(mapDlTileProvider);

		GarminJpegTileImageDataWriter writer = new GarminJpegTileImageDataWriter(
				(TileImageJpegDataWriter) parameters.getFormat().getDataWriter());
		MapTileBuilder mapTileBuilder = new MapTileBuilder(this, writer, writer, true);
		int customTileCount = mapTileBuilder.getCustomTileCount();
		if (customTileCount > 100)
			throw new MapCreationException("Too many tiles: " + customTileCount
					+ "\nMaximum tile count is 100.");
		atlasProgress.initMapCreation(customTileCount);
		mapTileBuilder.createTiles();
	}

	protected void createImage(String imageFileName) throws InterruptedException,
			MapCreationException {

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
			if (len > 1024) {
				graphics.setTransform(AffineTransform.getScaleInstance(scaleFactor, scaleFactor));
				graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			}
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
			writer = new TileImageJpegDataWriter(0.9);
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

	private void buildKmzFile(String imageFileName) throws ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException, IOException {
		int startX = xMin * tileSize;
		int endX = (xMax + 1) * tileSize;
		int startY = yMin * tileSize;
		int endY = (yMax + 1) * tileSize;
		initKmlDoc();
		addKmlEntry(map.getName(), imageFileName, startX, startY, endX - startX, endY - startY);
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

	private void addKmlEntry(String imageName, String imageFileName, int startX, int startY,
			int width, int height) {
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

		name.setTextContent(imageName);
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

	private class GarminJpegTileImageDataWriter extends TileImageJpegDataWriter implements
			MapTileWriter {

		int width;
		int height;

		public GarminJpegTileImageDataWriter(TileImageJpegDataWriter jpegWriter) {
			super(jpegWriter);
		}

		@Override
		public void processImage(BufferedImage image, OutputStream out) throws IOException {
			width = image.getWidth();
			height = image.getHeight();
			int max = Math.max(width, height);
			if (width > 1024 || height > 1024) {
				double factor = 1024d / max;
				int scaledWidth = (int) (factor * width);
				int scaledHeight = (int) (factor * height);
				Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight,
						BufferedImage.SCALE_SMOOTH);
				image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
				image.getGraphics().drawImage(scaledImage, 0, 0, null);
			}
			super.processImage(image, out);
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData)
				throws IOException {
			if (tileData.length > MAX_FILE_SIZE)
				throw new IOException("Image format exceeds 3 MiB - this is not allowd by "
						+ "specification please reduce the JPEG quality");

			String filename = String.format("files/tile%dx%d.jpg", tilex, tiley);
			String name = String.format("%s %dx%d", map.getName(), tilex, tiley);
			writeStoredEntry(filename, tileData);

			int xStart = (xMin * tileSize) + (tilex * parameters.getWidth());
			int yStart = (yMin * tileSize) + (tiley * parameters.getHeight());

			addKmlEntry(name, filename, xStart, yStart, width, height);
		}

		public void finalizeMap() {
		}
	}

}
