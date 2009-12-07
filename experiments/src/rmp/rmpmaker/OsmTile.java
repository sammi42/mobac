package rmp.rmpmaker;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class OsmTile implements CalibratedImage2 {
	private static final Logger log = Logger.getLogger(OsmTile.class);
	private int tilex;
	private int tiley;
	private int zoom;
	private String host;
	private BufferedImage image;

	public OsmTile(int x, int y, int zoom, String host) {
		this.tilex = x;
		this.tiley = y;
		this.zoom = zoom;
		this.host = host;

		image = null;
	}

	/**
	 * Returns the image of the tile. Creates on if necessary
	 */
	public BufferedImage getImage() {

		/* --- Load image if none is present --- */
		if (image == null)
			image = loadImage();

		return image;
	}

	/**
	 * Get the image that is associated with the tile. If there is no such
	 * image, then a black one is created.
	 * <P>
	 * 
	 * The image is buffered as long as the release function is not called
	 */
	private BufferedImage loadImage() {
		String filename;
		int maxtile;

		/* --- Build path and name of the image file --- */
		filename = String.format("%d/%d/%d.png", zoom, tilex, tiley);
		log.debug("loadImage " + filename);
		/*
		 * --- Create a black image if the coordinates are outside the maximum
		 * ---
		 */
		maxtile = (int) Math.pow(2, zoom);
		if (tilex >= maxtile || tiley >= maxtile)
			image = createBlack(256, 256);

		/* --- Load file from the host service --- */
		try {
			if (image == null)
				image = ImageIO.read(new URL(host + filename));
		} catch (IOException e) {
			/* --- Create a black image --- */
			image = createBlack(256, 256);

			/* --- Write filename into the image --- */
			Graphics graph = image.getGraphics();
			graph.setColor(Color.WHITE);
			graph.drawString(filename, 10, 120);
			graph.drawRect(0, 0, 255, 255);
		}

		return image;
	}

	/**
	 * create a black Tile
	 * 
	 * @return image of black square
	 */
	private BufferedImage createBlack(int width, int height) {
		BufferedImage img;

		// Create an image that does not support transparency
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		/* --- Create a graphics context from the image --- */
		Graphics graph = img.getGraphics();

		/* --- Fill it with black --- */
		graph.setColor(Color.BLACK);
		graph.fillRect(0, 0, width, height);

		return img;
	}

	public void getSubImage(BoundingRect dest_area, BufferedImage dest_image) {
		BufferedImage src_image;
		WritableRaster src_graph, dst_graph;
		BoundingRect src_area;
		int maxx, maxy;
		int x, y;
		double src_c_x, src_c_y;
		double help;
		int pix_x, pix_y;
		BufferedImage imageBuffer;
		Graphics graphics;
		int[] pixel = new int[3];

		/* --- Get the coordination rectangle of the source image --- */
		src_area = getBoundingRect();

		/* --- Get Graphics context --- */
		src_image = getImage();
		dst_graph = dest_image.getRaster();
		src_graph = src_image.getRaster();

		/* --- Convert it to RGB color space --- */
		imageBuffer = new BufferedImage(src_image.getWidth(), src_image.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		graphics = imageBuffer.createGraphics();
		graphics.drawImage(src_image, 0, 0, null);
		src_graph = imageBuffer.getRaster();

		/*
		 * --- Iterate over all pixels of the destination image. Unfortunately
		 * we need this technique because source and dest do not have exactly
		 * the same zoom level, so the source image has to be compressed or
		 * expanded to match the destination image ---
		 */
		maxx = dest_image.getWidth();
		maxy = dest_image.getHeight();
		for (y = 0; y < maxy; y++) {

			/* --- Calculate the y-coordinate of the current line --- */
			src_c_y = dest_area.getNorth() + (dest_area.getSouth() - dest_area.getNorth()) * y
					/ maxy;

			/* --- Calculate the pixel line of the source image --- */
			help = (src_c_y - src_area.getNorth()) * 256
					/ (src_area.getSouth() - src_area.getNorth()) + 0.5;
			pix_y = (int) help;

			/* --- Ignore line that are out of the source area --- */
			if (pix_y < 0 || pix_y > 255)
				continue;

			for (x = 0; x < maxx; x++) {
				/* --- Calculate the x-coordinate of the current row --- */
				src_c_x = dest_area.getWest() + (dest_area.getEast() - dest_area.getWest()) * x
						/ maxx;

				/* --- Calculate the pixel row of the source image --- */
				help = (src_c_x - src_area.getWest()) * 256
						/ (src_area.getEast() - src_area.getWest()) + 0.5;
				pix_x = (int) help;

				/* --- Ignore the row if it is outside the source area --- */
				if (pix_x < 0 || pix_x > 255)
					continue;

				/* --- Transfer the pixel --- */
				src_graph.getPixel(pix_x, pix_y, pixel);
				dst_graph.setPixel(x, y, pixel);
			}
		}
	}

	public void releaseResources() {
		image = null;
	}

	public BoundingRect getBoundingRect() {
		return new BoundingRectOsm(tilex, tiley, 256, 256, zoom);
	}

	public int getImageHeight() {
		/* --- A tile is always 256 pixels high --- */
		return 256;
	}

	public int getImageWidth() {
		/* --- A tile is always 256 pixels wide --- */
		return 256;
	}

	public BufferedImage getSubImage(BoundingRect area, int width, int height) {
		BufferedImage result;

		result = createBlack(width, height);

		getSubImage(area, result);

		return result;
	}

	@Override
	public String toString() {
		return String.format("OsmTile x/y/z [%d/%d/%d]", tilex, tiley, zoom);
	}

}
