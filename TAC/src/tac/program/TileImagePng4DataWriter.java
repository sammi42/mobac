package tac.program;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import tac.optional.JavaAdvancedImaging;
import tac.program.interfaces.TileImageDataWriter;
import tac.utilities.imageio.Png4BitWriter;

public class TileImagePng4DataWriter implements TileImageDataWriter {

	public TileImagePng4DataWriter() {
	}

	public void initialize() {
	}

	public void processImage(RenderedImage image, OutputStream out) throws IOException {
		BufferedImage image2 = JavaAdvancedImaging.colorReduceMedianCut(image, 16);
		Png4BitWriter.writeImage(out, image2);
	}

	public void dispose() {
	}

	public String getFileExt() {
		return "png";
	}

}
