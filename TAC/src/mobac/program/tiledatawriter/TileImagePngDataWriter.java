package mobac.program.tiledatawriter;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import mobac.program.interfaces.TileImageDataWriter;

import org.apache.log4j.Logger;


public class TileImagePngDataWriter implements TileImageDataWriter {

	protected Logger log;

	protected ImageWriter pngImageWriter = null;

	public TileImagePngDataWriter() {
		log = Logger.getLogger(this.getClass());
	}

	public void initialize() {
		if (log.isTraceEnabled()) {
			String s = "Available PNG image writers:";
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
			while (writers.hasNext()) {
				ImageWriter w = writers.next();
				s += "\n\t" + w.getClass().getName();
			}
			log.trace(s);
		}
		pngImageWriter = ImageIO.getImageWritersByFormatName("png").next();
		log.debug("Used PNG image writer: " + pngImageWriter.getClass().getName());
	}

	public void processImage(RenderedImage image, OutputStream out) throws IOException {
		pngImageWriter.setOutput(ImageIO.createImageOutputStream(out));
		IIOImage ioImage = new IIOImage(image, null, null);
		pngImageWriter.write(ioImage);
	}

	public void dispose() {
		pngImageWriter.dispose();
		pngImageWriter = null;
	}

	public String getFileExt() {
		return "png";
	}

}
