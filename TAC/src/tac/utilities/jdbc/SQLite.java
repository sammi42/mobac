package tac.utilities.jdbc;

import java.io.File;

import org.apache.log4j.Logger;

import tac.program.DirectoryManager;
import tac.program.mapcreators.MapCreatorBigPlanet;
import tac.utilities.ExtensionClassLoader;

/**
 * Dynamic loading of "SQLite Java Wrapper/JDBC Driver" (BSD-style license)
 * http://www.ch-werner.de/javasqlite/
 */
public class SQLite {

	private static final String DB_DRIVER = "SQLite.JDBCDriver";
	private static boolean SQLITE_LOADED = false;

	private static final Logger log = Logger.getLogger(SQLite.class);

	public static boolean loadSQLite() {
		if (SQLITE_LOADED)
			return true;
		try {
			synchronized (MapCreatorBigPlanet.class) {
				if (SQLITE_LOADED)
					return true;
				try {
					Class.forName(DB_DRIVER);
					SQLITE_LOADED = true;
					return true;
				} catch (Exception e) {
				}
				ExtensionClassLoader cl;
				File[] dirList = new File[] { DirectoryManager.programDir,
						new File(DirectoryManager.programDir, "lib"), DirectoryManager.currentDir,
						new File(DirectoryManager.currentDir, "lib") };
				cl = new ExtensionClassLoader(dirList, "sqlite.*");
				DriverProxy.loadSQLDriver("SQLite.JDBCDriver", cl);
				SQLITE_LOADED = true;
				return true;
			}
		} catch (Exception e) {
			log.error("SQLite loading failed", e);
			return false;
		}
	}
}
