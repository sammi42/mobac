package rmp.rmpfile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import rmp.interfaces.CalibratedImage;
import rmp.interfaces.CalibratedImage2;
import rmp.rmpmaker.BoundingRect;

/**
 * CalibratedImage that gets its data from a set of other CalibratedImage2
 * 
 */
public class MultiImage implements CalibratedImage {

	private static final Logger log = Logger.getLogger(MultiImage.class);

	private CalibratedImage2[] images;
	private BoundingRect bounds;
	private int imageWidth;
	private int imageHeight;
	private ArrayList<CalibratedImage2> lastImages;
	private int activeImageMax;

	public MultiImage(CalibratedImage2[] images) {
		log.debug("New instance: images count " + images.length + "\n\t" + Arrays.toString(images));
		this.images = images;

		this.lastImages = new ArrayList<CalibratedImage2>();

		/* --- Collect the extremes of the coordinates --- */
		buildBoundingRect();

		/*
		 * --- We cannot calculate the exact size of the image in pixel, so we
		 * have to guess a bit from the resolution of one part
		 */

		BoundingRect part_bounds = images[0].getBoundingRect();
		double help = images[0].getImageWidth() * (this.bounds.getEast() - this.bounds.getWest())
				/ (part_bounds.getEast() - part_bounds.getWest());
		this.imageWidth = (int) help;

		help = images[0].getImageHeight() * (this.bounds.getSouth() - this.bounds.getNorth())
				/ (part_bounds.getSouth() - part_bounds.getNorth());
		this.imageHeight = (int) help;

		this.activeImageMax = 4;
	}

	/**
	 * Number of images that may be stored in memory for the same time
	 */
	public int getActiveImageMax() {
		return activeImageMax;
	}

	/**
	 * Number of images that may be stored in memory for the same time
	 */
	public void setActiveImageMax(int activeImageMax) {
		this.activeImageMax = activeImageMax;
	}

	private void buildBoundingRect() {
		double north = 90.0D;
		double south = -90.0D;
		double west = 180.0D;
		double east = -180.0D;

		for (int i = 0; i < this.images.length; ++i) {
			BoundingRect part_bounds = this.images[i].getBoundingRect();

			if (part_bounds.getWest() < west) {
				west = part_bounds.getWest();
			}
			if (part_bounds.getEast() > east) {
				east = part_bounds.getEast();
			}
			if (part_bounds.getNorth() < north) {
				north = part_bounds.getNorth();
			}
			if (part_bounds.getSouth() > south) {
				south = part_bounds.getSouth();
			}
		}

		this.bounds = new BoundingRect(north, south, west, east);
	}

	public BoundingRect getBoundingRect() {
		return this.bounds;
	}

	public int getImageHeight() {
		return this.imageHeight;
	}

	public int getImageWidth() {
		return this.imageWidth;
	}

	public BufferedImage getSubImage(BoundingRect area, int width, int height) {
		log.debug(String.format("getSubImage %d %d %s", width, height, area));
		BufferedImage result = null;

		int hit = 0;

		boolean found = false;

		result = new BufferedImage(width, height, 1);

		Graphics2D graph = result.createGraphics();
		graph.setColor(new Color(255, 255, 255));
		graph.fillRect(0, 0, width, height);

		int i = 0;
		do {
			hit = hitType(this.images[i].getBoundingRect(), area);

			if (hit != 0) {
				this.images[i].getSubImage(area, result);

				for (int last = 0; (last < this.lastImages.size()) && (!(found)); ++last) {
					if (this.lastImages.get(last) == this.images[i]) {
						found = true;
					}
				}
				if (!(found))
					this.lastImages.add(this.images[i]);
			}
			++i;
			if (i >= this.images.length)
				break;
		} while (hit != 2);

		/* --- Free resources if we have more then max images loaded --- */
		while (this.lastImages.size() > this.activeImageMax) {
			this.lastImages.remove(0);
		}

		return result;
	}

	/**
	 * Checks if the small rect is part of the big rect
	 * 
	 * @return 0=no hit, 1=overlap, 2=full hit
	 */
	private int hitType(BoundingRect big, BoundingRect small) {
		int hit = 0;

		/* --- Count the number of hits --- */
		if (small.getWest() >= big.getWest() && small.getWest() <= big.getEast() && small.getNorth() >= big.getNorth()
				&& small.getNorth() <= big.getSouth())
			hit++;

		if (small.getEast() >= big.getWest() && small.getEast() <= big.getEast() && small.getNorth() >= big.getNorth()
				&& small.getNorth() <= big.getSouth())
			hit++;

		if (small.getWest() >= big.getWest() && small.getWest() <= big.getEast() && small.getSouth() >= big.getNorth()
				&& small.getSouth() <= big.getSouth())
			hit++;

		if (small.getEast() >= big.getWest() && small.getEast() <= big.getEast() && small.getSouth() >= big.getNorth()
				&& small.getSouth() <= big.getSouth())
			hit++;

		/* --- Correct the result 0-4 to 0-2 --- */
		if (hit == 4)
			hit = 2;
		else if (hit != 0)
			hit = 1;

		return hit;
	}

	@Override
	public String toString() {
		return "MultiImage [activeImageMax=" + activeImageMax + ", bounds=" + bounds + ", imageHeight=" + imageHeight
				+ ", imageWidth=" + imageWidth + ", images=" + Arrays.toString(images) + ", lastImages=" + lastImages
				+ "]";
	}

}