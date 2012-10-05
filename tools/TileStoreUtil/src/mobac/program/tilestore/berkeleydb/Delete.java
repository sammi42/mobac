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

public class Delete implements Runnable {

	final String condition;
	final File dbDir;

	public Delete(String dbDir, String condition) {
		this.condition = condition;
		this.dbDir = new File(dbDir);
		if (!ParamTests.testBerkelyDbDir(this.dbDir))
			throw new InvalidParameterException();
	}

	@Override
	public void run() {

		String eTagValue = "";
		String[] conditionSplit = condition.split(":");
		if ("etag".equalsIgnoreCase(conditionSplit[0])) {
			eTagValue = conditionSplit[1].trim();
			eTagValue = "\"" + eTagValue + "\"";
			System.out.println("Deleting all tiles with an etag of " + eTagValue);
		} else {
			System.err.println("Invalid condition: " + condition);
			System.exit(-1);
		}

		BerkeleyDbTileStore tileStore = (BerkeleyDbTileStore) TileStore.getInstance();
		TileDatabase db = null;
		try {
			db = tileStore.new TileDatabase("Db", dbDir);
			Main.log.info("Tile store entry count: " + db.entryCount() + " (before deleting)");
			EntityCursor<TileDbEntry> cursor = db.getTileIndex().entities();
			try {
				TileDbEntry entry = cursor.next();
				while (entry != null) {
					if (entry.geteTag().equals(eTagValue)) {
						Main.log.trace("Deleting " + entry);
						cursor.delete();
					}
					entry = cursor.next();
				}
			} finally {
				cursor.close();
			}
			db.purge();
			Main.log.info("Tile store entry count: " + db.entryCount() + " (after deleting)");
		} catch (Exception e) {
			Main.log.error("Deleting of tiles failed", e);
		} finally {
			db.close(false);
		}
	}

}
