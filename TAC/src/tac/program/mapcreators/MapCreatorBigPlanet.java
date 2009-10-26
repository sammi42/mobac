package tac.program.mapcreators;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import tac.exceptions.MapCreationException;
import tac.gui.AtlasProgress;
import tac.program.AtlasThread;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;
import tac.utilities.jdbc.SQLite;

/**
 * Atlas/Map creator for "BigPlanet-Maps application for Android" (offline
 * SQLite maps) http://code.google.com/p/bigplanet/
 * <p>
 * Requires "SQLite Java Wrapper/JDBC Driver" (BSD-style license)
 * http://www.ch-werner.de/javasqlite/
 * </p>
 * <p>
 * Some source parts are taken from the "android-map.blogspot.com Version of
 * TrekBuddy Atlas Creator": http://code.google.com/p/android-map/
 * </p>
 * <p>
 * Additionally the created BigPlanet SQLite database has one additional table
 * containing special info needed by the Android application <a
 * href="http://robertdeveloper.blogspot.com/search/label/rmaps.release"
 * >RMaps</a>.<br>
 * (Database statements: {@link #RMAPS_TABLE_INFO_DDL} and
 * {@link #RMAPS_UPDATE_INFO_SQL} ).<br>
 * Changes made by <a href="mailto:robertk506@gmail.com">Robert</a>, author of
 * RMaps.
 * <p>
 */
public class MapCreatorBigPlanet extends MapCreator {

	private static final String TABLE_DDL = "CREATE TABLE IF NOT EXISTS tiles (x int, y int, z int, s int, image blob, PRIMARY KEY (x,y,z,s))";
	private static final String INDEX_DDL = "CREATE INDEX IF NOT EXISTS IND on tiles (x,y,z,s)";
	private static final String INSERT_SQL = "INSERT or IGNORE INTO tiles (x,y,z,s,image) VALUES (?,?,?,?,?)";
	private static final String RMAPS_TABLE_INFO_DDL = "CREATE TABLE IF NOT EXISTS info AS SELECT 99 As minzoom, 0 As maxzoom";
	private static final String RMAPS_CLEAR_INFO_SQL = "DELETE FROM info";
	private static final String RMAPS_UPDATE_INFO_SQL = "INSERT INTO info SELECT MIN(z) as minzoom, MAX(z) as maxzoom FROM tiles";

	private static final String DATABASE_FILENAME = "BigPlanet_maps.sqlitedb";

	/**
	 * Commit only every i tiles
	 */
	private static final int COMMIT_RATE = 1000;

	private String databaseFile;

	private Connection conn = null;
	private PreparedStatement prepStmt;

	public MapCreatorBigPlanet(MapInterface map, TarIndex tarTileIndex, File atlasDir) {
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
			conn.close();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		} catch (ClassNotFoundException e) {
			throw new MapCreationException("SQLite Java driver not available", e);
		} catch (SQLException e) {
			throw new MapCreationException("Error creating SQL database", e);
		}
	}

	private Connection getConnection() throws ClassNotFoundException, SQLException {
		SQLite.loadSQLite();
		String url = "jdbc:sqlite:/" + this.databaseFile;
		Connection conn = DriverManager.getConnection(url);
		return conn;
	}

	private void initializeDB() throws ClassNotFoundException, SQLException {
		conn = getConnection();
		Statement stat = conn.createStatement();
		stat.executeUpdate(TABLE_DDL);
		stat.executeUpdate(INDEX_DDL);
		stat.executeUpdate(RMAPS_TABLE_INFO_DDL);

		stat.executeUpdate("CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)");
		if (!(stat.executeQuery("SELECT * FROM android_metadata").first())) {
			String locale = Locale.getDefault().toString();
			stat.executeUpdate("INSERT INTO android_metadata VALUES ('" + locale + "')");
		}
		stat.close();
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
			conn.setAutoCommit(false);
			int tileCount = 0;
			prepStmt = conn.prepareStatement(INSERT_SQL);
			for (int x = xMin; x <= xMax; x++) {
				for (int y = yMin; y <= yMax; y++) {
					if (t.isInterrupted())
						throw new InterruptedException();
					if (ap != null)
						ap.incMapCreationProgress();
					try {
						byte[] sourceTileData = mapDlTileProcessor.getTileData(x, y);
						if (sourceTileData != null) {
							writeTile(x, y, zoom, sourceTileData);
							if (++tileCount % COMMIT_RATE == 0) {
								prepStmt.executeBatch();
								conn.commit();
								prepStmt.clearBatch();
							}
						}
					} catch (IOException e) {
						log.error("", e);
					}
				}
			}
			prepStmt.executeBatch();
			conn.commit();
			prepStmt.clearBatch();

			Statement stat = conn.createStatement();
			stat.addBatch(RMAPS_CLEAR_INFO_SQL);
			stat.addBatch(RMAPS_UPDATE_INFO_SQL);
			stat.executeBatch();
			stat.close();
			conn.commit();
		} catch (SQLException e) {
			log.error("", e);
		}
	}

	private void writeTile(int x, int y, int z, byte[] tileData) throws SQLException, IOException {
		InputStream is = new ByteArrayInputStream(tileData);
		int s = 0;
		prepStmt.setInt(1, x);
		prepStmt.setInt(2, y);
		prepStmt.setInt(3, 17 - z);
		prepStmt.setInt(4, s);
		prepStmt.setBinaryStream(5, is, is.available());
		prepStmt.addBatch();
		is.close();
	}

}