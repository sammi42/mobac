/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.program.atlascreators;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.Settings;
import mobac.utilities.Utilities;
import mobac.utilities.jdbc.SQLiteLoader;

/**
 * Atlas/Map creator for "BigPlanet-Maps application for Android" (offline SQLite maps)
 * http://code.google.com/p/bigplanet/
 * <p>
 * Requires "SQLite Java Wrapper/JDBC Driver" (BSD-style license) http://www.ch-werner.de/javasqlite/
 * </p>
 * <p>
 * Some source parts are taken from the "android-map.blogspot.com Version of Mobile Atlas Creator":
 * http://code.google.com/p/android-map/
 * </p>
 * <p>
 * Additionally the created BigPlanet SQLite database has one additional table containing special info needed by the
 * Android application <a href="http://robertdeveloper.blogspot.com/search/label/rmaps.release" >RMaps</a>.<br>
 * (Database statements: {@link #RMAPS_TABLE_INFO_DDL} and {@link #RMAPS_UPDATE_INFO_SQL} ).<br>
 * Changes made by <a href="mailto:robertk506@gmail.com">Robert</a>, author of RMaps.
 * <p>
 */
public class RMapsSQLite extends AtlasCreator implements RequiresSQLite {

	private static final String TABLE_DDL = "CREATE TABLE IF NOT EXISTS tiles (x int, y int, z int, s int, image blob, PRIMARY KEY (x,y,z,s))";
	private static final String INDEX_DDL = "CREATE INDEX IF NOT EXISTS IND on tiles (x,y,z,s)";
	private static final String INSERT_SQL = "INSERT or IGNORE INTO tiles (x,y,z,s,image) VALUES (?,?,?,?,?)";
	private static final String RMAPS_TABLE_INFO_DDL = "CREATE TABLE IF NOT EXISTS info AS SELECT 99 AS minzoom, 0 AS maxzoom";
	private static final String RMAPS_CLEAR_INFO_SQL = "DELETE FROM info;";
	private static final String RMAPS_UPDATE_INFO_SQL = "INSERT INTO info SELECT MIN(z) as minzoom, MAX(z) as maxzoom FROM tiles;";

	private String databaseFile;

	protected Connection conn = null;
	private PreparedStatement prepStmt;

	public RMapsSQLite() {
		super();
		SQLiteLoader.loadSQLiteOrShowError();
	}

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws IOException, AtlasTestException,
			InterruptedException {
		if (customAtlasDir == null)
			customAtlasDir = Settings.getInstance().getAtlasOutputDirectory();
		super.startAtlasCreation(atlas, customAtlasDir);
		databaseFile = new File(atlasDir, getDatabaseFileName()).getAbsolutePath();
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
			openConnection();
			initializeDB();
			createTiles();
		} catch (SQLException e) {
			throw new MapCreationException("Error creating SQL database \"" + databaseFile + "\": " + e.getMessage(), e);
		}
	}

	private void openConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			String url = "jdbc:sqlite:/" + this.databaseFile;
			conn = DriverManager.getConnection(url);
		}
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		SQLiteLoader.closeConnection(conn);
		conn = null;
		super.abortAtlasCreation();
	}

	@Override
	public void finishAtlasCreation() throws IOException, InterruptedException {
		SQLiteLoader.closeConnection(conn);
		conn = null;
		super.finishAtlasCreation();
	}

	protected void initializeDB() throws SQLException {
		Statement stat = conn.createStatement();
		stat.executeUpdate(TABLE_DDL);
		stat.executeUpdate(INDEX_DDL);
		stat.executeUpdate(RMAPS_TABLE_INFO_DDL);

		stat.executeUpdate("CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)");
		if (!(stat.executeQuery("SELECT * FROM android_metadata").next())) {
			String locale = Locale.getDefault().toString();
			stat.executeUpdate("INSERT INTO android_metadata VALUES ('" + locale + "')");
		}
		stat.close();
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		int maxMapProgress = 2 * (xMax - xMin + 1) * (yMax - yMin + 1);
		atlasProgress.initMapCreation(maxMapProgress);
		try {
			conn.setAutoCommit(false);
			int batchTileCount = 0;
			Runtime r = Runtime.getRuntime();
			long heapMaxSize = r.maxMemory();
			prepStmt = conn.prepareStatement(getTileInsertSQL());
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
								log.trace("Executing batch containing " + batchTileCount + " tiles");
								prepStmt.executeBatch();
								prepStmt.clearBatch();
								System.gc();
								batchTileCount = 0;
								conn.commit();
								atlasProgress.incMapCreationProgress(batchTileCount);
							}
						}
					} catch (IOException e) {
						throw new MapCreationException(e);
					}
				}
			}
			prepStmt.executeBatch();
			prepStmt.clearBatch();
			System.gc();
			updateTileMetaInfo();
			log.trace("Final commit");
			conn.commit();
			atlasProgress.setMapCreationProgress(maxMapProgress);
		} catch (SQLException e) {
			throw new MapCreationException(e);
		}
	}

	protected void updateTileMetaInfo() throws SQLException {
		Statement stat = conn.createStatement();
		stat.addBatch(RMAPS_CLEAR_INFO_SQL);
		stat.addBatch(RMAPS_UPDATE_INFO_SQL);
		stat.executeBatch();
		stat.close();
	}

	protected void writeTile(int x, int y, int z, byte[] tileData) throws SQLException, IOException {
		int s = 0;
		prepStmt.setInt(1, x);
		prepStmt.setInt(2, y);
		prepStmt.setInt(3, 17 - z);
		prepStmt.setInt(4, s);
		prepStmt.setBytes(5, tileData);
		prepStmt.addBatch();
	}

	protected String getDatabaseFileName() {
		return "Custom " + atlas.getName() + ".sqlitedb";
	}

	protected String getTileInsertSQL() {
		return INSERT_SQL;
	}

}
