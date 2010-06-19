package mobac.tools.testtileserver.servlets;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mobac.program.tiledatawriter.TileImageJpegDataWriter;

/**
 * 
 * Generates for each request a png tile of size 256x256 containing the url request broken down to multiple lines.
 * 
 * @author r_x
 * 
 */
public class JpgTileGeneratorServlet extends AbstractTileGeneratorServlet {

	private final TileImageJpegDataWriter jpgWriter;

	/**
	 * @param compressionLevel
	 *            [0..100]
	 */
	public JpgTileGeneratorServlet(int compressionLevel) {
		super();
		jpgWriter = new TileImageJpegDataWriter(compressionLevel / 100d);
		jpgWriter.initialize();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedImage tile = generateImage(request);
		response.setContentType("image/jpeg");
		ServletOutputStream out = response.getOutputStream();
		ByteArrayOutputStream bout = new ByteArrayOutputStream(32000);
		try {
			synchronized (jpgWriter) {
				jpgWriter.processImage(tile, bout);
			}
			byte[] buf = bout.toByteArray();
			response.setContentLength(buf.length);
			out.write(buf);
		} finally {
			out.close();
			response.flushBuffer();
		}
	}
}
