package rmp.rmpmaker;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import rmp.interfaces.CalibratedImage;
import tac.program.atlascreators.tileprovider.TileProvider;

public class TacTile implements CalibratedImage {
	private static final Logger log = Logger.getLogger(TacTile.class);

	private final TileProvider tileProvider;
	private final int tilex;
	private final int tiley;
	private final int zoom;
	private BufferedImage image;

	private BoundingRect boundingRect;

	public TacTile(TileProvider tileProvider, MapSpace mapSpace, int tilex, int tiley, int zoom) {
		this.tileProvider = tileProvider;
		this.tilex = tilex;
		this.tiley = tiley;
		this.zoom = zoom;
		image = null;

		int tileSize = mapSpace.getTileSize();
		int x = tilex * tileSize;
		int y = tiley * tileSize;
		double north = mapSpace.cYToLat(y, zoom);
		double south = mapSpace.cYToLat(y + tileSize, zoom);
		double west = mapSpace.cXToLon(x, zoom);
		double east = mapSpace.cXToLon(x + tileSize, zoom);
		boundingRect = new BoundingRect(north, south, west, east);
		log.trace(this.toString());
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

	private BufferedImage loadImage() {
		try {
			image = tileProvider.getTileImage(tilex, tiley);
		} catch (IOException e) {
			log.error("", e);
			image = createBlack(256, 256);
		}
		return image;
	}

	/**
	 * create a black Tile
	 * 
	 * @return image of black square
	 */
	private BufferedImage createBlack(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics graph = img.getGraphics();
		graph.setColor(Color.BLACK);
		graph.fillRect(0, 0, width, height);
		return img;
	}

	public void drawSubImage(BoundingRect dest_area, BufferedImage dest_image) {
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
		try {
			graphics.drawImage(src_image, 0, 0, null);
		} finally {
			graphics.dispose();
		}
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

	public BoundingRect getBoundingRect() {
		return boundingRect;
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

		drawSubImage(area, result);

		return result;
	}

	@Override
	public String toString() {
		return String.format("TacTile x/y/z [%d/%d/%d] = %s", tilex, tiley, zoom, boundingRect);
	}

}
