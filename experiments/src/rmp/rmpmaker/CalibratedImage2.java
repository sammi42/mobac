package rmp.rmpmaker;

import java.awt.image.BufferedImage;

public interface CalibratedImage2 extends CalibratedImage {
	public abstract void releaseResources();

	public abstract void getSubImage(BoundingRect paramBoundingRect,
			BufferedImage paramBufferedImage);

}
