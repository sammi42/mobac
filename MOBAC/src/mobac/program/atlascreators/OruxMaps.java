package mobac.program.atlascreators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

public class OruxMaps extends AtlasCreator {
	protected static final Color BG_COLOR = new Color(203, 211, 243);
	
	protected File mapFolder;
	protected FileTileWriter2 mapTileWriter;
	private TileImageParameters param;
	private int realWidth;
	private int realHeight;
	private CachedTile[] cache;
	private int cachePos;

	public OruxMaps() {
		this.cache = new CachedTile[10];
		this.cachePos = 0;
	}

	public boolean testMapSource(MapSource mapSource) {
		return mapSource.getMapSpace() instanceof MercatorPower2MapSpace;
	}

	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		LayerInterface layer = map.getLayer();
		this.mapFolder = new File(new File(this.atlasDir, layer.getName()), map.getName());

		if (this.parameters == null)
			this.param = new TileImageParameters(512, 512, TileImageFormat.JPEG90);
		else
			this.param = new TileImageParameters(512, 512, this.parameters.getFormat());
	}

	public void createMap() throws MapCreationException {
		try {
			Utilities.mkDirs(this.mapFolder);

			String otrk2 = this.mapFolder.getParent().toString() + File.separator
					+ this.map.getName().substring(0, this.map.getName().length() - 3)
					+ ".otrk2.xml";

			if (!(new File(otrk2).exists())) {
				writeMainMapFile(otrk2);
			}

			writeOtrk2XmlFile();

			this.mapTileWriter = new FileTileWriter2();
			createTiles();
			this.mapTileWriter.finalizeMap();

			this.mapTileWriter.finalizeMap();
		} catch (InterruptedException e) {
			return;
		} catch (Exception e) {
			throw new MapCreationException(e);
		}
	}

	private void writeMainMapFile(String mainMap) {
		this.log.trace("Writing MainMap file");

		File mapFile = new File(mainMap);
		FileOutputStream mapFileStream = null;
		try {
			OutputStreamWriter mapWriter = new OutputStreamWriter(new FileOutputStream(mapFile),
					"UTF8");
			mapWriter.write(prepareMainMapString(this.map.getName().substring(0,
					this.map.getName().length() - 3)));
			mapWriter.flush();
		} catch (IOException e) {
			this.log.error("", e);
		} finally {
			Utilities.closeStream(mapFileStream);
		}
	}

	protected void writeOtrk2XmlFile() {
		File mapFile = new File(this.mapFolder, this.map.getName() + ".otrk2.xml");
		FileOutputStream mapFileStream = null;
		try {
			mapFileStream = new FileOutputStream(mapFile);
			writeMapFile(mapFileStream);
		} catch (IOException e) {
			this.log.error("", e);
		} finally {
			Utilities.closeStream(mapFileStream);
		}
	}

	protected void writeMapFile(OutputStream stream) throws IOException {
		this.log.trace("Writing map file");
		OutputStreamWriter mapWriter = new OutputStreamWriter(stream, "UTF8");
		MapSpace mapSpace = this.mapSource.getMapSpace();
		double longitudeMin = mapSpace.cXToLon(this.xMin * this.tileSize, this.zoom);
		double longitudeMax = mapSpace.cXToLon((this.xMax + 1) * this.tileSize, this.zoom);
		double latitudeMin = mapSpace.cYToLat((this.yMax + 1) * this.tileSize, this.zoom);
		double latitudeMax = mapSpace.cYToLat(this.yMin * this.tileSize, this.zoom);
		int width = (this.xMax - this.xMin + 1) * this.tileSize;
		int height = (this.yMax - this.yMin + 1) * this.tileSize;
		mapWriter.write(prepareMapString(this.map.getName(), longitudeMin, longitudeMax,
				latitudeMin, latitudeMax, width, height));

		mapWriter.flush();
	}

	protected void createTiles() throws InterruptedException {
		Thread t = Thread.currentThread();

		int xStart = this.xMin * this.tileSize;
		int yStart = this.yMin * this.tileSize;

		int xEnd = this.xMax * this.tileSize + this.tileSize - 1;
		int yEnd = this.yMax * this.tileSize + this.tileSize - 1;

		int mergedWidth = xEnd - xStart;
		int mergedHeight = yEnd - yStart;

		this.realWidth = this.param.getWidth();
		this.realHeight = this.param.getHeight();

		AtlasProgress ap = null;
		if (t instanceof AtlasThread) {
			ap = ((AtlasThread) t).getAtlasProgress();
			int customTileCount = MyMath.divCeil(mergedWidth, this.realWidth)
					* MyMath.divCeil(mergedHeight, this.realHeight);

			ap.initMapCreation(customTileCount);
		}

		int xAbsPos = xStart;
		int yAbsPos = yStart;

		ImageIO.setUseCache(false);
		ByteArrayOutputStream buf = new ByteArrayOutputStream(32768);
		TileImageDataWriter tileImageDataWriter = this.param.getFormat().getDataWriter();
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
					BufferedImage tileImage = new BufferedImage(this.realWidth, this.realHeight, 5);

					buf.reset();
					try {
						Graphics2D graphics = tileImage.createGraphics();
						String tileFileName = this.map.getName() + "_" + (xRelPos / 512) + "_"
								+ (yRelPos / 512) + ".omc2";
						graphics.setColor(BG_COLOR);
						graphics.fillRect(0, 0, tileImage.getWidth(), tileImage.getHeight());

						paintCustomTile(graphics, xAbsPos, yAbsPos);
						graphics.dispose();
						tileImageDataWriter.processImage(tileImage, buf);
						this.mapTileWriter.writeTile(tileFileName, buf.toByteArray());
					} catch (Exception e) {
						this.log.error("Error writing tile image: ", e);
					}

					xRelPos += this.realWidth;
					xAbsPos += this.realWidth;
				}
				yRelPos += this.realHeight;
				yAbsPos += this.realHeight;
			}
		} finally {
			tileImageDataWriter.dispose();
		}
	}

	private void paintCustomTile(Graphics2D graphics, int xAbsPos, int yAbsPos) {
		int xTile = xAbsPos / this.tileSize;
		int xTileOffset = -(xAbsPos % this.tileSize);

		for (int x = xTileOffset; x < this.realWidth; x += this.tileSize) {
			int yTile = yAbsPos / this.tileSize;
			int yTileOffset = -(yAbsPos % this.tileSize);
			for (int y = yTileOffset; y < this.realHeight; y += this.tileSize) {
				try {
					BufferedImage orgTileImage = loadOriginalMapTile(xTile, yTile);
					if (orgTileImage != null)
						graphics.drawImage(orgTileImage, xTileOffset, yTileOffset, orgTileImage
								.getWidth(), orgTileImage.getHeight(), null);
				} catch (Exception e) {
					this.log.error("Error while painting sub-tile", e);
				}
				++yTile;
				yTileOffset += this.tileSize;
			}
			++xTile;
			xTileOffset += this.tileSize;
		}
	}

	private BufferedImage loadOriginalMapTile(int xTile, int yTile) throws Exception {
		byte[] sourceTileData = this.mapDlTileProvider.getTileData(xTile, yTile);
		if (sourceTileData == null)
			return null;
		for (CachedTile ct : this.cache) {
			if (ct == null)
				continue;
			if ((ct.xTile == xTile) && (ct.yTile == yTile)) {
				return ct.image;
			}
		}

		BufferedImage image = ImageIO.read(new ByteArrayInputStream(sourceTileData));
		this.cache[this.cachePos] = new CachedTile(image, xTile, yTile);
		this.cachePos = ((this.cachePos + 1) % this.cache.length);
		return image;
	}

	protected String prepareMapString(String fileName, double longitudeMin, double longitudeMax,
			double latitudeMin, double latitudeMax, int width, int height) {
		StringBuffer sbMap = new StringBuffer();

		sbMap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sbMap
				.append("<OruxTracker xmlns:orux=\"http://oruxtracker.com/app/res/calibration\"\n versionCode=\"2.1\">\n");

		sbMap.append("<MapCalibration layers=\"false\" layerLevel=\"" + this.map.getZoom()
				+ "\">\n");
		sbMap.append("<MapName><![CDATA[" + fileName + "]]></MapName>\n");

		String mapFileName = fileName.replaceAll("&", "&amp;");
		mapFileName = mapFileName.replaceAll("<", "&lt;");
		mapFileName = mapFileName.replaceAll(">", "&gt;");
		mapFileName = mapFileName.replaceAll("\"", "&quot;");
		mapFileName = mapFileName.replaceAll("'", "&apos;");

		int mapWidth = (this.xMax - this.xMin + 1) * this.tileSize;
		int mapHeight = (this.yMax - this.yMin + 1) * this.tileSize;
		int numXimg = (mapWidth + 512 - 1) / 512;
		int numYimg = (mapHeight + 512 - 1) / 512;
		sbMap.append("<MapChunks xMax=\"" + numXimg + "\" yMax=\"" + numYimg + "\" datum=\""
				+ "WGS84" + "\" projection=\"" + "Mercator" + "\" img_height=\"" + 512
				+ "\" img_width=\"" + 512 + "\" file_name=\"" + mapFileName + "\" />\n");

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
		sbMap
				.append("<OruxTracker xmlns:orux=\"http://oruxtracker.com/app/res/calibration\"\n versionCode=\"2.1\">\n");

		sbMap.append("<MapCalibration layers=\"true\" layerLevel=\"0\">\n");
		sbMap.append("<MapName><![CDATA[" + mapName + "]]></MapName>\n");
		sbMap.append("</MapCalibration>\n");
		sbMap.append("</OruxTracker>\n");
		return sbMap.toString();
	}

	private class FileTileWriter2 {
		File setFolder;

		public FileTileWriter2() throws IOException {
			this.setFolder = new File(OruxMaps.this.mapFolder, "set");
			Utilities.mkDir(this.setFolder);
		}

		public void writeTile(String tileFileName, byte[] tileData) throws IOException {
			File f = new File(this.setFolder, tileFileName);
			FileOutputStream out = new FileOutputStream(f);
			try {
				out.write(tileData);
			} finally {
				Utilities.closeStream(out);
			}
		}

		public void finalizeMap() {
		}
	}

	private static class CachedTile {
		BufferedImage image;
		int xTile;
		int yTile;

		public CachedTile(BufferedImage image, int tile, int tile2) {
			this.image = image;
			this.xTile = tile;
			this.yTile = tile2;
		}
	}
}
