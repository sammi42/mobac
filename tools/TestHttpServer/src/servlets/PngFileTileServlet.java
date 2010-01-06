package servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import server.TestHttpTileServer;

/**
 * 
 * Returns for each an every request the same static png file. For performance
 * reasons the file "tile.png" is cached on servlet initialization.
 * 
 * @author r_x
 * 
 */
public class PngFileTileServlet extends HttpServlet {
	private static final long serialVersionUID = 4314615099893608350L;

	private static byte[] fileContent = null;

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			InputStream in = PngTileGeneratorServlet.class.getResourceAsStream("tile.png");
			fileContent = new byte[in.available()];
			in.read(fileContent);
			in.close();
			System.out.println("Static png file loaded successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (TestHttpTileServer.errorResponse(request, response))
			return;
		response.setContentType("image/png");
		OutputStream out = response.getOutputStream();
		out.write(fileContent);
		out.close();
	}

}
