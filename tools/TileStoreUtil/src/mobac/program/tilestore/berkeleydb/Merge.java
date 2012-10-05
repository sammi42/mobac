package mobac.program.tilestore.berkeleydb;

import java.io.File;
import java.security.InvalidParameterException;

import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.BerkeleyDbTileStore.TileDatabase;
import mobac.ts_util.Main;
import mobac.ts_util.ParamTests;

import com.sleepycat.persist.EntityCursor;

public class Merge implements Runnable {

	final File sourceDir;
	final File destDir;

	public Merge(String sourceDir, String destDir) {
		this.sourceDir = new File(sourceDir);
		this.destDir = new File(destDir);
		if (!ParamTests.testBerkelyDbDir(this.sourceDir))
			throw new InvalidParameterException();
		if (!ParamTests.testBerkelyDbDir(this.destDir))
			throw new InvalidParameterException();
	}

	public void run() {
		BerkeleyDbTileStore tileStore = (BerkeleyDbTileStore) TileStore.getInstance();
		TileDatabase dbSource = null;
		TileDatabase dbDest = null;
		try {
			dbSource = tileStore.new TileDatabase("Source", sourceDir);
			Main.log.info("Source tile store entry count: " + dbSource.entryCount());
			dbDest = tileStore.new TileDatabase("Destination", destDir);
			Main.log.info("Destination tile store entry count: " + dbSource.entryCount() + " (before merging)");
			dbDest.purge();
			EntityCursor<TileDbEntry> cursor = dbSource.getTileIndex().entities();
			try {
				TileDbEntry entry = cursor.next();
				while (entry != null) {
					Main.log.trace("Adding " + entry);
					dbDest.put(entry);
					entry = cursor.next();
				}
			} finally {
				cursor.close();
			}
			Main.log.info("Destination tile store entry count: " + dbSource.entryCount() + " (after merging)");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbSource.close(false);
			dbDest.close(false);
		}
	}

}
