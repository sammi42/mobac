package mobac.optional;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ColorQuantizerDescriptor;

/**
 * Centralizes all methods that require the optional Java Advanced Imaging
 * library.
 * 
 */
public class JavaAdvancedImaging {

	// private static final Logger log =
	// Logger.getLogger(JavaAdvancedImaging.class);

	public static BufferedImage colorReduceMedianCut(BufferedImage image, int colorCount) {
		int pixelBits = image.getColorModel().getPixelSize();
		if (pixelBits != 24) {
			/*
			 * For preventing the javax.media.jai.util.ImagingException: All
			 * factories fail for the operation "ColorQuantizer" we have to
			 * create a "compatible" (e.g. TYPE_3BYTE_BGR) BufferedImage
			 */
			BufferedImage trueColorImage = new BufferedImage(image.getWidth(), image.getHeight(),
					BufferedImage.TYPE_3BYTE_BGR);
			Graphics g = trueColorImage.getGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			image = trueColorImage;
		}
		RenderedOp ro = ColorQuantizerDescriptor.create(image, ColorQuantizerDescriptor.MEDIANCUT, // 
				new Integer(colorCount), // Max number of colors
				null, null, new Integer(1), Integer.valueOf(1), null);
		return ro.getAsBufferedImage();
	}
}
