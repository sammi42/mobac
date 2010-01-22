package mobac.optional;

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
				null, null, new Integer(1), Integer.valueOf(1), null);
		return ro.getAsBufferedImage();
	}

}
