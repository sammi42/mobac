package tiledb;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import utilities.Utils;

/**
 * Tested version: HSQLDB 1.8.1
 * 
 * Problem: Database loading of DB with ~11000 tiles takes about 9 seconds
 * (database in disk cache)
 * 
 */
public class HsqlTest {

	public static final String JDBC_DRIVER_CLASS = "org.hsqldb.jdbcDriver";

	public static final String JDBC_CONNECTION = "jdbc:hsqldb:file:test-db/hsql/testdb";

	public static final String TILE_DIR = "../TAC/tilestore/Google Maps";

	static Connection conn;

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Driver driver;
		try {
			// File dir = new File("test-db/hsql");
			// for (File f : dir.listFiles())
			// f.delete();
			// dir.delete();
			driver = (Driver) Class.forName(JDBC_DRIVER_CLASS).newInstance();
			DriverManager.registerDriver(driver);

			conn = DriverManager.getConnection(JDBC_CONNECTION);
			conn.setAutoCommit(false);
			// insertTest();
			selectTest();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("END");
		long diff = (end - start) / 1000;
		System.out.println("Time: " + diff + "s");
	}

	static void selectTest() throws Exception {
		PreparedStatement ps = conn.prepareStatement("SELECT data FROM tiles WHERE x=? AND y=? AND zoom=?;");
		// TODO
	}

	static void insertTest() throws Exception {
		Statement s = conn.createStatement();
		s.execute("CREATE TABLE tiles (x int, y int, zoom int, data LONGVARBINARY, " + "PRIMARY KEY (x,y,zoom))");

		PreparedStatement ps = conn.prepareStatement("INSERT INTO tiles (x,y,zoom,data) values (?,?,?,?)");

		File tileDir = new File(TILE_DIR);
		if (!tileDir.isDirectory())
			throw new IOException(tileDir + " does not exist");
		File[] tiles = tileDir.listFiles();
		System.out.println("tiles: " + tiles.length);
		int i = 0;
		long rawSize = 0;
		for (File tf : tiles) {
			String[] n = tf.getName().split("[_.]");
			// for (String ee : n)
			// System.out.println(ee);
			// throw new Exception("split: " + n.length);
			ps.setInt(1, Integer.parseInt(n[0]));
			ps.setInt(2, Integer.parseInt(n[1]));
			ps.setInt(3, Integer.parseInt(n[2]));
			byte[] data = Utils.getFileBytes(tf);
			rawSize += data.length;
			ps.setBytes(4, data);
			ps.addBatch();
			System.out.print('.');
			// System.out.print(data.length);
			if (++i % 100 == 0) {
				ps.executeBatch();
				ps.clearBatch();
				conn.commit();
				System.out.println(" " + i);
			}
		}
		ps.executeBatch();
		ps.clearBatch();
		conn.commit();
		System.out.println("\nRaw tile sizes (bytes): " + rawSize + "\n\n");
	}
}
