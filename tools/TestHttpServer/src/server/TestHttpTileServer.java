package server;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import servlets.PngFileTileServlet;
import servlets.PngTileGeneratorServlet;
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

	private static final boolean GENERATE_PNG_FOR_EACH_REQUEST = true;

	/**
	 * Error rate in percent
	 */
	private static final int ERROR_RATE = 0;

	private static final SecureRandom RAND = new SecureRandom();

	private static final long serialVersionUID = -1L;

	public TestHttpTileServer() {
		// setting aliases, for an optional file servlet
		PathTreeDictionary aliases = new PathTreeDictionary();
		setMappingTable(aliases);
		// setting properties for the server, and exchangable Acceptors
		Properties properties = new Properties();
		properties.put("port", 80);
		properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
		this.arguments = properties;
		addDefaultServlets(null);

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
		TestHttpTileServer srv = new TestHttpTileServer();
		srv.serve();
	}
}