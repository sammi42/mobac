package tiledb.berkeley;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.DirectoryManager;
import tac.program.model.Settings;
import tac.utilities.Utilities;
import tac.utilities.file.DeleteFileFilter;
import tiledb.berkeley.TileDbEntry.TileDbKey;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class TileStore {

	private static TileStore INSTANCE = null;

	private File tileStoreDir;

	private Logger log;

	private EnvironmentConfig envConfig;

	private TileStore() {
		log = Logger.getLogger(this.getClass());

		envConfig = new EnvironmentConfig();
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(true);

		String tileStorePath = Settings.getInstance().tileStoreDirectory;
		if (tileStorePath != null)
			tileStoreDir = new File(tileStorePath);
		else
			tileStoreDir = new File(DirectoryManager.userHomeDir, "tilestore");
	}

	public static TileStore getInstance() {
		if (INSTANCE != null)
			return INSTANCE;
		synchronized (TileStore.class) {
			if (INSTANCE == null)
				INSTANCE = new TileStore();
			return INSTANCE;
		}
	}

	private TileDatabase getTileDatabase(MapSource mapSource) {
		return null; // TODO: implement
	}

	public TileDbEntry getTile(int x, int y, int zoom, MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return null;
		try {
			return getTileDatabase(mapSource).get(new TileDbKey(zoom, x, y));
		} catch (DatabaseException e) {
			return null;
		}
	}

	public boolean contains(int x, int y, int zoom, MapSource mapSource) {
		try {
			return getTileDatabase(mapSource).contains(new TileDbKey(zoom, x, y));
		} catch (DatabaseException e) {
			log.error("", e);
			return false;
		}
	}

	public void prepareTileStore(MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return;
		getTileDatabase(mapSource);
	}

	public void clearStore(MapSource tileSource) {
		File tileStore = new File(tileStoreDir, tileSource.getName());
		// TODO: Close opened tile database if open
		if (tileStore.exists()) {
			DeleteFileFilter dff = new DeleteFileFilter();
			tileStore.listFiles(dff);
			tileStore.delete();
			log.debug("Tilestore " + tileSource.getName() + " cleared: " + dff);
		}
	}

	/**
	 * This method returns the amount of tiles in the store of tiles which is
	 * specified by the TileSource object.
	 * 
	 * @param mapSource
	 *            the store to calculate number of tiles in
	 * @return the amount of tiles in the specified store.
	 * @throws InterruptedException
	 */
	public long getNrOfTiles(MapSource mapSource) throws InterruptedException {
		try {
			return getTileDatabase(mapSource).entryCount();
		} catch (DatabaseException e) {
			log.error("", e);
			return -1;
		}
	}

	public long getStoreSize(MapSource tileSource) throws InterruptedException {
		return 0;
	}

	/**
	 * Returns <code>true</code> if the tile store directory of the specified
	 * {@link MapSource} exists.
	 * 
	 * @param tileSource
	 * @return
	 */
	public boolean storeExists(MapSource tileSource) {
		File tileStore = new File(tileStoreDir, tileSource.getName());
		return (tileStore.isDirectory()) && (tileStore.exists());
	}

	public static class TileStoreInfo {

		int tileCount;
		long storeSize;

		public TileStoreInfo(long storeSize, int tileCount) {
			super();
			this.storeSize = storeSize;
			this.tileCount = tileCount;
		}

		public int getTileCount() {
			return tileCount;
		}

		public long getStoreSize() {
			return storeSize;
		}

	}

	protected class TileDatabase {

		final MapSource mapSource;
		final Environment env;
		final EntityStore store;
		final PrimaryIndex<TileDbKey, TileDbEntry> tileIndex;
		boolean dbOpen;

		public TileDatabase(MapSource mapSource) throws IOException, EnvironmentLockedException, DatabaseException {
			this.mapSource = mapSource;

			File storeDir = new File(tileStoreDir, mapSource.getName());
			Utilities.mkDirs(storeDir);

			env = new Environment(storeDir, envConfig);

			StoreConfig storeConfig = new StoreConfig();
			storeConfig.setAllowCreate(true);
			storeConfig.setTransactional(true);
			store = new EntityStore(env, "TilesEntityStore", storeConfig);

			tileIndex = store.getPrimaryIndex(TileDbKey.class, TileDbEntry.class);
		}

		public long entryCount() throws DatabaseException {
			return tileIndex.count();
		}

		public void clear() throws DatabaseException {

			Transaction txn = env.beginTransaction(null, null);
			EntityCursor<TileDbEntry> cursor = null;
			try {
				cursor = tileIndex.entities(txn, CursorConfig.DEFAULT);
				TileDbEntry td = cursor.first();
				while (td != null) {
					cursor.delete();
					td = cursor.next();
				}
			} catch (DatabaseException e) {
				txn.abort();
				throw e;
			} finally {
				if (cursor != null)
					cursor.close();
				txn.commitSync();
			}
		}

		public void put(TileDbEntry tile) throws DatabaseException {
			Transaction txn = env.beginTransaction(null, null);
			try {
				tileIndex.put(txn, tile);
			} finally {
				txn.commit();
			}
		}

		public boolean contains(TileDbKey key) throws DatabaseException {
			return tileIndex.contains(key);
		}

		public TileDbEntry get(TileDbKey key) throws DatabaseException {
			return tileIndex.get(key);
		}

		protected void purge() {
			try {
				env.cleanLog();
			} catch (DatabaseException e) {
				log.error("database compression failed: ", e);
			}
		}

		public synchronized void close() {
			try {
				if (store != null)
					store.close();
			} catch (DatabaseException e) {
			}
			try {
				env.close();
			} catch (DatabaseException e) {
			}
		}

		@Override
		protected void finalize() throws Throwable {
			close();
			super.finalize();
		}

	}
}