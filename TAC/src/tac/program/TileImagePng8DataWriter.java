package tac.program;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import tac.optional.JavaAdvancedImaging;

public class TileImagePng8DataWriter extends TileImagePngDataWriter {

	public TileImagePng8DataWriter() {
	}

	public void processImage(RenderedImage image, OutputStream out) throws IOException {
		BufferedImage image2 = JavaAdvancedImaging.colorReduceMedianCut(image, 256);
		super.processImage(image2, out);
	}

}
