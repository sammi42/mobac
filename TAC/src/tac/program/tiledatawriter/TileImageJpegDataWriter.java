package tac.program.tiledatawriter;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.log4j.Logger;

import tac.program.interfaces.TileImageDataWriter;

public class TileImageJpegDataWriter implements TileImageDataWriter {

	protected Logger log;

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
		log = Logger.getLogger(this.getClass());
	}

	public void initialize() {
		if (log.isTraceEnabled()) {
			String s = "Available JPEG image writers:";
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
			while (writers.hasNext()) {
				ImageWriter w = writers.next();
				s += "\n\t" + w.getClass().getName();
			}
			log.trace(s);
		}
		jpegImageWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
		log.debug("Used JPEG image writer: " + jpegImageWriter.getClass().getName());
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
