package mobac.tools.testtileserver.servlets;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mobac.utilities.imageio.Png4BitWriter;

/**
 * 
 * Generates for each request a png tile of size 256x256 containing the url request broken down to multiple lines.
 * 
 * @author r_x
 * 
 */
public class PngTileGeneratorServlet extends AbstractTileGeneratorServlet {

	private int pngCompressionLevel;

	public PngTileGeneratorServlet(int pngCompressionLevel) {
		super();
		this.pngCompressionLevel = pngCompressionLevel;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedImage tile = generateImage(request);
		response.setContentType("image/png");
		OutputStream out = response.getOutputStream();
		ByteArrayOutputStream bout = new ByteArrayOutputStream(16000);
		Png4BitWriter.writeImage(bout, tile, pngCompressionLevel, request.getRequestURL().toString());
		byte[] buf = bout.toByteArray();
		response.setContentLength(buf.length);
		out.write(buf);
		out.close();
		response.flushBuffer();
	}
}
