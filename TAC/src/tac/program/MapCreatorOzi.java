package tac.program;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.Tile;

import tac.gui.AtlasProgress;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;
import tac.utilities.Utilities;
import tac.utilities.imageio.PngXxlWriter;

/**
 * 
 */
public class MapCreatorOzi extends MapCreator {

	private File mapDir;

	public MapCreatorOzi(MapInterface map, TarIndex tarTileIndex, File atlasDir) {
		super(map, tarTileIndex, atlasDir);
		mapDir = new File(atlasDir, map.getLayer().getName());
	}

	public void createMap() {
		mapDir.mkdirs();
		try {
			createTiles();
			writeMapFile();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		}
	}

	protected void writeMapFile() {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(mapDir, map.getName() + ".map"));
			writeMapFile(map.getName() + ".png", fout);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(fout);
		}
	}

	/**
	 * Writes the large picture (tile) line by line. Each line has the full
	 * width of the map and the height of one tile (256 pixels).
	 */
	@Override
	protected void createTiles() throws InterruptedException {
		Thread t = Thread.currentThread();
		AtlasProgress ap = null;
		if (t instanceof AtlasThread) {
			ap = ((AtlasThread) t).getAtlasProgress();
			ap.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		}
		ImageIO.setUseCache(false);

		int width = (xMax - xMin + 1) * Tile.SIZE;
		int height = (yMax - yMin + 1) * Tile.SIZE;
		int tileLineHeight = Tile.SIZE;

		FileOutputStream fileOs = null;
		try {
			fileOs = new FileOutputStream(new File(mapDir, map.getName() + ".png"));
			PngXxlWriter pngWriter = new PngXxlWriter(width, height, fileOs);

			for (int y = yMin; y <= yMax; y++) {
				BufferedImage lineImage = new BufferedImage(width, tileLineHeight,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = lineImage.createGraphics();
				try {
					int lineX = 0;
					for (int x = xMin; x <= xMax; x++) {
						if (t.isInterrupted())
							throw new InterruptedException();
						if (ap != null)
							ap.incMapCreationProgress();
						try {
							byte[] sourceTileData = tarTileIndex.getEntryContent("y" + y + "x" + x
									+ "." + mapSource.getTileType());
							if (sourceTileData != null) {
								BufferedImage tile = ImageIO.read(new ByteArrayInputStream(
										sourceTileData));
								graphics.drawImage(tile, lineX, 0, Color.WHITE, null);
							}
						} catch (IOException e) {
							log.error("", e);
						}
						lineX += Tile.SIZE;
					}
				} finally {
					graphics.dispose();
				}
				pngWriter.writeTileLine(lineImage);
			}
			pngWriter.finish();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(fileOs);
		}
	}
}