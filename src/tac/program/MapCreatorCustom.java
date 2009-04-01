package tac.program;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ColorQuantizerDescriptor;
import javax.swing.JOptionPane;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.gui.AtlasProgress;
import tac.program.model.AtlasOutputFormat;
import tac.program.model.MapSlice;
import tac.program.model.TileImageColorDepth;
import tac.tar.TarIndex;
import tac.utilities.MyMath;
import tac.utilities.Png4BitWriter;

/**
 * Extends the {@link MapCreator} so that custom tiles are written. Custom tiles
 * can have a size different of 255x255 pixels), a different color depth and a
 * different image type (jpg/png).
 * 
 * @author r_x
 */
public class MapCreatorCustom extends MapCreator {

	public enum TileImageFormat {
		Unchanged, Png, Jpg
	};

	private TileImageParameters param;

	private String targetFileType;

	public MapCreatorCustom(MapSlice smp, TarIndex tileIndex, File atlasFolder, String mapName,
			TileSource tileSource, int zoom, AtlasOutputFormat atlasOutputFormat, int mapNumber,
			TileImageParameters parameters) {
		super(smp, tileIndex, atlasFolder, mapName, tileSource, zoom, atlasOutputFormat, mapNumber);
		this.param = parameters;
		switch (param.format) {
		case Unchanged: {
			targetFileType = tileSource.getTileType();
			break;
		}
		case Jpg: {
			targetFileType = "jpg";
			break;
		}
		default:
			targetFileType = "png";
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

		if (param.width > mergedWidth || param.height > mergedHeight) {
			if (!tileSizeErrorNotified) {
				JOptionPane.showMessageDialog(null,
						"Tile size settings is too large: default of 256 will be used instead, ",
						"Information", JOptionPane.INFORMATION_MESSAGE);
				tileSizeErrorNotified = true;
			}
			super.createTiles();
			return;
		}

		AtlasProgress ap = null;
		if (t instanceof AtlasThread) {
			ap = ((AtlasThread) t).getAtlasProgress();
			int customTileCount = MyMath.divCeil(mergedWidth, param.width)
					* MyMath.divCeil(mergedHeight, param.height);
			ap.initMapSplice(customTileCount);
		}

		// Absolute positions
		int xAbsPos = xStart;
		int yAbsPos = yStart;

		log.trace("tile size: " + param.width + " * " + param.height);
		log.trace("X: from " + xStart + " to " + xEnd);
		log.trace("Y: from " + yStart + " to " + yEnd);

		// We don't work with large images, therefore we can disable the (file)
		// cache of ImageIO. This will speed up the creation process a bit
		ImageIO.setUseCache(false);

		int yRelPos = 0;
		while (yAbsPos < yEnd) {
			int xRelPos = 0;
			xAbsPos = xStart;
			while (xAbsPos < xEnd) {
				if (t.isInterrupted())
					throw new InterruptedException();
				if (ap != null)
					ap.incMapProgress();
				BufferedImage tileImage = new BufferedImage(param.width, param.height,
						BufferedImage.TYPE_3BYTE_BGR);
				try {
					Graphics2D graphics = tileImage.createGraphics();
					String tileFileName = "t_" + xRelPos + "_" + yRelPos + "." + targetFileType;
					log.trace("Creating tile " + tileFileName);
					paintCustomTile(graphics, xAbsPos, yAbsPos);
					graphics.dispose();
					if (param.colorDepth != TileImageColorDepth.Unchanged) {
						int colors = 256;
						if (param.colorDepth == TileImageColorDepth.FourBit)
							colors = 16;

						RenderedOp ro = ColorQuantizerDescriptor.create(tileImage,
								ColorQuantizerDescriptor.MEDIANCUT, // 
								new Integer(colors), // Max number of colors
								null, null, new Integer(1), new Integer(1), null);
						tileImage = ro.getAsBufferedImage();
					}
					ByteArrayOutputStream buf = new ByteArrayOutputStream(32768);
					if ((param.colorDepth == TileImageColorDepth.FourBit)
							&& (targetFileType == "png"))
						Png4BitWriter.writeImage(buf, tileImage);
					else
						ImageIO.write(tileImage, targetFileType, buf);
					tileWriter.writeTile(tileFileName, buf.toByteArray());
				} catch (Exception e) {
					log.error("Error writing tile image: ", e);
				}

				xRelPos += param.width;
				xAbsPos += param.width;
			}
			yRelPos += param.height;
			yAbsPos += param.height;
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

		for (int x = xTileOffset; x < param.width; x += Tile.SIZE) {
			int yTile = yAbsPos / Tile.SIZE;
			int yTileOffset = -(yAbsPos % Tile.SIZE);
			for (int y = yTileOffset; y < param.height; y += Tile.SIZE) {
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
		String tileFileName = "y" + yTile + "x" + xTile + "." + targetFileType;
		byte[] sourceTileData = tileIndex.getEntryContent(tileFileName);
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

	public static class TileImageParameters {
		public int width;
		public int height;
		public TileImageFormat format;
		public TileImageColorDepth colorDepth;
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