package rmp.rmpmaker;

import java.io.File;

import org.apache.log4j.Logger;

import rmp.gui.UserInterface;
import rmp.gui.osm.BackgroundInfo;
import rmp.rmpfile.RmpLayer;
import rmp.rmpfile.RmpPacker;
import rmp.rmpfile.entries.Bmp2bit;
import rmp.rmpfile.entries.Bmp4bit;
import rmp.rmpfile.entries.RmpIni;


public class ImageProcessor extends Thread {

	private static int num = 0;

	Logger log = Logger.getLogger(ImageProcessor.class);

	BackgroundInfo frontend;
	File destPath;
	UserInterface userIf;

	/**
	 * Constructor
	 * 
	 * @param jpg_file
	 *            path to read image from
	 * @param nw
	 *            Coordinate of North-West corner
	 * @param ne
	 *            Coordinate of North-East corner
	 * @param sw
	 *            Coordinate of South-West corner
	 * @param se
	 *            Coordinate of South-East corner
	 * @param frontend
	 *            BackgroundInfo for status info
	 */
	public ImageProcessor(UserInterface ui, BackgroundInfo frontend, File dest_path)
			throws Exception {
		super("ImageProcessor " + (num++));
		this.userIf = ui;
		this.frontend = frontend;
		this.destPath = dest_path;
		log.debug("ImageProcessor created");
	}

	/**
	 * Background worker thread function
	 */
	public void run() {
		log.debug("run()");
		Throwable ex = null;
		int i = 0;
		try {
			/* --- Check if settings are plausible --- */
			frontend.setActionText("Checking entries");
			String error = userIf.checkPlausible();
			if (error != null)
				throw new IllegalArgumentException(error);

			/* --- Build calibrated image --- */
			frontend.setActionText("Read image file");
			CalibratedImage[] imageCreators = userIf.getImageCreator();

			log.debug("image creators: " + imageCreators.length);

			/*
			 * --- Build the TLM files and free the memory of the image that is
			 * no longer needed after creating the TLM file ---
			 */
			RmpLayer[] rmpLayer = new RmpLayer[imageCreators.length];
			for (i = 0; i < rmpLayer.length; i++) {
				log.info("********* LAYER: " + i);
				rmpLayer[i] = RmpLayer.createFromSimpleImage(imageCreators[i], frontend, i + 1);
				imageCreators[i] = null;
			}

			log.info("***************************************************");

			imageCreators = null;

			String image_name = buildImageName(destPath);

			/* --- Create RMP.ini --- */
			RmpIni rmp_ini = new RmpIni(image_name, rmpLayer.length);

			/* --- Create packer and fill it with content --- */
			frontend.setActionText("Write RMP file to disk");
			RmpPacker packer = new RmpPacker();
			packer.addFile(rmp_ini);

			for (i = 0; i < rmpLayer.length; i++) {
				String layerName = buildTileName(image_name, i);
				log.trace("Adding layer: " + layerName);
				packer.addFile(rmpLayer[i].getTLMFile(layerName));
				packer.addFile(rmpLayer[i].getA00File(layerName));

				/* --- Free resources --- */
				rmpLayer[i] = null;
			}

			packer.addFile(new Bmp2bit());
			packer.addFile(new Bmp4bit());

			/* --- Write to disk --- */
			packer.writeToDisk(getDestPath(destPath));
		} catch (Throwable e) {
			log.error("", e);
			ex = e;
		}

		/* --- Notify front end about the result --- */
		frontend.finished(ex);
	}

	/**
	 * Build destination path from source path.
	 * <P>
	 * The destination path is the same as the source path but filename is
	 * limited to image_name and extension is "rmp"
	 */
	private File getDestPath(File source_path) {
		File dir = source_path.getParentFile();

		/* --- Add image name --- */
		String name = buildImageName(source_path);

		/* --- Add Extension --- */
		name += ".rmp";

		return new File(dir, name);
	}

	/**
	 * Build an image name from a filename. The image name is the name of the
	 * file without path and extension . The length of the name is limited to 8
	 * chars. We use only 6 chars, so we can use 99 images
	 */
	private String buildImageName(File filename) {
		int index;

		String name = filename.getName();

		/* --- Remove the extension --- */
		index = name.indexOf('.');
		if (index != -1)
			name = name.substring(0, index);

		/* --- Limit the filename to 8 chars --- */
		if (name.length() > 8)
			name = name.substring(0, 8);

		return name.toLowerCase();
	}

	/**
	 * Builds a tile name from a basename and an index.
	 */
	public static String buildTileName(String basename, int index) {
		String indexstr;

		/* --- Convert the index number to a string --- */
		indexstr = String.valueOf(index);

		/*
		 * --- cut the basename so that basename+index is not longer than 8
		 * chars ---
		 */
		if (indexstr.length() + basename.length() > 8)
			basename = basename.substring(0, 8 - indexstr.length());

		return basename + indexstr;
	}
}
