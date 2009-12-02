package tac.program.atlascreators;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.MapCreationException;
import tac.mapsources.mapspace.MercatorPower2MapSpace;
import tac.program.interfaces.TileImageDataWriter;
import tac.program.model.AtlasOutputFormat;
import tac.utilities.MyMath;
import tac.utilities.Utilities;

/**
 * Extends the {@link TrekBuddy} so that custom tiles are written. Custom tiles
 * can have a size different of 255x255 pixels), a different color depth and a
 * different image type (jpg/png).
 * 
 * @author r_x
 */
public class TrekBuddyCustom extends TrekBuddy {

	private int realWidth;
	private int realHeight;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		// if (mapSource instanceof MultiLayerMapSource)
		// return false;
		return (mapSource.getMapSpace() instanceof MercatorPower2MapSpace);
	}

	public void createMap() throws MapCreationException {
		try {
			Utilities.mkDirs(mapFolder);

			// write the .map file containing the calibration points
			writeMapFile();

			// This means there should not be any resizing of the tiles.
			if (atlasOutputFormat == AtlasOutputFormat.TaredAtlas)
				mapTileWriter = new TarTileWriter();
			else
				mapTileWriter = new FileTileWriter();

			// Select the tile creator instance based on whether tile image
			// parameters has been set or not
			if (parameters != null)
				createTiles();
			else
				super.createTiles();

			mapTileWriter.finalizeMap();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		} catch (MapCreationException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(e);
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
	@Override
	protected void createTiles() throws InterruptedException {
		log.debug("Starting map creation using custom parameters: " + parameters);

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
		if (realWidth > mergedWidth)
			realWidth = mergedWidth;
		if (realHeight > mergedHeight)
			realHeight = mergedHeight;

		int customTileCount = MyMath.divCeil(mergedWidth, realWidth)
				* MyMath.divCeil(mergedHeight, realHeight);
		atlasProgress.initMapCreation(customTileCount);

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
		TileImageDataWriter tileImageDataWriter = parameters.getFormat().getDataWriter();
		tileImageDataWriter.initialize();
		try {

			int yRelPos = 0;
			while (yAbsPos < yEnd) {
				int xRelPos = 0;
				xAbsPos = xStart;
				while (xAbsPos < xEnd) {
					checkUserAbort();
					atlasProgress.incMapCreationProgress();
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