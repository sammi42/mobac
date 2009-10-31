package tac.tilestore;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.DirectoryManager;
import tac.program.model.Settings;
import tac.tilestore.berkeleydb.BerkeleyDbTileStore;

public abstract class TileStore {

	private static TileStore INSTANCE = null;

	protected Logger log;

	protected File tileStoreDir;

	public static TileStore getInstance() {
		if (INSTANCE != null)
			return INSTANCE;
		synchronized (TileStore.class) {
			if (INSTANCE != null)
				return INSTANCE;
			INSTANCE = new BerkeleyDbTileStore();
		}
		return INSTANCE;
	}

	protected TileStore() {
		log = Logger.getLogger(this.getClass());
		String tileStorePath = Settings.getInstance().tileStoreDirectory;
		if (tileStorePath != null)
			tileStoreDir = new File(tileStorePath);
		else
			tileStoreDir = new File(DirectoryManager.currentDir, "tilestore");
		log.debug("Tile store path: " + tileStoreDir);
	}

	public abstract void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource)
			throws IOException;

	public abstract void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource,
			long timeLastModified, long timeExpires, String eTag) throws IOException;

	/**
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @param mapSource
	 * @return
	 */
	public abstract byte[] getTileData(int x, int y, int zoom, MapSource mapSource);

	public abstract boolean contains(int x, int y, int zoom, MapSource mapSource);

	public abstract void prepareTileStore(MapSource mapSource);

	public abstract void clearStore(MapSource mapSource);

	/**
	 * This method returns the amount of tiles in the store of tiles which is
	 * specified by the {@link MapSource} object.
	 * 
	 * @param mapSource
	 *            the store to calculate number of tiles in
	 * @return the amount of tiles in the specified store.
	 * @throws InterruptedException
	 */
	public abstract int getNrOfTiles(MapSource mapSource) throws InterruptedException;

	/**
	 * Returns the size in bytes occupied by all entries of the tile store
	 * instance belonging to <code>mapSource</code>
	 * 
	 * @param mapSource
	 * @return
	 * @throws InterruptedException
	 */
	public abstract long getStoreSize(MapSource mapSource) throws InterruptedException;

	/**
	 * Returns <code>true</code> if the tile store directory of the specified
	 * {@link MapSource} exists.
	 * 
	 * @param mapSource
	 * @return
	 */
	public abstract boolean storeExists(MapSource mapSource);

	public abstract TileStoreInfo getStoreInfo(MapSource mapSource) throws InterruptedException;

	public void closeAll(boolean shutdown) {
	};

}