/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2004,2008 Oracle.  All rights reserved.
 *
 * $Id: SimpleExample.java,v 1.51 2008/01/07 14:28:41 cwl Exp $
 */

package tiledb.berkeley;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import tiledb.berkeley.TileDbEntry.TileDbKey;
import utilities.Utils;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

/**
 * SimpleExample creates a database environment, a database, and a database
 * cursor, inserts and retrieves data.
 */
class BerkelyDBTest {

	static File envDir = new File("test-db/BerkelyDB");

	public static final String TILE_DIR = "../TAC/tilestore/Google Maps";

	static EntityStore store;
	static PrimaryIndex<TileDbKey, TileDbEntry> tileIndex;

	public static void main(String argv[]) {
		long start = System.currentTimeMillis();
		try {
			// Thread.sleep(2000);
			envDir.mkdirs();
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setTransactional(true);
			envConfig.setAllowCreate(true);
			Environment env = new Environment(envDir, envConfig);

			StoreConfig storeConfig = new StoreConfig();
			storeConfig.setAllowCreate(true);
			storeConfig.setTransactional(true);
			store = new EntityStore(env, "TilesEntityStore", storeConfig);

			tileIndex = store.getPrimaryIndex(TileDbKey.class, TileDbEntry.class);

			//insertTest(env);
			truncateTest(env);
			// selectTest(env);
			// deleteTest(env);

			long cstart = System.currentTimeMillis();
			env.cleanLog();
			env.compress();
			long cend = System.currentTimeMillis();
			System.out.println("Time: " + (cend - cstart) + "ms");
			store.close();
			env.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("END");
		long diff = (end - start) / 1000;
		System.out.println("Time: " + diff + "s");
	}

	static void selectTest(Environment env) throws Exception {
		System.out.println("\nTiles in db: " + tileIndex.count());

		TileDbEntry td = tileIndex.get(new TileDbKey(17, 68663, 44566));
		System.out.println(td.eTag);
		System.out.println(td.data.length + " bytes");
		System.out.println(new Date(td.timeDownloaded));
		Thread.sleep(2000);

	}

	static void truncateTest(Environment env) throws Exception {
		Transaction txn = env.beginTransaction(null, null);
		EntityCursor<TileDbEntry> cursor = null;
		try {
			cursor = tileIndex.entities(txn, CursorConfig.DEFAULT);
			TileDbEntry td = cursor.first();
			while (td != null) {
				cursor.delete();
				td = cursor.next();
			}
		} catch (DatabaseException e) {
			txn.abort();
			throw e;
		} finally {
			if (cursor != null)
				cursor.close();
			txn.commit();
			env.sync();
			env.cleanLog();
			env.compress();
		}
	}

	static void deleteTest(Environment env) throws Exception {

		Transaction txn = env.beginTransaction(null, null);

		EntityCursor<TileDbEntry> cursor = tileIndex.entities(txn, CursorConfig.DEFAULT);
		try {
			TileDbEntry td = cursor.first();
			int x = 0;
			while (td != null) {
				if (++x % 3 == 0)
					cursor.delete();
				td = cursor.next();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		txn.commit();
	}

	static void insertTest(Environment env) throws Exception {

		File tileDir = new File(TILE_DIR);
		if (!tileDir.isDirectory())
			throw new IOException(tileDir + " does not exist");
		File[] tiles = tileDir.listFiles();
		System.out.println("tiles: " + tiles.length);
		int i = 0;
		long rawSize = 0;

		for (File tf : tiles) {
			String[] n = tf.getName().split("[_.]");
			Transaction txn = env.beginTransaction(null, null);
			byte[] data = Utils.getFileBytes(tf);

			int zoom = Integer.parseInt(n[0]);
			int x = Integer.parseInt(n[1]);
			int y = Integer.parseInt(n[2]);

			TileDbEntry td = new TileDbEntry();
			td.tileKey = new TileDbKey(zoom, x, y);
			td.data = data;
			td.timeDownloaded = System.currentTimeMillis();

			tileIndex.put(txn, td);

			rawSize += data.length;
			System.out.print('.');
			if (++i % 100 == 0) {
				System.out.println(" " + i);
			}
			txn.commit();
		}
		System.out.println("\nTiles in db: " + tileIndex.count());
		System.out.println("\nRaw tile sizes (bytes): " + rawSize + "\n\n");
	}

}
