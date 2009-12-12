package rmp.rmpfile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;

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

	public MultiImage(TacTile[] images, MapInterface map) {
		log.debug("New instance: images count " + images.length + "\n\t" + Arrays.toString(images));
		this.images = images;

		/* --- Collect the extremes of the coordinates --- */
		bounds = buildBoundingRect();
		Point max = map.getMaxTileCoordinate();
		Point min = map.getMinTileCoordinate();
		imageWidth = max.x - min.x;
		imageHeight = max.y - min.y;
	}

	private BoundingRect buildBoundingRect() {
		double north = 90.0D;
		double south = -90.0D;
		double west = 180.0D;
		double east = -180.0D;

		for (TacTile tile : images) {
			BoundingRect part_bounds = tile.getBoundingRect();

			west = Math.min(west, part_bounds.getWest());
			east = Math.max(east, part_bounds.getEast());
			north = Math.min(north, part_bounds.getNorth());
			south = Math.max(south, part_bounds.getSouth());
		}

		return new BoundingRect(north, south, west, east);
	}

	public BoundingRect getBoundingRect() {
		return this.bounds;
	}

	public BufferedImage getSubImage(BoundingRect area, int width, int height) {
		if (log.isTraceEnabled())
			log.trace(String.format("getSubImage %d %d %s", width, height, area));

		int hit = 0;

		BufferedImage result = new BufferedImage(width, height, 1);

		Graphics2D graph = result.createGraphics();
		try {
			graph.setColor(new Color(255, 255, 255));
			graph.fillRect(0, 0, width, height);

			int i = 0;
			do {
				TacTile image = images[i];
				hit = hitType(image.getBoundingRect(), area);
				// log.trace("HIT: " + hit + " " + this.images[i]);

				if (hit != HIT_NOHIT) {
					image.drawSubImage(area, result);
				}
				++i;
			} while (hit != HIT_FULLHIT && i < images.length);
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

		// Test up the possibilities where "small" lies totally outside of "big"
		if (small.getWest() > big.getEast())
			return HIT_NOHIT; // no intersection possible
		if (small.getEast() < big.getWest())
			return HIT_NOHIT; // no intersection possible
		if (small.getSouth() < big.getNorth())
			return HIT_NOHIT; // no intersection possible
		if (small.getNorth() > big.getSouth())
			return HIT_NOHIT; // no intersection possible

		int hit = HIT_NOHIT;
		/* --- Count the number of hits --- */
		if (small.getWest() >= big.getWest() && small.getNorth() >= big.getNorth())
			hit++;

		if (small.getEast() <= big.getEast() && small.getNorth() >= big.getNorth())
			hit++;

		if (small.getWest() >= big.getWest() && small.getSouth() <= big.getSouth())
			hit++;

		if (small.getEast() <= big.getEast() && small.getSouth() <= big.getSouth())
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
		return "MultiImage [bounds=" + bounds + ", imageHeight=" + imageHeight + ", imageWidth="
				+ imageWidth + ", images=" + Arrays.toString(images) + "]";
	}

}