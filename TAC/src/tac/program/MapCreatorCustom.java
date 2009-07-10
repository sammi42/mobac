package tac.program;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.Tile;

import tac.gui.AtlasProgress;
import tac.program.interfaces.MapInterface;
import tac.program.interfaces.TileImageDataWriter;
import tac.program.model.TileImageParameters;
import tac.tar.TarIndex;
import tac.utilities.MyMath;

/**
 * Extends the {@link MapCreator} so that custom tiles are written. Custom tiles
 * can have a size different of 255x255 pixels), a different color depth and a
 * different image type (jpg/png).
 * 
 * @author r_x
 */
public class MapCreatorCustom extends MapCreator {

	private TileImageParameters param;
	private int realWidth;
	private int realHeight;

	public MapCreatorCustom(MapInterface map, TarIndex tarTileIndex, File atlasDir,
			TileImageParameters parameters) {
		super(map, tarTileIndex, atlasDir);
		this.param = parameters;
	}

	/**
	 * New experimental custom tile size algorithm implementation.
	 * 
	 * It creates each custom sized tile separately. Therefore each original
	 * tile (256x256) will be loaded and painted multiple times. Therefore this
	 * implementation needs much more CPU power as each original tile is loaded
	 * at least once and each generated tile has to be saved.
	 */
	@Override
	protected void createTiles() throws InterruptedException {
		log.debug("Starting map creation using custom parameters: " + param);
		Thread t = Thread.currentThread();

		// left upper point on the map in pixels
		// regarding the current zoom level
		int xStart = xMin * Tile.SIZE;
		int yStart = yMin * Tile.SIZE;

		// lower right point on the map in pixels
		// regarding the current zoom level
		int xEnd = xMax * Tile.SIZE + (Tile.SIZE - 1);
		int yEnd = yMax * Tile.SIZE + (Tile.SIZE - 1);

		int mergedWidth = xEnd - xStart;
		int mergedHeight = yEnd - yStart;

		// Reduce tile size of overall map height/width is smaller that one tile
		realWidth = param.getWidth();
		realHeight = param.getHeight();
		if (realWidth > mergedWidth)
			realWidth = mergedWidth;
		if (realHeight > mergedHeight)
			realHeight = mergedHeight;

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

		log.trace("tile size: " + realWidth + " * " + realHeight);
		log.trace("X: from " + xStart + " to " + xEnd);
		log.trace("Y: from " + yStart + " to " + yEnd);

		// We don't work with large images, therefore we can disable the (file)
		// cache of ImageIO. This will speed up the creation process a bit
		ImageIO.setUseCache(false);
		ByteArrayOutputStream buf = new ByteArrayOutputStream(32768);
		TileImageDataWriter tileImageDataWriter = param.getFormat().getDataWriter();
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
						String tileFileName = "t_" + xRelPos + "_" + yRelPos + "."
								+ tileImageDataWriter.getFileExt();
						log.trace("Creating tile " + tileFileName);
						paintCustomTile(graphics, xAbsPos, yAbsPos);
						graphics.dispose();
						tileImageDataWriter.processImage(tileImage, buf);
						mapTileWriter.writeTile(tileFileName, buf.toByteArray());
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
		int xTile = xAbsPos / Tile.SIZE;
		int xTileOffset = -(xAbsPos % Tile.SIZE);

		for (int x = xTileOffset; x < realWidth; x += Tile.SIZE) {
			int yTile = yAbsPos / Tile.SIZE;
			int yTileOffset = -(yAbsPos % Tile.SIZE);
			for (int y = yTileOffset; y < realHeight; y += Tile.SIZE) {
				try {
					BufferedImage orgTileImage = loadOriginalMapTile(xTile, yTile);
					if (orgTileImage != null)
						graphics.drawImage(orgTileImage, xTileOffset, yTileOffset, orgTileImage
								.getWidth(), orgTileImage.getHeight(), null);
				} catch (Exception e) {
					log.error("Error while painting sub-tile", e);
				}
				yTile++;
				yTileOffset += Tile.SIZE;
			}
			xTile++;
			xTileOffset += Tile.SIZE;
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
		String tileFileName = "y" + yTile + "x" + xTile + "." + mapSource.getTileType();
		byte[] sourceTileData = tarTileIndex.getEntryContent(tileFileName);
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
}