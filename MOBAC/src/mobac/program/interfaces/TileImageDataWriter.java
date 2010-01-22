package mobac.program.interfaces;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface TileImageDataWriter {

	public void initialize();
	/**
	 * Processes the <code>image</code> according to the implementation of this
	 * interfaces and saves the image data in a binary representation such as
	 * PNG, JPG, ...into the given OutputStream.
	 * 
	 * @param image
	 * @param out
	 *            {@link OutputStream} to write binary image data to (usually
	 *            this is a {@link FileOutputStream} or a
	 *            {@link ByteArrayOutputStream}
	 * @throws IOException
	 */
	public void processImage(RenderedImage image, OutputStream out) throws IOException;
	
	public void dispose();
	
	public String getFileExt();
}
