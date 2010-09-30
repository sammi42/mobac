package mobac.program.atlascreators;

import java.io.File;
import java.io.IOException;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.interfaces.AtlasInterface;
import mobac.utilities.Utilities;
import mobac.utilities.stream.ZipStoreOutputStream;

/**
 * 
 * @see http://sourceforge.net/tracker/?func=detail&atid=1105497&aid=3078443&group_id=238075
 */
public class OSMDroid extends OSMTracker {

	protected ZipStoreOutputStream zipStream = null;

	public void createMap() throws MapCreationException, InterruptedException {
		createTiles();
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws AtlasTestException, IOException,
			InterruptedException {
		super.startAtlasCreation(atlas, customAtlasDir);
		zipStream = new ZipStoreOutputStream(new File(atlasDir, atlas.getName() + ".zip"));
		mapTileWriter = new OSMDroidTileWriter();
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		Utilities.closeStream(zipStream);
		super.abortAtlasCreation();
	}

	@Override
	public void finishAtlasCreation() throws IOException, InterruptedException {
		Utilities.closeStream(zipStream);
		super.finishAtlasCreation();
	}

	private class OSMDroidTileWriter implements MapTileWriter {

		public void finalizeMap() throws IOException {
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {
			String tileName = map.getMapSource().getStoreName() + "/"
					+ String.format(tileFileNamePattern, zoom, tilex, tiley, tileType);
			zipStream.writeStoredEntry(tileName, tileData);
		}
	}
}
