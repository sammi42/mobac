/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2004,2008 Oracle.  All rights reserved.
 *
 * $Id: SimpleExample.java,v 1.51 2008/01/07 14:28:41 cwl Exp $
 */

package tiledb;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import utilities.Utils;

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
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

	static DatabaseConfig dbConfig;
	static EntityStore store;
	static PrimaryIndex<TileKey, TileData> tileIndex;
	
	public static void main(String argv[]) {
		long start = System.currentTimeMillis();
		try {
			envDir.mkdirs();
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setTransactional(true);
			envConfig.setAllowCreate(true);
			Environment env = new Environment(envDir, envConfig);

//	        StoreConfig storeConfig = new StoreConfig();
//	        storeConfig.setAllowCreate(true);
//	        storeConfig.setTransactional(true);
//	        store = new EntityStore(env, "SQLAppStore", storeConfig);
//
//	        tileIndex = store.getPrimaryIndex(TileKey.class, TileData.class);
			
			Transaction txn = env.beginTransaction(null, null);
			dbConfig = new DatabaseConfig();
			dbConfig.setTransactional(true);
			dbConfig.setAllowCreate(true);
			dbConfig.setSortedDuplicates(false);
			Database tileDb = env.openDatabase(txn, "tiles", dbConfig);
			txn.commit();
			insertTest(env, tileDb);
			//selectTest(env, tileDb);
			tileDb.close();
			env.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("END");
		long diff = (end - start) / 1000;
		System.out.println("Time: " + diff + "s");
	}

	static void selectTest(Environment env, Database tileDb) throws Exception {
		Cursor cursor = tileDb.openCursor(null, null);

		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		while (cursor.getNext(keyEntry, dataEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {

			// int key = IntegerBinding.entryToInt(keyEntry);
			// MyData data = dataBinding.entryToObject(dataEntry);

			System.out.println("key=" + keyEntry);
		}
		cursor.close();
	}

	static void insertTest(Environment env, Database tileDb) throws Exception {

		File tileDir = new File(TILE_DIR);
		if (!tileDir.isDirectory())
			throw new IOException(tileDir + " does not exist");
		File[] tiles = tileDir.listFiles();
		System.out.println("tiles: " + tiles.length);
		int i = 0;
		long rawSize = 0;
		dbConfig.setSortedDuplicates(false);
		Database myClassDb = env.openDatabase(null, "classDb", dbConfig);
		// Instantiate the class catalog
		StoredClassCatalog classCatalog = new StoredClassCatalog(myClassDb);
		SerialBinding<TileKey> sb = new SerialBinding<TileKey>(classCatalog, TileKey.class);
		for (File tf : tiles) {
			String[] n = tf.getName().split("[_.]");
			Transaction txn = env.beginTransaction(null, null);
			byte[] data = Utils.getFileBytes(tf);

			int x = Integer.parseInt(n[0]);
			int y = Integer.parseInt(n[1]);
			int zoom = Integer.parseInt(n[2]);

			DatabaseEntry dataEntry = new DatabaseEntry(data);
			DatabaseEntry keyEntry = new DatabaseEntry();

			sb.objectToEntry(new TileKey(x, y, zoom), keyEntry);

			rawSize += data.length;
			System.out.print('.');
			if (++i % 100 == 0) {
				System.out.println(" " + i);
			}
			OperationStatus status = tileDb.put(txn, keyEntry, dataEntry);
			if (status != OperationStatus.SUCCESS) {
				throw new DatabaseException("Data insertion got status " + status);
			}
			txn.commit();
		}
		myClassDb.close();
		System.out.println("\nRaw tile sizes (bytes): " + rawSize + "\n\n");
	}

	public static class TileKey implements Serializable {
		int x;
		int y;
		int zoom;

		public TileKey(int x, int y, int zoom) {
			super();
			this.x = x;
			this.y = y;
			this.zoom = zoom;
		}

	}

	public static class TileData implements Serializable {
		byte[] data;
		String eTag;
		Date lastModified;
		Date downloaded;
	}
}
