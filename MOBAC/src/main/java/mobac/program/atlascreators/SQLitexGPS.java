package mobac.program.atlascreators;

import java.sql.SQLException;
import java.sql.Statement;

public class SQLitexGPS extends BigPlanetSql {

	private static final String TABLE_MAPS = "CREATE TABLE maps "
			+ "(id INTEGER, name TEXT, zoom TEXT, type INTEGER, PRIMARY KEY(id));";
	private static final String TABLE_MAPREGIONS = "CREATE TABLE map_regions "
			+ "(regionsid INTEGER, mapid INTEGER, PRIMARY KEY(regionsid))";
	private static final String TABLE_REGIONS_POINTS = "CREATE TABLE regions_points "
			+ "(regionsid INTEGER, lat REAL,lon REAL, pos INTEGER, PRIMARY KEY(regionsid,lat,lon))";
	private static final String TABLE_TILES = "CREATE TABLE tiles "
			+ "(x INTEGER, y INTEGER,zoom INTEGER,type INTEGER, img BLOB, PRIMARY KEY(x,y,zoom,type))";

	private static final String INSERT_SQL = "INSERT or REPLACE INTO tiles "
			+ "(x,y,zoom,type,img) VALUES (?,?,?,?,?)";
	private static final String DATABASE_FILENAME = "xGPS_map.db";

	@Override
	protected void initializeDB() throws SQLException {
		Statement stat = conn.createStatement();
		stat.executeUpdate(TABLE_MAPS);
		stat.executeUpdate(TABLE_TILES);
		stat.executeUpdate(TABLE_MAPREGIONS);
		stat.executeUpdate(TABLE_REGIONS_POINTS);
		stat.close();
	}

	@Override
	protected String getDatabaseFileName() {
		return DATABASE_FILENAME;
	}

	@Override
	protected String getTileInsertSQL() {
		return INSERT_SQL;
	}

}
