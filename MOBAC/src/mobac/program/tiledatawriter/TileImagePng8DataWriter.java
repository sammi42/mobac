package mobac.program.tiledatawriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import mobac.optional.JavaAdvancedImaging;

public class TileImagePng8DataWriter extends TileImagePngDataWriter {

	public TileImagePng8DataWriter() {
	}

	@Override
	public void processImage(BufferedImage image, OutputStream out) throws IOException {
		BufferedImage image2 = JavaAdvancedImaging.colorReduceMedianCut(image, 256);
		System.out.println(image2.getColorModel());
		super.processImage(image2, out);
	}

}
