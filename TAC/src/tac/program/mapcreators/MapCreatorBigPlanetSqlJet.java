package tac.program.mapcreators;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Locale;

import org.tmatesoft.sqljet.core.SqlJetErrorCode;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.internal.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import tac.exceptions.MapCreationException;
import tac.gui.AtlasProgress;
import tac.program.AtlasThread;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;

/**
 * Uses SQLJet (Java only SQLite-DB implementation, does not require native
 * library).
 * 
 * Status: SQLJet has some bugs - if those are fixed it may be an alternative.
 */
public class MapCreatorBigPlanetSqlJet extends MapCreator {

	private static final String TABLE_DDL = "CREATE TABLE IF NOT EXISTS tiles (x int, y int, z int, s int, image blob, PRIMARY KEY (x,y,z,s))";
	private static final String INDEX_DDL = "CREATE INDEX IF NOT EXISTS IND on tiles (x,y,z,s)";
	private static final String TABLE_ANDROID = "CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)";
	private static final String TABLE_RMAPS_INFO = "CREATE TABLE IF NOT EXISTS info (minzoom int, maxzoom int)";
	// private static final String RMAPS_UPDATE_INFO_SQL =
	// "DELETE FROM info; INSERT INTO info SELECT MIN(z), MAX(z) FROM tiles;";

	private static final String DATABASE_FILENAME = "BigPlanet_maps.sqlitedb";

	/**
	 * Commit only every i tiles
	 */
	private static final int COMMIT_RATE = 1000;

	private String databaseFile;

	private SqlJetDb db;

	public MapCreatorBigPlanetSqlJet(MapInterface map, TarIndex tarTileIndex, File atlasDir) {
		super(map, tarTileIndex, atlasDir);
		atlasDir.delete(); // We don't use the atlas directory
		databaseFile = new File(atlasDir.getParent(), DATABASE_FILENAME).getAbsolutePath();
	}

	@Override
	public void createMap() throws MapCreationException {
		try {
			mapTileWriter = null;
			initializeDB();
			createTiles();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		} catch (ClassNotFoundException e) {
			throw new MapCreationException("SQLite Java driver not available", e);
		} catch (SqlJetException e) {
			throw new MapCreationException("Error creating SQL database", e);
		} finally {
			try {
				db.close();
			} catch (Exception e) {
				log.error("", e);
			}
			db = null;
		}
	}

	private SqlJetDb getConnection() throws ClassNotFoundException, SqlJetException {
		File dbFile = new File(databaseFile);
		boolean exists = dbFile.isFile();

		// create database, table and two indices:
		SqlJetDb db = SqlJetDb.open(dbFile, true);
		if (!exists) {
			// set DB option that have to be set before running any
			// transactions:
			db.getOptions().setAutovacuum(true);
			// set DB option that have to be set in a transaction:
			db.runTransaction(new ISqlJetTransaction() {
				public Object run(SqlJetDb db) throws SqlJetException {
					db.getOptions().setUserVersion(1);
					return true;
				}
			}, SqlJetTransactionMode.WRITE);
		}
		return db;
	}

	private void initializeDB() throws ClassNotFoundException, SqlJetException {
		db = getConnection();
		db.beginTransaction(SqlJetTransactionMode.WRITE);
		try {
			db.createTable(TABLE_DDL);
			db.createIndex(INDEX_DDL);
			db.createTable(TABLE_RMAPS_INFO);
			db.createTable(TABLE_ANDROID);
		} finally {
			db.commit();
		}
		ISqlJetTable table = db.getTable("android_metadata");
		table.insert(Locale.getDefault().toString());
	}

	@Override
	protected void createTiles() throws InterruptedException {

		Thread t = Thread.currentThread();
		AtlasProgress ap = null;
		if (t instanceof AtlasThread) {
			ap = ((AtlasThread) t).getAtlasProgress();
			ap.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		}
		try {
			SqlJetInsertOrUpdateTile statement = new SqlJetInsertOrUpdateTile(db, "tiles");
			for (int x = xMin; x <= xMax; x++) {
				for (int y = yMin; y <= yMax; y++) {
					if (t.isInterrupted())
						throw new InterruptedException();
					if (ap != null)
						ap.incMapCreationProgress();
					try {
						byte[] sourceTileData = mapDlTileProcessor.getTileData(x, y);
						if (sourceTileData != null)
							statement.addTile(x, y, zoom, 0, sourceTileData);
					} catch (IOException e) {
						log.error("", e);
					}
				}
			}
			db.commit();

			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			ISqlJetTable tbInfo = db.getTable("info");
			ISqlJetTable tbTiles = db.getTable("info");
			int z_min = 99;
			int z_max = 0;

			ISqlJetCursor allTiles = tbTiles.open();
			if (!allTiles.eof()) {
				do {
					int z = (int) allTiles.getInteger(2);
					log.info(allTiles.getInteger(0) + " " + allTiles.getInteger(1) + " "
							+ allTiles.getInteger(2));
					z_min = Math.min(z_min, z);
					z_max = Math.max(z_max, z);

				} while (allTiles.next());
			}
			allTiles.close();
			db.commit();
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			tbInfo.clear();
			tbInfo.insert(z_min, z_max);
			db.commit();
		} catch (SqlJetException e) {
			log.error("", e);
		}
	}

	/**
	 * Reusable transaction for inserting/updating a tile entry
	 */
	public static class SqlJetInsertOrUpdateTile {

		final SqlJetDb db;
		final ISqlJetTable table;

		Object[] data; // Array containing the data columns

		private int counter = 0;

		public SqlJetInsertOrUpdateTile(SqlJetDb db, String tableName) throws SqlJetException {
			this.db = db;
			this.table = db.getTable(tableName);
			db.beginTransaction(SqlJetTransactionMode.WRITE);
		}

		public void addTile(int x, int y, int zoom, int s, byte[] tileData) throws SqlJetException {
			this.data = new Object[] { x, y, zoom, s, tileData };
			try {
				table.insert(data);
			} catch (SqlJetException e) {
				if (SqlJetErrorCode.CONSTRAINT.equals(e.getErrorCode())) {
					Object[] key = new Object[] { data[0], data[1], data[2], data[3] };
					ISqlJetCursor updateCursor = table.lookup("IND", key);
					do {
						updateCursor.update(data);
					} while (updateCursor.next());
					updateCursor.close();
				} else
					throw e;
			}
			if (++counter % COMMIT_RATE == 0) {
				db.commit();
				db.beginTransaction(SqlJetTransactionMode.WRITE);
			}
		}
	}

	/**
	 * Standalone test
	 */
	public static void main(String[] args) throws SqlJetException {
		try {
			File dbFile = new File("D:/test/test.db");
			if (!dbFile.delete())
				throw new IOException("Unable to delete database");
			SqlJetDb db = SqlJetDb.open(dbFile, true);
			db.getOptions().setAutovacuum(true);
			db.runTransaction(new ISqlJetTransaction() {
				public Object run(SqlJetDb db) throws SqlJetException {
					db.getOptions().setUserVersion(1);
					return true;
				}
			}, SqlJetTransactionMode.WRITE);
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			db.createTable(TABLE_DDL);
			db.createIndex(INDEX_DDL);
			db.createTable(TABLE_RMAPS_INFO);
			db.createTable(TABLE_ANDROID);
			// private static final String TABLE_RMAPS_INFO_INIT =
			// "INSERT SELECT 99 As minzoom, 0 As maxzoom";

			db.commit();

			SqlJetInsertOrUpdateTile iou = new SqlJetInsertOrUpdateTile(db, "tiles");
			SecureRandom rnd = new SecureRandom();
			for (int i = 0; i < 10000; i++) {
				byte[] blob = new byte[1024 + rnd.nextInt(4096)];
				rnd.nextBytes(blob);
				byte[] text = ("LOOP" + i).getBytes("ASCII");
				System.arraycopy(text, 0, blob, 0, text.length);
				iou.addTile(rnd.nextInt(2048), 1, 1, 0, blob);
			}
			db.commit();

			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			ISqlJetTable tbInfo = db.getTable("info");
			ISqlJetTable tbTiles = db.getTable("info");
			int z_min = 99;
			int z_max = 0;

			ISqlJetCursor allTiles = tbTiles.open();
			if (!allTiles.eof()) {
				do {
					int z = (int) allTiles.getInteger(2);
					z_min = Math.min(z_min, z);
					z_max = Math.max(z_max, z);

				} while (allTiles.next());
			}
			allTiles.close();
			db.commit();
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			tbInfo.clear();
			tbInfo.insert(z_min, z_max);
			db.commit();

			db.close();
		} catch (SqlJetException e) {
			System.err.println(e.getErrorCode());
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("END");
	}
}