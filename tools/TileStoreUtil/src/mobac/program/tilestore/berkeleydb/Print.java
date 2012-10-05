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
package mobac.program.tilestore.berkeleydb;

import java.io.File;
import java.security.InvalidParameterException;

import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.BerkeleyDbTileStore.TileDatabase;
import mobac.ts_util.Main;
import mobac.ts_util.ParamTests;

import com.sleepycat.persist.EntityCursor;

public class Print implements Runnable {

	final File dbDir;

	public Print(String dbDir) {
		this.dbDir = new File(dbDir);
		if (!ParamTests.testBerkelyDbDir(this.dbDir))
			throw new InvalidParameterException();
	}

	@Override
	public void run() {
		BerkeleyDbTileStore tileStore = (BerkeleyDbTileStore) TileStore.getInstance();
		TileDatabase db = null;
		try {
			db = tileStore.new TileDatabase("Db", dbDir);
			EntityCursor<TileDbEntry> cursor = db.getTileIndex().entities();
			try {
				TileDbEntry entry = cursor.next();
				while (entry != null) {
					System.out.println(entry);
					entry = cursor.next();
				}
			} finally {
				cursor.close();
			}
			System.out.println("Tile store entry count: " + db.entryCount());
		} catch (Exception e) {
			Main.log.error("Deleting of tiles failed", e);
		} finally {
			db.close(false);
		}
	}

}
