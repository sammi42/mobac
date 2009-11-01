package tac.utilities.jdbc;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import tac.program.DirectoryManager;
import tac.utilities.ExtensionClassLoader;

/**
 * Dynamic loading of "SQLite Java Wrapper/JDBC Driver" (BSD-style license)
 * http://www.ch-werner.de/javasqlite/
 */
public class SQLite {

	private static final String DB_DRIVER = "SQLite.JDBCDriver";
	private static boolean SQLITE_LOADED = false;

	public static final String MSG_SQLITE_MISSING = "Unable to find the SQLite libraries. "
			+ "These are required for BigPlanet output format.<br>Please read the README.HTM "
			+ "section \"Creating and using atlases with BigPlanet / RMaps\". ";

	public static boolean loadSQLiteOrShowError() {
		try {
			SQLite.loadSQLite();
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "<html>" + MSG_SQLITE_MISSING + "</html>",
					"Error - SQLite not available", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public static void loadSQLite() throws SQLException {
		if (SQLITE_LOADED)
			return;
		synchronized (SQLite.class) {
			if (SQLITE_LOADED)
				return;
			try {
				Class.forName(DB_DRIVER);
				SQLITE_LOADED = true;
				return;
			} catch (Exception e) {
			}
			ExtensionClassLoader cl;
			File[] dirList = new File[] { DirectoryManager.programDir,
					new File(DirectoryManager.programDir, "lib"), DirectoryManager.currentDir,
					new File(DirectoryManager.currentDir, "lib") };
			try {
				cl = ExtensionClassLoader.create(dirList, "sqlite.*");
			} catch (FileNotFoundException e) {
				throw new SQLException("sqlite.jar and native library not found.");
			}
			try {
				DriverProxy.loadSQLDriver(DB_DRIVER, cl);
			} catch (Exception e) {
				throw new SQLException("Error while loading SQLite driver", e);
			}
			SQLITE_LOADED = true;
			return;
		}
	}
}
