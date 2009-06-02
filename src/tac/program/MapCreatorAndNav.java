package tac.program;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import tac.gui.AtlasProgress;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;
import tac.utilities.Utilities;

/**
 * 
 */
public class MapCreatorAndNav extends MapCreator {

	private File mapZoomDir;

	public MapCreatorAndNav(MapInterface map, TarIndex tarTileIndex, File atlasDir) {
		super(map, tarTileIndex, atlasDir);
		File mapDir = new File(atlasDir, map.getMapSource().getName());
		mapZoomDir = new File(mapDir, Integer.toString(map.getZoom()));
	}

	public void createMap() {
		mapZoomDir.mkdirs();

		// This means there should not be any resizing of the tiles.
		try {
			mapTileWriter = new AndNavTileWriter();
			createTiles();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		}
	}

	@Override
	protected void createTiles() throws InterruptedException {
		Thread t = Thread.currentThread();
		AtlasProgress ap = null;
		if (t instanceof AtlasThread) {
			ap = ((AtlasThread) t).getAtlasProgress();
			ap.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		}
		ImageIO.setUseCache(false);

		for (int x = xMin; x <= xMax; x++) {
			File xDir = new File(mapZoomDir, Integer.toString(x));
			xDir.mkdir();
			for (int y = yMin; y <= yMax; y++) {
				if (t.isInterrupted())
					throw new InterruptedException();
				if (ap != null)
					ap.incMapCreationProgress();
				try {
					String tileFileName = x + "/" + y + "." + mapSource.getTileType() + ".andnav";
					byte[] sourceTileData = tarTileIndex.getEntryContent("y" + y + "x" + x + "."
							+ mapSource.getTileType());
					if (sourceTileData != null)
						mapTileWriter.writeTile(tileFileName, sourceTileData);
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
	}

	protected class AndNavTileWriter implements MapTileWriter {

		public void writeTile(String tileFileName, byte[] tileData) throws IOException {
			File f = new File(mapZoomDir, tileFileName);
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

}