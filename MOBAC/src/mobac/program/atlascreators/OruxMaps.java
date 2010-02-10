package mobac.program.atlascreators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.imageio.ImageIO;

import mobac.exceptions.MapCreationException;
import mobac.gui.AtlasProgress;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.AtlasThread;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageFormat;
import mobac.program.model.TileImageParameters;
import mobac.utilities.MyMath;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

/**
 * Creates maps using the OruxMaps (Android) atlas format. Most of the code is
 * taken from TrekBuddy.java and TrekBuddyCustom.java
 * 
 * @author orux
 */
public class OruxMaps extends AtlasCreator {

	// OruxMaps tile size
	protected static final int TILE_SIZE = 512;

	// OruxMaps background color
	protected static final Color BG_COLOR = new Color(0xcb, 0xd3, 0xf3);

	protected File layerFolder;
	protected File mapFolder;
	protected File setFolder;
	private int realWidth;
	private int realHeight;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return (mapSource.getMapSpace() instanceof MercatorPower2MapSpace);
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		LayerInterface layer = map.getLayer();
		layerFolder = new File(atlasDir, layer.getName());
		mapFolder = new File(layerFolder, map.getName());
		setFolder = new File(mapFolder, "set");
		// OruxMaps default image format, jpeg90; always TILE_SIZE=512;
		if (parameters == null)
			parameters = new TileImageParameters(TILE_SIZE, TILE_SIZE, TileImageFormat.JPEG90);
		else
			parameters = new TileImageParameters(TILE_SIZE, TILE_SIZE, parameters.getFormat());
	}

	public void createMap() throws MapCreationException {

		try {
			Utilities.mkDirs(setFolder);

			// look for main otrk2 calibration file; write if not exists
			String otrk2FileName = map.getName().substring(0, map.getName().length() - 3)
					+ ".otrk2.xml";
			File otrk2 = new File(layerFolder, otrk2FileName);
			if (!otrk2.exists())
				writeMainOtrk2File(otrk2);

			writeOtrk2File();
			createTiles();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		} catch (Exception e) {
			throw new MapCreationException(e);
		}

	}

	// main calibration file
	private void writeMainOtrk2File(File otrk2) {
		log.trace("Writing main otrk2 file");
		OutputStreamWriter writer;
		FileOutputStream otrk2FileStream = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(otrk2), "UTF8");
			writer.write(prepareMainMapString(map.getName()
					.substring(0, map.getName().length() - 3)));
			writer.flush();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(otrk2FileStream);
		}
	}

	protected void writeOtrk2File() {
		File otrk2File = new File(mapFolder, map.getName() + ".otrk2.xml");
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(otrk2File);
			OutputStreamWriter mapWriter = new OutputStreamWriter(stream, "UTF8");
			MapSpace mapSpace = mapSource.getMapSpace();
			double longitudeMin = mapSpace.cXToLon(xMin * tileSize, zoom);
			double longitudeMax = mapSpace.cXToLon((xMax + 1) * tileSize, zoom);
			double latitudeMin = mapSpace.cYToLat((yMax + 1) * tileSize, zoom);
			double latitudeMax = mapSpace.cYToLat(yMin * tileSize, zoom);
			int width = (xMax - xMin + 1) * tileSize;
			int height = (yMax - yMin + 1) * tileSize;
			mapWriter.write(prepareOtrk2String(map.getName(), longitudeMin, longitudeMax,
					latitudeMin, latitudeMax, width, height));
			mapWriter.flush();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(stream);
		}
	}

	/**
	 * New experimental custom tile size algorithm implementation.
	 * 
	 * It creates each custom sized tile separately. Therefore each original
	 * tile (256x256) will be loaded and painted multiple times. Therefore this
	 * implementation needs much more CPU power as each original tile is loaded
	 * at least once and each generated tile has to be saved.
	 */

	protected void createTiles() throws InterruptedException {
		// log.debug("Starting map creation using custom parameters: " + param);
		Thread t = Thread.currentThread();

		// left upper point on the map in pixels
		// regarding the current zoom level
		int xStart = xMin * tileSize;
		int yStart = yMin * tileSize;

		// lower right point on the map in pixels
		// regarding the current zoom level
		int xEnd = xMax * tileSize + (tileSize - 1);
		int yEnd = yMax * tileSize + (tileSize - 1);

		int mergedWidth = xEnd - xStart;
		int mergedHeight = yEnd - yStart;

		// Reduce tile size of overall map height/width is smaller that one tile
		realWidth = parameters.getWidth();
		realHeight = parameters.getHeight();

		AtlasProgress ap = null;
		if (t instanceof AtlasThread) {
			ap = ((AtlasThread) t).getAtlasProgress();
			int customTileCount = MyMath.divCeil(mergedWidth, realWidth)
					* MyMath.divCeil(mergedHeight, realHeight);
			ap.initMapCreation(customTileCount);
		}

		// Absolute positions
		int xAbsPos = xStart;
		int yAbsPos = yStart;

		// log.trace("tile size: " + realWidth + " * " + realHeight);
		// log.trace("X: from " + xStart + " to " + xEnd);
		// log.trace("Y: from " + yStart + " to " + yEnd);

		// We don't work with large images, therefore we can disable the (file)
		// cache of ImageIO. This will speed up the creation process a bit
		ImageIO.setUseCache(false);
		ByteArrayOutputStream buf = new ByteArrayOutputStream(32768);
		TileImageDataWriter tileImageDataWriter = parameters.getFormat().getDataWriter();
		tileImageDataWriter.initialize();
		try {

			int yRelPos = 0;
			while (yAbsPos < yEnd) {
				int xRelPos = 0;
				xAbsPos = xStart;
				while (xAbsPos < xEnd) {
					if (t.isInterrupted())
						throw new InterruptedException();
					if (ap != null)
						ap.incMapCreationProgress();
					BufferedImage tileImage = new BufferedImage(realWidth, realHeight,
							BufferedImage.TYPE_3BYTE_BGR);

					buf.reset();
					try {
						Graphics2D graphics = tileImage.createGraphics();
						String tileFileName = map.getName() + "_" + xRelPos / TILE_SIZE + "_"
								+ yRelPos / TILE_SIZE + ".omc2";
						graphics.setColor(BG_COLOR);
						graphics.fillRect(0, 0, tileImage.getWidth(), tileImage.getHeight());
						// log.trace("Creating tile " + tileFileName);
						paintCustomTile(graphics, xAbsPos, yAbsPos);
						graphics.dispose();
						tileImageDataWriter.processImage(tileImage, buf);
						writeTile(tileFileName, buf.toByteArray());
					} catch (Exception e) {
						log.error("Error writing tile image: ", e);
					}

					xRelPos += realWidth;
					xAbsPos += realWidth;
				}
				yRelPos += realHeight;
				yAbsPos += realHeight;
			}
		} finally {
			tileImageDataWriter.dispose();
		}
	}

	/**
	 * Paints the graphics of the custom tile specified by the pixel coordinates
	 * <code>xAbsPos</code> and <code>yAbsPos</code> on the currently selected
	 * map & layer.
	 * 
	 * @param graphics
	 * @param xAbsPos
	 * @param yAbsPos
	 */
	private void paintCustomTile(Graphics2D graphics, int xAbsPos, int yAbsPos) {
		int xTile = xAbsPos / tileSize;
		int xTileOffset = -(xAbsPos % tileSize);

		for (int x = xTileOffset; x < realWidth; x += tileSize) {
			int yTile = yAbsPos / tileSize;
			int yTileOffset = -(yAbsPos % tileSize);
			for (int y = yTileOffset; y < realHeight; y += tileSize) {
				try {
					BufferedImage orgTileImage = loadOriginalMapTile(xTile, yTile);
					if (orgTileImage != null)
						graphics.drawImage(orgTileImage, xTileOffset, yTileOffset, orgTileImage
								.getWidth(), orgTileImage.getHeight(), null);
				} catch (Exception e) {
					log.error("Error while painting sub-tile", e);
				}
				yTile++;
				yTileOffset += tileSize;
			}
			xTile++;
			xTileOffset += tileSize;
		}
	}

	/**
	 * A simple local cache holding the last 10 loaded original tiles. If the
	 * custom tile size is smaller than 256x256 the efficiency of this cache is
	 * very high (~ 75% hit rate).
	 */
	private CachedTile[] cache = new CachedTile[10];
	private int cachePos = 0;

	private BufferedImage loadOriginalMapTile(int xTile, int yTile) throws Exception {
		byte[] sourceTileData = mapDlTileProvider.getTileData(xTile, yTile);
		if (sourceTileData == null)
			return null;
		for (CachedTile ct : cache) {
			if (ct == null)
				continue;
			if (ct.xTile == xTile && ct.yTile == yTile) {
				// log.trace("cache hit");
				return ct.image;
			}
		}
		// log.trace("cache miss");
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(sourceTileData));
		cache[cachePos] = new CachedTile(image, xTile, yTile);
		cachePos = (cachePos + 1) % cache.length;
		return image;
	}

	private static class CachedTile {
		BufferedImage image;
		int xTile;
		int yTile;

		public CachedTile(BufferedImage image, int tile, int tile2) {
			super();
			this.image = image;
			xTile = tile;
			yTile = tile2;
		}
	}

	public void writeTile(String tileFileName, byte[] tileData) throws IOException {
		File f = new File(setFolder, tileFileName);
		FileOutputStream out = new FileOutputStream(f);
		try {
			out.write(tileData);
		} finally {
			Utilities.closeStream(out);
		}
	}

	protected String prepareOtrk2String(String fileName, double longitudeMin, double longitudeMax,
			double latitudeMin, double latitudeMax, int width, int height) {

		StringBuffer sbMap = new StringBuffer();

		sbMap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sbMap.append("<OruxTracker xmlns:orux=\"http://oruxtracker.com/app/res/calibration\"\n"
				+ " versionCode=\"2.1\">\n");
		sbMap.append("<MapCalibration layers=\"false\" layerLevel=\"" + map.getZoom() + "\">\n");
		sbMap.append("<MapName><![CDATA[" + fileName + "]]></MapName>\n");

		// convert ampersands and others
		String mapFileName = fileName.replaceAll("&", "&amp;");
		mapFileName = mapFileName.replaceAll("<", "&lt;");
		mapFileName = mapFileName.replaceAll(">", "&gt;");
		mapFileName = mapFileName.replaceAll("\"", "&quot;");
		mapFileName = mapFileName.replaceAll("'", "&apos;");

		int mapWidth = (xMax - xMin + 1) * tileSize;
		int mapHeight = (yMax - yMin + 1) * tileSize;
		int numXimg = (mapWidth + TILE_SIZE - 1) / TILE_SIZE;
		int numYimg = (mapHeight + TILE_SIZE - 1) / TILE_SIZE;
		sbMap.append("<MapChunks xMax=\"" + numXimg + "\" yMax=\"" + numYimg + "\" datum=\""
				+ "WGS84" + "\" projection=\"" + "Mercator" + "\" img_height=\"" + TILE_SIZE
				+ "\" img_width=\"" + TILE_SIZE + "\" file_name=\"" + mapFileName + "\" />\n");
		sbMap.append("<MapDimensions height=\"" + mapHeight + "\" width=\"" + mapWidth + "\" />\n");
		sbMap.append("<MapBounds minLat=\"" + latitudeMin + "\" maxLat=\"" + latitudeMax
				+ "\" minLon=\"" + longitudeMin + "\" maxLon=\"" + longitudeMax + "\" />\n");
		sbMap.append("<CalibrationPoints>\n");
		sbMap.append("<CalibrationPoint corner=\"TL\" lon=\"" + longitudeMin + "\" lat=\""
				+ latitudeMax + "\" />\n");
		sbMap.append("<CalibrationPoint corner=\"BR\" lon=\"" + longitudeMax + "\" lat=\""
				+ latitudeMin + "\" />\n");
		sbMap.append("<CalibrationPoint corner=\"TR\" lon=\"" + longitudeMax + "\" lat=\""
				+ latitudeMax + "\" />\n");
		sbMap.append("<CalibrationPoint corner=\"BL\" lon=\"" + longitudeMin + "\" lat=\""
				+ latitudeMin + "\" />\n");
		sbMap.append("</CalibrationPoints>\n");
		sbMap.append("</MapCalibration>\n");
		sbMap.append("</OruxTracker>\n");
		return sbMap.toString();
	}

	protected String prepareMainMapString(String mapName) {

		StringBuffer sbMap = new StringBuffer();
		sbMap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sbMap.append("<OruxTracker xmlns:orux=\"http://oruxtracker.com/app/res/calibration\"\n"
				+ " versionCode=\"2.1\">\n");
		sbMap.append("<MapCalibration layers=\"true\" layerLevel=\"" + 0 + "\">\n");
		sbMap.append("<MapName><![CDATA[" + mapName + "]]></MapName>\n");
		sbMap.append("</MapCalibration>\n");
		sbMap.append("</OruxTracker>\n");
		return sbMap.toString();

	}
}
