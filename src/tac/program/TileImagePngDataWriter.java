package tac.program;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import tac.program.interfaces.TileImageDataWriter;

public class TileImagePngDataWriter implements TileImageDataWriter {

	protected ImageWriter pngImageWriter = null;

	protected ImageWriteParam iwp = null;

	public TileImagePngDataWriter() {
	}

	public void initialize() {
		pngImageWriter = ImageIO.getImageWritersByFormatName("png").next();
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
