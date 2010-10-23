package tiledb;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyTest {

	private String protocol = "jdbc:derby:";

	public DerbyTest() throws Exception {
		loadDriver();
		Connection conn = null;
		String dbName = "/test/tiles";
		conn = DriverManager.getConnection(protocol + dbName + ";create=true");
		conn.setAutoCommit(false);
		Statement s = conn.createStatement();
		try {
			s.execute("create table tile(x int, y int, zoom int, data blob)");
		} catch (SQLException e) {
		}
		conn.commit();
		conn.close();
	}

	public static void main(String[] args) {
		try {
			new DerbyTest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadDriver() {
		try {
			DriverManager.registerDriver((Driver) Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance());
			System.out.println("Loaded the appropriate driver");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
