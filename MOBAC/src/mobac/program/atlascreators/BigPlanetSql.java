package mobac.program.atlascreators;

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

import mobac.exceptions.MapCreationException;
import mobac.mapsources.MultiLayerMapSource;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.Settings;
import mobac.utilities.Utilities;
import mobac.utilities.jdbc.SQLiteLoader;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

/**
 * Atlas/Map creator for "BigPlanet-Maps application for Android" (offline
 * SQLite maps) http://code.google.com/p/bigplanet/
 * <p>
 * Requires "SQLite Java Wrapper/JDBC Driver" (BSD-style license)
 * http://www.ch-werner.de/javasqlite/
 * </p>
 * <p>
 * Some source parts are taken from the "android-map.blogspot.com Version of
 * Mobile Atlas Creator": http://code.google.com/p/android-map/
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
public class BigPlanetSql extends AtlasCreator {

	private static final String TABLE_DDL = "CREATE TABLE IF NOT EXISTS tiles (x int, y int, z int, s int, image blob, PRIMARY KEY (x,y,z,s))";
	private static final String INDEX_DDL = "CREATE INDEX IF NOT EXISTS IND on tiles (x,y,z,s)";
	private static final String INSERT_SQL = "INSERT or IGNORE INTO tiles (x,y,z,s,image) VALUES (?,?,?,?,?)";
	private static final String RMAPS_TABLE_INFO_DDL = "CREATE TABLE IF NOT EXISTS info AS SELECT 99 As minzoom, 0 As maxzoom";
	private static final String RMAPS_CLEAR_INFO_SQL = "DELETE FROM info";
	private static final String RMAPS_UPDATE_INFO_SQL = "INSERT INTO info SELECT MIN(z) as minzoom, MAX(z) as maxzoom FROM tiles";

	private static final String DATABASE_FILENAME = "BigPlanet_maps.sqlitedb";

	private String databaseFile;

	/**
	 * Accumulate tiles in batch process until 5MB of heap are remaining
	 */
	private static final long HEAP_MIN = 5 * 1024 * 1024;

	private Connection conn = null;
	private PreparedStatement prepStmt;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		if (mapSource instanceof MultiLayerMapSource)
			return false;
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas) throws IOException {
		this.atlas = atlas;
		atlasDir = Settings.getInstance().getAtlasOutputDirectory();
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		databaseFile = new File(atlasDir, DATABASE_FILENAME).getAbsolutePath();
		log.debug("SQLite Database file: " + databaseFile);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(atlasDir);
		} catch (IOException e) {
			throw new MapCreationException(e);
		}
		try {
			SQLiteLoader.loadSQLite();
		} catch (SQLException e) {
			throw new MapCreationException(SQLiteLoader.MSG_SQLITE_MISSING, e);
		}
		try {
			initializeDB();
			createTiles();
			conn.close();
		} catch (SQLException e) {
			throw new MapCreationException("Error creating SQL database \"" + databaseFile + "\": "
					+ e.getMessage(), e);
		}
	}

	private Connection getConnection() throws SQLException {
		String url = "jdbc:sqlite:/" + this.databaseFile;
		Connection conn = DriverManager.getConnection(url);
		return conn;
	}

	private void initializeDB() throws SQLException {
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

	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation(2 * (xMax - xMin + 1) * (yMax - yMin + 1));
		try {
			conn.setAutoCommit(false);
			int batchTileCount = 0;
			Runtime r = Runtime.getRuntime();
			long heapMaxSize = r.maxMemory();
			prepStmt = conn.prepareStatement(INSERT_SQL);
			for (int x = xMin; x <= xMax; x++) {
				for (int y = yMin; y <= yMax; y++) {
					checkUserAbort();
					atlasProgress.incMapCreationProgress();
					try {
						byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
						if (sourceTileData != null) {
							writeTile(x, y, zoom, sourceTileData);
							long heapAvailable = heapMaxSize - r.totalMemory() + r.freeMemory();

							batchTileCount++;
							if (heapAvailable < HEAP_MIN) {
								log.trace("Batch commited containing " + batchTileCount + " tiles");
								prepStmt.executeBatch();
								prepStmt.clearBatch();
								atlasProgress.incMapCreationProgress(batchTileCount);
								batchTileCount = 0;
								conn.commit();
								System.gc();
							}
						}
					} catch (IOException e) {
						throw new MapCreationException(e);
					}
				}
			}
			prepStmt.executeBatch();
			conn.commit();
			prepStmt.clearBatch();
			atlasProgress.incMapCreationProgress(batchTileCount);

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