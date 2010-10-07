package mobac.program.tiledatawriter;

import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteWarningListener;

import org.apache.log4j.Logger;

/**
 * Allows to capture non-fatal warnings that may occur upon writing an image. At the moment all warnings are simply
 * logged.
 */
public class ImageWriterWarningListener implements IIOWriteWarningListener {

	private static final Logger log = Logger.getLogger(ImageWriterWarningListener.class);

	public static final IIOWriteWarningListener INSTANCE = new ImageWriterWarningListener();
	
	public void warningOccurred(ImageWriter source, int imageIndex, String warning) {
		if (log.isDebugEnabled())
			log.warn(warning + " - caused by: " + source + " on imageIndex " + imageIndex);
		else
			log.warn(warning);
	}

}
