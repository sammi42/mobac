package mobac.program.tilestore.berkeleydb;

import java.io.File;

import mobac.program.Logging;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.BerkeleyDbTileStore.TileDatabase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.sleepycat.persist.EntityCursor;

public class MergeTileUtil {

	public static Logger log;

	public static void merge(File sourceDir, File destDir) {
		BerkeleyDbTileStore tileStore = (BerkeleyDbTileStore) TileStore
				.getInstance();
		TileDatabase dbSource = null;
		TileDatabase dbDest = null;
		try {
			dbSource = tileStore.new TileDatabase("Source", sourceDir);
			log.info("Source tile store entry count: " + dbSource.entryCount());
			dbDest = tileStore.new TileDatabase("Destination", destDir);
			log.info("Destination tile store entry count: "
					+ dbSource.entryCount() + " (before merging)");
			dbDest.purge();
			EntityCursor<TileDbEntry> cursor = dbSource.getTileIndex()
					.entities();
			try {
				TileDbEntry entry = cursor.next();
				while (entry != null) {
					log.trace("Adding " + entry);
					dbDest.put(entry);
					entry = cursor.next();
				}
			} finally {
				cursor.close();
			}
			log.info("Destination tile store entry count: "
					+ dbSource.entryCount() + " (after merging)");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbSource.close(false);
			dbDest.close(false);
		}
	}

	private static void showHelp() {
		System.out.println("MOBAC TileStore utility");
		System.out.println("usage: ");
		System.out
				.println("\tjava -jar ts-util.jar src-directory destination-directory");
		System.out
				.println("\nCopies the tiles from teh tile store in the src directory\n"
						+ "into the tile-store in the destination directory.");
		System.exit(1);
	}

	static File srcDir = null;
	static File destDir = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		boolean parametersValid = false;

		if (args.length == 2) {
			srcDir = new File(args[0]);
			destDir = new File(args[1]);
			parametersValid = srcDir.isDirectory() && destDir.isDirectory();
		}
		if (!parametersValid)
			showHelp();

		Logging.configureConsoleLogging(Level.DEBUG);
		log = Logger.getLogger("TileStoreUtil");
		log.setLevel(Level.TRACE);
		Thread t = new DelayedInterruptThread("Merge") {

			@Override
			public void run() {
				TileStore.initialize();
				merge(srcDir, destDir);
			}

		};
		t.start();
	}
}
