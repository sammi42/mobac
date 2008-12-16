package tac.program;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.gui.AtlasProgress;
import tac.utilities.MyMath;

/**
 * Extends the {@link MapCreator} so that tiles of custom size are written.
 * 
 * @author r_x
 */
public class MapCreatorCustomTileSize extends MapCreator {

	public MapCreatorCustomTileSize(SubMapProperties smp, File oziFolder, File atlasFolder,
			String mapName, TileSource tileSource, int zoom, int mapNumber, int tileSizeWidth,
			int tileSizeHeight) {
		super(smp, oziFolder, atlasFolder, mapName, tileSource, zoom, mapNumber, tileSizeWidth,
				tileSizeHeight);
	}

	/**
	 * New experimental custom tile size algorithm implementation.
	 * 
	 * It creates each custom sized tile separately. Therefore each original
	 * tile (256x256) will be loaded and painted multiple times. Therefore this
	 * implementation needs much more CPU power as each original tile is loaded
	 * up to 4-6 times.
	 * 
	 * @param setFolder
	 * @throws InterruptedException
	 */
	@Override
	protected void createTiles(File setFolder) throws InterruptedException {
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

		if (tileSizeWidth > mergedWidth || tileSizeHeight > mergedHeight) {
			if (!tileSizeErrorNotified) {
				JOptionPane.showMessageDialog(null,
						"Tile size settings is too large: default of 256 will be used instead, ",
						"Information", JOptionPane.INFORMATION_MESSAGE);
				tileSizeErrorNotified = true;
			}
			super.createTiles(setFolder);
			return;
		}

		AtlasProgress ap = null;
		if (t instanceof AtlasThread) {
			ap = ((AtlasThread) t).getAtlasProgress();
			int customTileCount = MyMath.divCeil(mergedWidth, tileSizeWidth)
					* MyMath.divCeil(mergedHeight, tileSizeHeight);
			ap.initMapProgressBar(customTileCount);
		}

		// Absolute positions
		int xAbsPos = xStart;
		int yAbsPos = yStart;

		log.trace("tile size: " + tileSizeWidth + " * " + tileSizeHeight);
		log.trace("X: from " + xStart + " to " + xEnd);
		log.trace("Y: from " + yStart + " to " + yEnd);

		int yRelPos = 0;
		while (yAbsPos < yEnd) {
			int xRelPos = 0;
			xAbsPos = xStart;
			while (xAbsPos < xEnd) {
				if (t.isInterrupted())
					throw new InterruptedException();
				if (ap != null)
					ap.incMapProgressBar();
				BufferedImage tileImage = new BufferedImage(tileSizeWidth, tileSizeHeight,
						BufferedImage.TYPE_3BYTE_BGR);
				Graphics2D graphics = tileImage.createGraphics();
				File fDest = new File(setFolder, layerName + "_" + xRelPos + "_" + yRelPos + "."
						+ tileSource.getTileType());
				log.trace("Creating tile " + fDest.getName());
				paintCustomTile(graphics, xAbsPos, yAbsPos);
				try {
					graphics.dispose();
					ImageIO.write(tileImage, tileSource.getTileType(), fDest);
					setFiles.add(fDest.getName());
				} catch (IOException e) {
					log.error("Error writing tile image: ", e);
				}

				xRelPos += tileSizeWidth;
				xAbsPos += tileSizeWidth;
			}
			yRelPos += tileSizeHeight;
			yAbsPos += tileSizeHeight;
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

		for (int x = xTileOffset; x < tileSizeWidth; x += Tile.SIZE) {
			int yTile = yAbsPos / Tile.SIZE;
			int yTileOffset = -(yAbsPos % Tile.SIZE);
			for (int y = yTileOffset; y < tileSizeHeight; y += Tile.SIZE) {
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
	 * custom zile size is smaller than 256x256 the efficiency of this cache is
	 * very high (~ 75% hit rate).
	 */
	private CachedTile[] cache = new CachedTile[10];
	private int cachePos = 0;

	private BufferedImage loadOriginalMapTile(int xTile, int yTile) throws Exception {
		String tileFileName = "y" + yTile + "x" + xTile + "." + tileSource.getTileType();
		File fSource = (File) tilesInFileFormat.get(tileFileName);
		if (fSource == null)
			return null;
		for (CachedTile ct : cache) {
			if (ct == null)
				continue;
			if (ct.xTile == xTile && ct.yTile == yTile) {
				//log.trace("cache hit");
				return ct.image;
			}
		}
		//log.trace("cache miss");
		BufferedImage image = ImageIO.read(fSource);
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