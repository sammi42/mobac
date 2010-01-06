package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import servlets.PngFileTileServlet;
import servlets.PngTileGeneratorServlet;
import servlets.ShutdownServlet;
import Acme.Serve.Serve;

/**
 * 
 * <p>
 * Provides a dummy HTTP server that returns a png for each request.
 * </p>
 * 
 * Depending on the static variable <code>GENERATE_PNG_FOR_EACH_REQUEST</code>
 * the returned png is generated (requires a lot of CPU!) or static.
 * 
 * @author r_x
 * 
 */
public class TestHttpTileServer extends Serve {

	private static boolean GENERATE_PNG_FOR_EACH_REQUEST = true;

	/**
	 * Error rate in percent
	 */
	private static int ERROR_RATE = 0;

	private static TestHttpTileServer server;

	private static final SecureRandom RAND = new SecureRandom();

	private static final long serialVersionUID = -1L;

	public TestHttpTileServer() {
		// setting aliases, for an optional file servlet
		PathTreeDictionary aliases = new PathTreeDictionary();
		setMappingTable(aliases);
		// setting properties for the server, and exchangable Acceptors
		Properties properties = new Properties();
		int port = Integer.getInteger("TestHttpServer.port", 80);

		try {
			HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port
					+ "/shutdown").openConnection();
			c.setConnectTimeout(100);
			c.setRequestMethod("DELETE");
			c.connect();
			if (c.getResponseCode() == 202)
				Thread.sleep(1000);
			c.disconnect();
		} catch (SocketTimeoutException e) {
			// port is unused -> OK
		} catch (Exception e) {
			e.printStackTrace();
		}

		properties.put("port", port);
		properties.put("z", "20"); // max number of created threads in a thread
		properties.put("keep-alive", "true");
		properties.put("bind-address", "127.0.0.1");
		// pool
		properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
		this.arguments = properties;
		// addDefaultServlets(null);
		GENERATE_PNG_FOR_EACH_REQUEST = Boolean.getBoolean("TestHttpServer.generatePNGperRequest");

		addServlet("/shutdown", new ShutdownServlet());
		if (GENERATE_PNG_FOR_EACH_REQUEST)
			addServlet("/", new PngTileGeneratorServlet());
		else
			addServlet("/", new PngFileTileServlet());

		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	}

	public static boolean errorResponse(HttpServletResponse response) throws IOException {
		if (ERROR_RATE == 0)
			return false;
		int rnd = RAND.nextInt(100);
		if (rnd <= ERROR_RATE) {
			response.sendError(404);
			System.out.println("Error");
			return true;
		}
		return false;
	}

	public static void shutdown() {
		try {
			server.notifyStop();
		} catch (IOException e) {
		}
	}

	protected class ShutdownHook extends Thread {
		public void run() {
			try {
				TestHttpTileServer.this.notifyStop();
			} catch (java.io.IOException ioe) {
			}
			TestHttpTileServer.this.destroyAllServlets();
		}
	}

	public static void main(String[] args) {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("server.properties"));
			System.getProperties().putAll(prop);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Unable to load server.properties: "
					+ e.getMessage() + "\nUsing default values", "Error loading properties",
					JOptionPane.ERROR_MESSAGE);
		}
		server = new TestHttpTileServer();
		server.serve();
	}

}