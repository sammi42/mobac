package mobac.program.atlascreators.impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.exceptions.MapCreationException;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageParameters;
import mobac.utilities.MyMath;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class MapTileBuilder {

	private static final Logger log = Logger.getLogger(MapTileBuilder.class);

	private final AtlasCreator atlasCreator;
	private final TileProvider mapDlTileProvider;
	private final MapInterface map;
	private final MapSource mapSource;
	private final TileImageParameters parameters;
	private final TileImageDataWriter tileImageDataWriter;
	private final int tileSize;
	private final int xMin;
	private final int xMax;
	private final int yMin;
	private final int yMax;

	private final boolean useRealTileSize;

	private int realWidth;
	private int realHeight;
	int mergedWidth;
	int mergedHeight;

	final int customTileCount;
	final int xStart;
	final int yStart;
	final int xEnd;
	final int yEnd;

	protected final MapTileWriter mapTileWriter;

	public MapTileBuilder(AtlasCreator atlasCreator, MapTileWriter mapTileWriter,
			boolean useRealTileSize) {
		this(atlasCreator, atlasCreator.getParameters().getFormat().getDataWriter(), mapTileWriter,
				useRealTileSize);
	}

	public MapTileBuilder(AtlasCreator atlasCreator, TileImageDataWriter tileImageDataWriter,
			MapTileWriter mapTileWriter, boolean useRealTileSize) {
		this.atlasCreator = atlasCreator;
		this.tileImageDataWriter = tileImageDataWriter;
		this.mapTileWriter = mapTileWriter;
		this.mapDlTileProvider = atlasCreator.getMapDlTileProvider();
		this.useRealTileSize = useRealTileSize;
		map = atlasCreator.getMap();
		mapSource = map.getMapSource();
		tileSize = mapSource.getMapSpace().getTileSize();
		xMax = atlasCreator.getXMax();
		xMin = atlasCreator.getXMin();
		yMax = atlasCreator.getYMax();
		yMin = atlasCreator.getYMin();
		parameters = atlasCreator.getParameters();
		// left upper point on the map in pixels
		// regarding the current zoom level
		xStart = xMin * tileSize;
		yStart = yMin * tileSize;

		// lower right point on the map in pixels
		// regarding the current zoom level
		xEnd = xMax * tileSize + (tileSize - 1);
		yEnd = yMax * tileSize + (tileSize - 1);

		mergedWidth = xEnd - xStart + 1;
		mergedHeight = yEnd - yStart + 1;

		// Reduce tile size of overall map height/width is smaller that one tile
		realWidth = parameters.getWidth();
		realHeight = parameters.getHeight();
		if (realWidth > mergedWidth)
			realWidth = mergedWidth;
		if (realHeight > mergedHeight)
			realHeight = mergedHeight;

		customTileCount = MyMath.divCeil(mergedWidth, realWidth)
				* MyMath.divCeil(mergedHeight, realHeight);
	}

	public void createTiles() throws MapCreationException, InterruptedException {

		// Absolute positions
		int xAbsPos = xStart;
		int yAbsPos = yStart;

		log.trace("tile size: " + realWidth + " * " + realHeight);
		log.trace("X: from " + xStart + " to " + xEnd);
		log.trace("Y: from " + yStart + " to " + yEnd);

		// We don't work with large images, therefore we can disable the (file)
		// cache of ImageIO. This will speed up the creation process a bit
		ImageIO.setUseCache(false);
		ByteArrayOutputStream buf = new ByteArrayOutputStream(32768);
		tileImageDataWriter.initialize();
		int currentTileHeight = realHeight;
		int currentTileWidth = realWidth;
		try {
			String tileType = tileImageDataWriter.getFileExt();
			int tiley = 0;
			while (yAbsPos < yEnd) {
				int tilex = 0;
				xAbsPos = xStart;
				if (useRealTileSize)
					currentTileHeight = Math.min(realHeight, yEnd - yAbsPos + 1);
				while (xAbsPos < xEnd) {
					if (useRealTileSize)
						currentTileWidth = Math.min(realWidth, xEnd - xAbsPos + 1);
					atlasCreator.checkUserAbort();
					atlasCreator.getAtlasProgress().incMapCreationProgress();
					BufferedImage tileImage = new BufferedImage(currentTileWidth,
							currentTileHeight, BufferedImage.TYPE_3BYTE_BGR);
					buf.reset();
					try {
						Graphics2D graphics = tileImage.createGraphics();
						prepareTile(graphics);
						paintCustomTile(graphics, xAbsPos, yAbsPos);
						graphics.dispose();
						tileImageDataWriter.processImage(tileImage, buf);
						mapTileWriter.writeTile(tilex, tiley, tileType, buf.toByteArray());
					} catch (IOException e) {
						throw new MapCreationException("Error writing tile image: "
								+ e.getMessage(), e);
					}

					tilex++;
					xAbsPos += realWidth;
				}
				tiley++;
				yAbsPos += realHeight;
			}
		} finally {
			tileImageDataWriter.dispose();
		}
	}

	protected void prepareTile(Graphics2D graphics) {
		graphics.setColor(mapSource.getBackgroundColor());
		graphics.fillRect(0, 0, realWidth, realHeight);
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
					if (orgTileImage != null) {
						int w = orgTileImage.getWidth();
						int h = orgTileImage.getHeight();
						graphics.drawImage(orgTileImage, xTileOffset, yTileOffset, w, h, null);
					}
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

	public int getCustomTileCount() {
		return customTileCount;
	}

	/**
	 * A simple local cache holding the last 10 loaded original tiles. If the
	 * custom tile size is smaller than 256x256 the efficiency of this cache is
	 * very high (~ 75% hit rate).
	 */
	private CachedTile[] cache = new CachedTile[10];
	private int cachePos = 0;

	private BufferedImage loadOriginalMapTile(int xTile, int yTile) throws Exception {
		for (CachedTile ct : cache) {
			if (ct == null)
				continue;
			if (ct.xTile == xTile && ct.yTile == yTile) {
				// log.trace("cache hit");
				return ct.image;
			}
		}
		// log.trace("cache miss");
		BufferedImage image = mapDlTileProvider.getTileImage(xTile, yTile);
		if (image == null)
			return null;
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
}
