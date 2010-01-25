package SQLite;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCDriver implements java.sql.Driver {

	public static final int MAJORVERSION = 1;

	public static boolean sharedCache = false;

	public static String vfs = null;

	protected Connection conn;

	static {
		try {
			init();
		} catch (java.lang.Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void init() throws Exception {
		java.sql.DriverManager.registerDriver(new JDBCDriver());
		try {
			String shcache = System.getProperty("SQLite.sharedcache");
			if (shcache != null
					&& (shcache.startsWith("y") || shcache.startsWith("Y"))) {
				sharedCache = SQLite.Database._enable_shared_cache(true);
			}
		} catch (java.lang.Exception e) {
		}
		try {
			String tvfs = System.getProperty("SQLite.vfs");
			if (tvfs != null) {
				vfs = tvfs;
			}
		} catch (java.lang.Exception e) {
		}
	}

	public JDBCDriver() {
	}

	public boolean acceptsURL(String url) throws SQLException {
		return url.startsWith("sqlite:/") || url.startsWith("jdbc:sqlite:/");
	}

	public Connection connect(String url, Properties info) throws SQLException {
		if (!acceptsURL(url)) {
			return null;
		}
		try {
			String args[] = new String[5];
			args[0] = url;
			if (info != null) {
				args[1] = info.getProperty("encoding");
				args[2] = info.getProperty("password");
				args[3] = info.getProperty("daterepr");
				args[4] = info.getProperty("vfs");
			}
			if (args[1] == null) {
				args[1] = java.lang.System.getProperty("SQLite.encoding");
			}
			if (args[4] == null) {
				args[4] = vfs;
			}
			conn = new SQLite.JDBC2z.JDBCConnection(args[0], args[1], args[2],
					args[3], args[4]);
		} catch (Exception e) {
			throw new SQLException(e.toString(), e);
		}
		return conn;
	}

	public int getMajorVersion() {
		return MAJORVERSION;
	}

	public int getMinorVersion() {
		return Constants.drv_minor;
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		DriverPropertyInfo p[] = new DriverPropertyInfo[4];
		DriverPropertyInfo pp = new DriverPropertyInfo("encoding", "");
		p[0] = pp;
		pp = new DriverPropertyInfo("password", "");
		p[1] = pp;
		pp = new DriverPropertyInfo("daterepr", "normal");
		p[2] = pp;
		pp = new DriverPropertyInfo("vfs", vfs);
		p[3] = pp;
		return p;
	}

	public boolean jdbcCompliant() {
		return false;
	}
}
