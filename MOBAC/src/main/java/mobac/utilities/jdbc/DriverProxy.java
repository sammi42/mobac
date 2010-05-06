package mobac.utilities.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Proxies all calls to {@link Driver} that has been loaded using a custom
 * {@link ClassLoader}. This is necessary as the SQL {@link DriverManager} does
 * only accept drivers loaded by the <code>SystemClassLoader</code>.
 */
public class DriverProxy implements Driver {

	private static Logger log = Logger.getLogger(DriverProxy.class);

	private final Driver driver;

	@SuppressWarnings("unchecked")
	public DriverProxy(String className, ClassLoader classLoader) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class<Driver> c = (Class<Driver>) classLoader.loadClass(className);
		driver = c.newInstance();
		log.info("SQL driver loaded: v" + driver.getMajorVersion() + "." + driver.getMinorVersion()
				+ " [" + driver.getClass().getName() + "]");
	}

	public static void loadSQLDriver(String className, ClassLoader classLoader)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			SQLException {
		DriverProxy driver = new DriverProxy(className, classLoader);
		DriverManager.registerDriver(driver);
	}

	public boolean acceptsURL(String url) throws SQLException {
		return driver.acceptsURL(url);
	}

	public Connection connect(String url, Properties info) throws SQLException {
		return driver.connect(url, info);
	}

	public int getMajorVersion() {
		return driver.getMajorVersion();
	}

	public int getMinorVersion() {
		return driver.getMinorVersion();
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return driver.getPropertyInfo(url, info);
	}

	public boolean jdbcCompliant() {
		return driver.jdbcCompliant();
	}

}
