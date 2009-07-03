package tac.optional;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ColorQuantizerDescriptor;

/**
 * Centralizes all methods that require the optional Java Advanced Imaging
 * library.
 * 
 */
public class JavaAdvancedImaging {

	public static BufferedImage colorReduceMedianCut(RenderedImage image, int colorCount) {
		RenderedOp ro = ColorQuantizerDescriptor.create(image, ColorQuantizerDescriptor.MEDIANCUT, // 
				new Integer(colorCount), // Max number of colors
				null, null, new Integer(1), new Integer(1), null);
		return ro.getAsBufferedImage();

		// MedianCutQuantizer mc = new MedianCutQuantizer();
		// mc.setPaletteSize(colorCount);
		// BufferedRGB24Image i = new BufferedRGB24Image((BufferedImage) image);
		// //Paletted8Image out = new Paletted8Image();
		// mc.setInputImage(i);
		// mc.setOutputImage(i);
		// try {
		// mc.process();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// return (BufferedImage) image;
	}

	// public static BufferedImage colorReduceErrorDiffusion(RenderedImage
	// image, int colorCount) {
	//
	// ParameterBlock param = new ParameterBlock();
	// param.addSource(image);
	// param.add(ColorQuantizerDescriptor.NEUQUANT);
	// param.add(colorCount);
	//
	// RenderedOp intern = JAI.create("ColorQuantizer", param);
	// LookupTableJAI lut = (LookupTableJAI)
	// intern.getProperty("JAI.LookupTable");
	// intern = null; // no longer needed
	//
	// byte[][] data = lut.getByteData();
	// System.out.println(data.length);
	// for (byte[] d : data) {
	// System.out.println(d.length);
	// for (byte b : d) {
	// System.out.print(Integer.toHexString(b & 0xFF) + " ");
	// }
	// System.out.println();
	// }
	// byte[] r = new byte[] { (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte)
	// 0x00, (byte) 0x00,
	// (byte) 0xFF, (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte)
	// 0x00,
	// (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	// byte[] g = new byte[] { (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte)
	// 0xFF, (byte) 0x00,
	// (byte) 0xFF, (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte)
	// 0x00,
	// (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	// byte[] b = new byte[] { (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte)
	// 0x00, (byte) 0xFF,
	// (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte)
	// 0x00,
	// (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	// lut = new LookupTableJAI(new byte[][] { r, g, b }, 0);
	//
	// ParameterBlock param2 = new ParameterBlock();
	// param2.addSource(image);
	// param2.add(lut);
	// param2.add(KernelJAI.ERROR_FILTER_FLOYD_STEINBERG);
	//
	// RenderedOp finalOp = JAI.create("ErrorDiffusion", param2);
	// return finalOp.getAsBufferedImage();
	// }

}
