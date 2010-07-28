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

import java.sql.SQLException;
import java.sql.Statement;

public class SQLitexGPS extends RMapsSQLite {

	private static final String TABLE_MAPS = "CREATE TABLE IF NOT EXISTS maps "
			+ "(id INTEGER, name TEXT, zoom TEXT, type INTEGER, PRIMARY KEY(id));";
	private static final String TABLE_MAPREGIONS = "CREATE TABLE IF NOT EXISTS map_regions "
			+ "(regionsid INTEGER, mapid INTEGER, PRIMARY KEY(regionsid))";
	private static final String TABLE_REGIONS_POINTS = "CREATE TABLE IF NOT EXISTS regions_points "
			+ "(regionsid INTEGER, lat REAL,lon REAL, pos INTEGER, PRIMARY KEY(regionsid,lat,lon))";
	private static final String TABLE_TILES = "CREATE TABLE IF NOT EXISTS tiles "
			+ "(x INTEGER, y INTEGER, zoom INTEGER, type INTEGER, img BLOB, PRIMARY KEY(x,y,zoom,type))";

	private static final String INSERT_SQL = "INSERT or REPLACE INTO tiles "
			+ "(x,y,zoom,type,img) VALUES (?,?,?,?,?)";
	private static final String DATABASE_FILENAME = "xGPS_map.db";

	@Override
	protected void initializeDB() throws SQLException {
		Statement stat = conn.createStatement();
		stat.execute(TABLE_MAPS);
		stat.execute(TABLE_TILES);
		stat.execute(TABLE_MAPREGIONS);
		stat.execute(TABLE_REGIONS_POINTS);
		stat.close();
	}

	
	@Override
	protected void updateTileMetaInfo() throws SQLException {
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
