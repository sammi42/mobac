package tac.tilestore;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.DirectoryManager;
import tac.program.model.Settings;

public abstract class TileStore {

	private static final TileStore INSTANCE = new FileTileStore();

	protected Logger log;

	protected File tileStoreDir;

	public static TileStore getInstance() {
		return INSTANCE;
	}

	protected TileStore() {
		log = Logger.getLogger(this.getClass());
		String tileStorePath = Settings.getInstance().tileStoreDirectory;
		if (tileStorePath != null)
			tileStoreDir = new File(tileStorePath);
		else
			tileStoreDir = new File(DirectoryManager.userHomeDir, "tilestore");
	}

	public abstract void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource)
			throws IOException;

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

	public abstract void clearStore(MapSource tileSource);

	/**
	 * This method returns the amount of tiles in the store of tiles which is
	 * specified by the TileSource object.
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

}