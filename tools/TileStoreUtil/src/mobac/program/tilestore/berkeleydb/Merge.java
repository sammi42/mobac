package mobac.program.tilestore.berkeleydb;

import java.io.File;

import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.BerkeleyDbTileStore.TileDatabase;

import com.sleepycat.persist.EntityCursor;

public class Merge {

	public static void merge(File sourceDir, File destDir) {
		BerkeleyDbTileStore tileStore = (BerkeleyDbTileStore) TileStore
				.getInstance();
		TileDatabase dbSource = null;
		TileDatabase dbDest = null;
		try {
			dbSource = tileStore.new TileDatabase("Source", sourceDir);
			TileStoreUtil.log.info("Source tile store entry count: "
					+ dbSource.entryCount());
			dbDest = tileStore.new TileDatabase("Destination", destDir);
			TileStoreUtil.log.info("Destination tile store entry count: "
					+ dbSource.entryCount() + " (before merging)");
			dbDest.purge();
			EntityCursor<TileDbEntry> cursor = dbSource.getTileIndex()
					.entities();
			try {
				TileDbEntry entry = cursor.next();
				while (entry != null) {
					TileStoreUtil.log.trace("Adding " + entry);
					dbDest.put(entry);
					entry = cursor.next();
				}
			} finally {
				cursor.close();
			}
			TileStoreUtil.log.info("Destination tile store entry count: "
					+ dbSource.entryCount() + " (after merging)");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbSource.close(false);
			dbDest.close(false);
		}
	}

}
