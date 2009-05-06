package tac.program;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import tac.program.interfaces.TileImageDataWriter;

public class TileImageJpegDataWriter implements TileImageDataWriter {

	protected ImageWriter jpegImageWriter = null;

	protected ImageWriteParam iwp = null;

	protected float jpegCompressionLevel;

	/**
	 * 
	 * @param jpegCompressionLevel
	 *            a float between 0 and 1; 1 specifies minimum compression and
	 *            maximum quality
	 */
	public TileImageJpegDataWriter(double jpegCompressionLevel) {
		this.jpegCompressionLevel = (float) jpegCompressionLevel;
	}

	public void initialize() {
		jpegImageWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
		iwp = jpegImageWriter.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(jpegCompressionLevel);
	}

	public void processImage(RenderedImage image, OutputStream out) throws IOException {
		jpegImageWriter.setOutput(ImageIO.createImageOutputStream(out));
		IIOImage ioImage = new IIOImage(image, null, null);
		jpegImageWriter.write(null, ioImage, iwp);
	}

	public void dispose() {
		jpegImageWriter.dispose();
		jpegImageWriter = null;
	}

	public String getFileExt() {
		return "jpg";
	}

}
