package tac.utilities.jdbc;

import java.io.File;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import tac.program.DirectoryManager;
import SQLite.Database;

/**
 * Dynamic loading of "SQLite Java Wrapper/JDBC Driver" (BSD-style license)
 * http://www.ch-werner.de/javasqlite/
 */
public class SQLiteLoader {

	private static final String CLASS_DB_DRIVER = "SQLite.JDBCDriver";
	private static boolean SQLITE_LOADED = false;

	private static Database database;

	public static final String MSG_SQLITE_MISSING = "Unable to find the SQLite libraries. "
			+ "These are required for BigPlanet output format.<br>Please read the README.HTM "
			+ "section \"Creating and using atlases with BigPlanet / RMaps\". ";

	public static boolean loadSQLiteOrShowError() {
		try {
			SQLiteLoader.loadSQLite();
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "<html>" + MSG_SQLITE_MISSING + "</html>",
					"Error - SQLite not available", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static void loadSQLite() throws SQLException {
		if (SQLITE_LOADED)
			return;
		synchronized (SQLiteLoader.class) {
			if (SQLITE_LOADED)
				return;
			try {
				// Load the main class which loads the native library
				database = new SQLite.Database();
				Class<Driver> jdbcDriverClass = (Class<Driver>) Class.forName(CLASS_DB_DRIVER);
				DriverManager.registerDriver(jdbcDriverClass.newInstance());
				SQLITE_LOADED = true;
				return;
			} catch (Throwable t) {
				// t.printStackTrace();
			}
			File pd = DirectoryManager.programDir;
			File cd = DirectoryManager.currentDir;
			File[] dirList = new File[] { pd, new File(pd, "lib"), cd, new File(cd, "lib") };

			File verifiedLibDir = null;
			String libName = System.mapLibraryName("sqlite_jni");
			for (File libDir : dirList) {
				File lib = new File(libDir, libName);
				if (lib.isFile()) {
					verifiedLibDir = libDir;
					break;
				}
			}
			if (verifiedLibDir == null)
				throw new SQLException("Native SQLite file not found: " + libName);
			System.setProperty("SQLite.library.path", verifiedLibDir.getPath());

			try {
				// Load the main class which loads the native library
				database = new SQLite.Database();
				Class<Driver> jdbcDriverClass = (Class<Driver>) Class.forName(CLASS_DB_DRIVER);
				DriverManager.registerDriver(jdbcDriverClass.newInstance());
				SQLITE_LOADED = true;
				return;
			} catch (Throwable t) {
				throw new SQLException(t);
			}
		}
	}

	public static String getDatabaseVersion() {
		return database.dbversion();
	}
}
