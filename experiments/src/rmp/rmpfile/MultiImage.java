package rmp.rmpfile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import rmp.interfaces.CalibratedImage;
import rmp.rmpmaker.BoundingRect;
import rmp.rmpmaker.TacTile;
import tac.program.interfaces.MapInterface;

/**
 * CalibratedImage that gets its data from a set of other CalibratedImage2
 * 
 */
public class MultiImage implements CalibratedImage {

	private static final Logger log = Logger.getLogger(MultiImage.class);

	private static final int HIT_NOHIT = 0;
	private static final int HIT_OVERLAP = 1;
	private static final int HIT_FULLHIT = 2;

	private final TacTile[] images;
	private final BoundingRect bounds;
	private final int imageWidth;
	private final int imageHeight;
	private final List<TacTile> lastImages;

	private int activeImageMax;

	public MultiImage(TacTile[] images, MapInterface map) {
		this.images = images;

		this.lastImages = new LinkedList<TacTile>();

		/* --- Collect the extremes of the coordinates --- */
		bounds = buildBoundingRect();
		Point max = map.getMaxTileCoordinate();
		Point min = map.getMinTileCoordinate();
		imageWidth = max.x - min.x;
		imageHeight = max.y - min.y;
		activeImageMax = 64;
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

	private BoundingRect buildBoundingRect() {
		double north = -90.0;
		double south = 90.0;
		double west = 180.0;
		double east = -180.0;

		for (TacTile tile : images) {
			BoundingRect part_bounds = tile.getBoundingRect();

			west = Math.min(west, part_bounds.getWest());
			east = Math.max(east, part_bounds.getEast());
			north = Math.max(north, part_bounds.getNorth());
			south = Math.min(south, part_bounds.getSouth());
		}

		return new BoundingRect(north, south, west, east);
	}

	public BoundingRect getBoundingRect() {
		return this.bounds;
	}

	public BufferedImage getSubImage(BoundingRect area, int width, int height) {
		log.debug(String.format("getSubImage %d %d %s", width, height, area));
		BufferedImage result = null;

		int hit = 0;

		boolean found = false;

		result = new BufferedImage(width, height, 1);

		Graphics2D graph = result.createGraphics();
		try {
			graph.setColor(new Color(255, 255, 255));
			graph.fillRect(0, 0, width, height);

			int i = 0;
			do {
				hit = hitType(this.images[i].getBoundingRect(), area);
				log.trace("HIT: " + hit + " " + this.images[i]);

				if (hit != HIT_NOHIT) {
					this.images[i].drawSubImage(area, result);

					for (int last = 0; (last < this.lastImages.size()); ++last) {
						if (this.lastImages.get(last) == this.images[i]) {
							found = true;
							break;
						}
					}
					if (!found)
						this.lastImages.add(this.images[i]);
				}
				++i;
			} while (hit != HIT_FULLHIT && i < images.length);

			/* --- Free resources if we have more then max images loaded --- */
			while (this.lastImages.size() > this.activeImageMax) {
				this.lastImages.remove(0);
			}
		} finally {
			graph.dispose();
		}
		return result;
	}

	/**
	 * Checks if the small rect is part of the big rect
	 * 
	 * @return 0=no hit, 1=overlap, 2=full hit
	 */
	private int hitType(BoundingRect big, BoundingRect small) {
		int hit = HIT_NOHIT;

		/* --- Count the number of hits --- */
		if (small.getWest() >= big.getWest() && small.getWest() <= big.getEast()
				&& small.getNorth() <= big.getNorth() && small.getNorth() >= big.getSouth())
			hit++;

		if (small.getEast() >= big.getWest() && small.getEast() <= big.getEast()
				&& small.getNorth() <= big.getNorth() && small.getNorth() >= big.getSouth())
			hit++;

		if (small.getWest() >= big.getWest() && small.getWest() <= big.getEast()
				&& small.getSouth() <= big.getNorth() && small.getSouth() >= big.getSouth())
			hit++;

		if (small.getEast() >= big.getWest() && small.getEast() <= big.getEast()
				&& small.getSouth() <= big.getNorth() && small.getSouth() >= big.getSouth())
			hit++;

		/* --- Correct the result 0-4 to 0-2 --- */
		if (hit == 4)
			return HIT_FULLHIT;
		if (hit != 0)
			return HIT_OVERLAP;
		return HIT_NOHIT;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	@Override
	public String toString() {
		return "MultiImage [bounds=" + bounds + "\n\timages=" + Arrays.toString(images) + "]";
	}

}