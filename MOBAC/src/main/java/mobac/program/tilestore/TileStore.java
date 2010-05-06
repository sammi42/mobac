package mobac.program.tilestore;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import mobac.exceptions.TileStoreException;
import mobac.program.DirectoryManager;
import mobac.program.model.Settings;
import mobac.program.tilestore.berkeleydb.BerkeleyDbTileStore;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public abstract class TileStore {

	private static TileStore INSTANCE = null;

	protected Logger log;

	protected File tileStoreDir;

	public static synchronized void initialize() {
		if (INSTANCE != null)
			return;
		try {
			INSTANCE = new BerkeleyDbTileStore();
		} catch (TileStoreException e) {
			String errMsg = "Multiple instances of Mobile Atlas Creator are trying "
					+ "to access the same tile store.\n"
					+ "The tile store can only be used by used by one instance at a time.\n"
					+ "Please close the other instance and try again.";
			JOptionPane.showMessageDialog(null, errMsg, "Multiple instances of Mobile "
					+ "Atlas Creator running", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public static TileStore getInstance() {
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
	public abstract TileStoreEntry getTile(int x, int y, int zoom, MapSource mapSource);

	public abstract boolean contains(int x, int y, int zoom, MapSource mapSource);

	public abstract void prepareTileStore(MapSource mapSource);

	public abstract void clearStore(MapSource mapSource);

	/**
	 * Returns <code>true</code> if the tile store directory of the specified
	 * {@link MapSource} exists.
	 * 
	 * @param mapSource
	 * @return
	 */
	public abstract boolean storeExists(MapSource mapSource);

	/**
	 * 
	 * @param mapSource
	 * @return
	 * @throws InterruptedException
	 */
	public abstract TileStoreInfo getStoreInfo(MapSource mapSource) throws InterruptedException;

	public abstract BufferedImage getCacheCoverage(MapSource mapSource, int zoom, Point tileNumMin,
			Point tileNumMax);

	/**
	 * 
	 * @param shutdown
	 *            if <code>true</code> the application wide tile store will
	 *            shutdown so that it can not be used again.
	 */
	public abstract void closeAll(boolean shutdown);

	public abstract void putTile(TileStoreEntry tile, MapSource mapSource);

	public abstract TileStoreEntry createNewEntry(int x, int y, int zoom, byte[] data,
			long timeLastModified, long timeExpires, String eTag);

}