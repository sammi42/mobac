package tac.tilestore.berkeleydb;

import java.io.File;
import java.io.IOException;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.tilestore.TileStore;
import tac.tilestore.TileStoreInfo;
import tac.tilestore.berkeleydb.TileDbEntry.TileDbKey;
import tac.utilities.Utilities;
import tac.utilities.file.DeleteFileFilter;
import tac.utilities.file.DirInfoFileFilter;

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

/**
 * The new database based tile store implementation.
 */
public class BerkeleyDbTileStore extends TileStore {

	private EnvironmentConfig envConfig;

	private TileDatabase currentDb;
	private MapSource currentMapSource;

	private int tileStoreSyncCounter = 0;

	public BerkeleyDbTileStore() {
		super();

		envConfig = new EnvironmentConfig();
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(true);

		currentDb = null;
		currentMapSource = null;
	}

	@Override
	public TileStoreInfo getStoreInfo(MapSource tileSource) throws InterruptedException {
		return new TileStoreInfo(-1, -1);
	}

	@Override
	public byte[] getTileData(int x, int y, int zoom, MapSource mapSource) {
		TileDbEntry tile = getTile(x, y, zoom, mapSource);
		if (tile == null) {
			if (log.isTraceEnabled())
				log.trace("Tile store cache miss: (x,y,z)" + x + "/" + y + "/" + zoom + " "
						+ mapSource.getName());
			return null;
		}
		if (log.isTraceEnabled())
			log.trace("Loaded " + mapSource.getName() + " " + tile);
		return tile.getData();
	}

	@Override
	public void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource)
			throws IOException {
		this.putTileData(tileData, x, y, zoom, mapSource, -1, -1, null);
	}

	@Override
	public void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource,
			long timeLastModified, long timeExpires, String eTag) throws IOException {
		if (!mapSource.allowFileStore())
			return;
		TileDbEntry tile = new TileDbEntry(x, y, zoom, tileData, timeLastModified, timeExpires,
				eTag);
		try {
			if (log.isTraceEnabled())
				log.trace("Saved " + mapSource.getName() + " " + tile);
			getTileDatabase(mapSource).put(tile);
		} catch (DatabaseException e) {
			log.error("Faild to write tile to tile store \"" + mapSource.getName() + "\"", e);
		}
	}

	private TileDatabase getTileDatabase(MapSource mapSource) throws DatabaseException {
		if (currentMapSource != null && mapSource.getName().equals(currentMapSource.getName()))
			return currentDb;

		int x = tileStoreSyncCounter;
		synchronized (this) {
			// test if another thread has already loaded the required mapSource
			if (x != tileStoreSyncCounter && currentMapSource != null
					&& mapSource.getName().equals(currentMapSource.getName()))
				return currentDb;

			if (currentDb != null)
				currentDb.close();
			try {
				currentDb = new TileDatabase(mapSource);
				currentMapSource = mapSource;
				tileStoreSyncCounter++;
				return currentDb;
			} catch (Exception e) {
				log.error("Error creating tile store db \"" + mapSource.getName() + "\"", e);
				currentDb = null;
				currentMapSource = null;
				throw new DatabaseException(e);
			}
		}
	}

	public TileDbEntry getTile(int x, int y, int zoom, MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return null;
		try {
			return getTileDatabase(mapSource).get(new TileDbKey(x, y, zoom));
		} catch (DatabaseException e) {
			log.error("failed to retrieve tile from tile store \"" + mapSource.getName() + "\"", e);
			return null;
		}
	}

	public boolean contains(int x, int y, int zoom, MapSource mapSource) {
		try {
			return getTileDatabase(mapSource).contains(new TileDbKey(x, y, zoom));
		} catch (DatabaseException e) {
			log.error("", e);
			return false;
		}
	}

	public void prepareTileStore(MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return;
		try {
			getTileDatabase(mapSource);
		} catch (DatabaseException e) {
		}
	}

	public void clearStore(MapSource tileSource) {
		File tileStore = new File(tileStoreDir, tileSource.getName());
		if (tileSource.equals(currentMapSource)) {
			synchronized (this) {
				currentDb.close();
				currentDb = null;
				currentMapSource = null;
			}
		}
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
	public int getNrOfTiles(MapSource mapSource) throws InterruptedException {
		try {
			return (int) getTileDatabase(mapSource).entryCount();
		} catch (DatabaseException e) {
			log.error("", e);
			return -1;
		}
	}

	public long getStoreSize(MapSource tileSource) throws InterruptedException {
		File tileStore = new File(tileStoreDir, tileSource.getName());
		if (tileStore.exists()) {
			DirInfoFileFilter diff = new DirInfoFileFilter();
			try {
				tileStore.listFiles(diff);
			} catch (RuntimeException e) {
				throw new InterruptedException();
			}
			return diff.getDirSize();
		} else {
			return 0;
		}
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

	protected class TileDatabase {

		final MapSource mapSource;
		final Environment env;
		final EntityStore store;
		final PrimaryIndex<TileDbKey, TileDbEntry> tileIndex;
		boolean dbClosed = false;

		public TileDatabase(MapSource mapSource) throws IOException, EnvironmentLockedException,
				DatabaseException {
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

		public boolean isClosed() {
			return dbClosed;
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
			if (dbClosed)
				return;
			try {
				log.debug("Closing tile store db \"" + mapSource.getName() + "\"");
				if (store != null)
					store.close();
			} catch (Exception e) {
				log.error("", e);
			}
			try {
				env.close();
			} catch (Exception e) {
				log.error("", e);
			} finally {
				dbClosed = true;
			}
		}

		@Override
		protected void finalize() throws Throwable {
			close();
			super.finalize();
		}

	}
}
