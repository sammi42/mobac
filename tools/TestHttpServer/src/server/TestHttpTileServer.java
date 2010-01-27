package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
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

	public static boolean GENERATE_PNG_FOR_EACH_REQUEST = true;
	public static int PNG_COMPRESSION = 1;

	public static int DELAY = 400;

	/**
	 * Error rate in percent
	 */
	private static int ERROR_RATE = 0;
	private static boolean ERROR_ON_URL = false;

	private static TestHttpTileServer server;

	private static final SecureRandom RAND = new SecureRandom();

	private static final long serialVersionUID = -1L;

	private static MessageDigest MD5;

	public TestHttpTileServer() {
		// setting aliases, for an optional file servlet
		PathTreeDictionary aliases = new PathTreeDictionary();
		setMappingTable(aliases);
		// setting properties for the server, and exchangable Acceptors
		Properties properties = new Properties();
		ERROR_RATE = Integer.getInteger("TestHttpServer.errorRate", 0);
		ERROR_ON_URL = Boolean.getBoolean("TestHttpServer.errorOnSpecificUrls");
		PNG_COMPRESSION = Integer.getInteger("TestHttpServer.generatedPNGcompression", 1);
		DELAY = Integer.getInteger("TestHttpServer.delay", 0);
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

		try {
			MD5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
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

	public static boolean errorResponse(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (ERROR_RATE == 0)
			return false;
		if (ERROR_ON_URL) {
			String url = request.getRequestURL() + request.getQueryString();
			byte[] digest = MD5.digest(url.getBytes());
			int hash = Math.abs(digest[4] % 100);
			System.out.println(url.toString() + " -> " + hash + ">" + ERROR_RATE+"?");
			if (hash > ERROR_RATE)
				return false;
		} else {
			int rnd = RAND.nextInt(100);
			if (rnd > ERROR_RATE)
				return false;
		}
		response.sendError(404);
		System.out.println("Error sent");
		return true;
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
			FileInputStream fi = new FileInputStream("DebugTileServer.properties");
			prop.load(fi);
			fi.close();
			System.getProperties().putAll(prop);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Unable to load file DebugTileServer.properties: "
					+ e.getMessage() + "\nUsing default values", "Error loading properties",
					JOptionPane.ERROR_MESSAGE);
		}
		server = new TestHttpTileServer();
		server.serve();
	}

}