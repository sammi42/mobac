package rmp.interfaces;

import java.awt.image.BufferedImage;

import rmp.rmpmaker.BoundingRect;

public interface CalibratedImage2 extends CalibratedImage {

	public abstract void getSubImage(BoundingRect paramBoundingRect,
			BufferedImage paramBufferedImage);

}
