/* ------------------------------------------------------------------------

   OsmBrowser.java

   Project: JpgToRmp

  --------------------------------------------------------------------------*/

/* ---
 created: 06.07.2009 a.sander

 $History:$
 --- */

package rmp.gui.osm;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import rmp.rmpmaker.BoundingRectOsm;

/**
 * Swing Component that can show a map from OSM tiles
 * 
 */
public class JOsmBrowser extends JComponent implements Runnable, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private String host;
	private int originX;
	private int originY;
	private int zoomLevel;
	private int tileOffsetX;
	private int tileOffsetY;

	private int dragStartX;
	private int dragStartY;
	private int dragStartOffsetX;
	private int dragStartOffsetY;

	private HashMap<String, BufferedImage> loadedImages;
	private LinkedList<String> requestedImages;
	private volatile boolean mustStop;
	private Object sync;
	private Thread backgroundProcess;

	private static final int NAV_BAR_SIZE = 15;

	private static enum DIR {
		LEFT, RIGHT, UP, DOWN
	};

	/**
	 * Constructor
	 */
	public JOsmBrowser() {
		/* --- Keep Basic functionality --- */
		super();

		/* --- Create a map as image cache --- */
		loadedImages = new HashMap<String, BufferedImage>();

		/* --- Create a list of requested images --- */
		requestedImages = new LinkedList<String>();

		/* --- Initialize globals --- */
		originX = 0;
		originY = 0;
		tileOffsetX = 0;
		tileOffsetY = 0;
		dragStartX = 0;
		dragStartOffsetY = 0;
		mustStop = false;
		sync = new Object();

		/* --- Start Background processing --- */
		backgroundProcess = new Thread(this);
		backgroundProcess.start();

		/* --- Add a listener for mouse actions --- */
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	/**
	 * Set the signal to stop all backround processing
	 */
	public void stop() {
		/* --- Send stop signal to the background process --- */
		synchronized (sync) {
			mustStop = true;
			sync.notifyAll();
		}

		/* --- Wait until the background process has stopped --- */
		try {
			backgroundProcess.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The host from which the tiles are loaded
	 */
	public String getHost() {
		return host;
	}

	/**
	 * The host from which the tiles are loaded
	 */
	public void setHost(String host) {
		this.host = host;

		/* --- The image map becomes invalid if the host changes --- */
		synchronized (loadedImages) {
			loadedImages.clear();
			requestedImages.clear();
		}
	}

	/**
	 * The horizontal number of the tile in the upper left corner
	 */
	public int getOriginX() {
		return originX;
	}

	/**
	 * The horizontal number of the tile in the upper left corner
	 */
	public void setOriginX(int originX) {
		/* --- Make sure the value is not out of bounds --- */
		if (originX < 0)
			originX = 0;

		if (originX >= (int) Math.pow(2, zoomLevel))
			originX = (int) Math.pow(2, zoomLevel) - 1;

		/* --- Store the new value --- */
		this.originX = originX;

		/* --- Redraw the control --- */
		this.repaint();
	}

	/**
	 * The vertical number of the tile in the upper left corner
	 */
	public int getOriginY() {
		return originY;
	}

	/**
	 * The vertical number of the tile in the upper left corner
	 */
	public void setOriginY(int originY) {
		/* --- Make sure the value is not out of bounds --- */
		if (originY < 0)
			originY = 0;

		if (originY >= (int) Math.pow(2, zoomLevel))
			originY = (int) Math.pow(2, zoomLevel) - 1;

		/* --- Store the new value --- */
		this.originY = originY;

		/* --- Redraw the control --- */
		this.repaint();
	}

	/**
	 * Number of pixels that the origin of each tile is moved in X direction
	 */
	public int getTileOffsetX() {
		return tileOffsetX;
	}

	/**
	 * Number of pixels that the origin of each tile is moved in X direction
	 */
	public void setTileOffsetX(int tileOffsetX) {
		this.tileOffsetX = tileOffsetX;
	}

	/**
	 * Number of pixels that the origin of each tile is moved in Y direction
	 */
	public int getTileOffsetY() {
		return tileOffsetY;
	}

	/**
	 * Number of pixels that the origin of each tile is moved in Y direction
	 */
	public void setTileOffsetY(int tileOffsetY) {
		this.tileOffsetY = tileOffsetY;
	}

	/**
	 * The current zoom level
	 */
	public int getZoomLevel() {
		return zoomLevel;
	}

	/**
	 * The current zoom level
	 */
	public void setZoomLevel(int zoomLevel) {
		int old_zoomlevel;
		Rectangle bounds;

		/* --- The image cache becomes invalid if the zoom level changes --- */
		synchronized (loadedImages) {
			loadedImages.clear();
			requestedImages.clear();
		}

		/* --- Store the new zoom level --- */
		old_zoomlevel = this.zoomLevel;
		this.zoomLevel = zoomLevel;

		/* --- Calculate the dimensions of the map up to the middle --- */
		bounds = this.getBounds();
		bounds.width = (bounds.width - 2 * NAV_BAR_SIZE) / 2;
		bounds.height = (bounds.height - 2 * NAV_BAR_SIZE) / 2;

		/* --- Change x/y and offset to keep position of the screen middle --- */
		while (old_zoomlevel < zoomLevel) {
			originX *= 2;
			originY *= 2;
			tileOffsetX = tileOffsetX * 2 + bounds.width;
			tileOffsetY = tileOffsetY * 2 + bounds.height;
			old_zoomlevel++;
		}

		while (old_zoomlevel > zoomLevel) {
			tileOffsetX += 256 * (originX % 2);
			tileOffsetY += 256 * (originY % 2);
			originX /= 2;
			originY /= 2;
			tileOffsetX = tileOffsetX / 2 - bounds.width / 2;
			tileOffsetY = tileOffsetY / 2 - bounds.height / 2;
			old_zoomlevel--;
		}

		/* --- Make sure the tileOffset does not extend a whole tile --- */
		originX += tileOffsetX / 256;
		tileOffsetX %= 256;

		originY += tileOffsetY / 256;
		tileOffsetY %= 256;

		/*
		 * --- Tile Offset might be negative so the origin has to be moved one
		 * tile ---
		 */
		if (tileOffsetX < 0) {
			originX--;
			tileOffsetX += 256;
		}

		if (tileOffsetY < 0) {
			originY--;
			tileOffsetY += 256;
		}

		/*
		 * --- Call the set functions for the new values. This does a range
		 * check and a control repaint ---
		 */
		setOriginX(originX);
		setOriginY(originY);
	}

	/**
	 * Create a bounding box with the coordinates of the corners of the visible
	 * map
	 */
	public BoundingRectOsm getBoundingBox() {
		Rectangle control;

		/* --- Get size of the control --- */
		control = getBounds();

		return new BoundingRectOsm(getOriginX() + getTileOffsetX() / 256, getOriginY() + getTileOffsetY() / 256,
				control.width - 2 * NAV_BAR_SIZE, control.height - 2 * NAV_BAR_SIZE, getZoomLevel());
	}

	/**
	 * Returns a rect that is made of osm tile. x/y is the number of the tile
	 * int the upper left position and width/height are the dimensions in tiles
	 * 
	 * @return tile rectangle
	 */
	public Rectangle getTileRect() {
		Rectangle dim;
		Rectangle control_rect;

		/* --- Create result rectangle --- */
		dim = new Rectangle();

		/* --- Upper left corner is the corner of the origin rectangle --- */
		dim.x = getOriginX();
		dim.y = getOriginY();

		/*
		 * --- Width and height of the rectangle is calculated from the control
		 * width. Be aware that tile are possibly shown partially only. Because
		 * of this we add another tile in width and height ---
		 */
		control_rect = getBounds();
		dim.width = control_rect.width / 256 + 1;
		dim.height = control_rect.height / 256 + 1;

		return dim;
	}

	@Override
	public void paint(Graphics graph) {

		/* --- Paint corners --- */
		paintCorners(graph);

		/* --- Paint navigation bars --- */
		paintNavigationBars(graph);

		/* --- Paint the map area --- */
		paintMap(graph);
	}

	/**
	 * Paint the corners where the four navigation bars hit each other
	 * 
	 * @param graph
	 *            graphics device to paint on
	 */
	private void paintCorners(Graphics graph) {
		Rectangle clip_rect;
		Rectangle bounds;
		Rectangle corner = new Rectangle();

		/* --- Get size of the control --- */
		bounds = this.getBounds();

		/* --- Get the area that has to be repainted --- */
		clip_rect = graph.getClipBounds();

		/* --- Check the upper left corner --- */
		corner.setBounds(0, 0, NAV_BAR_SIZE, NAV_BAR_SIZE);
		if (corner.intersects(clip_rect))
			paintCorner(graph, corner);

		/* --- Upper right corner --- */
		corner.setBounds(bounds.width - NAV_BAR_SIZE - 1, 0, NAV_BAR_SIZE, NAV_BAR_SIZE);
		if (corner.intersects(clip_rect))
			paintCorner(graph, corner);

		/* --- Lower left corner --- */
		corner.setBounds(0, bounds.height - NAV_BAR_SIZE - 1, NAV_BAR_SIZE, NAV_BAR_SIZE);
		if (corner.intersects(clip_rect))
			paintCorner(graph, corner);

		/* --- Lower right corner --- */
		corner.setBounds(bounds.width - NAV_BAR_SIZE - 1, bounds.height - NAV_BAR_SIZE - 1, NAV_BAR_SIZE, NAV_BAR_SIZE);
		if (corner.intersects(clip_rect))
			paintCorner(graph, corner);
	}

	/**
	 * Paint the background that is covered by a corner between the navigation
	 * bars
	 * 
	 * @param graph
	 *            graphics to paint on
	 * @param area
	 *            area that has to be filled with corner color
	 */
	private void paintCorner(Graphics graph, Rectangle area) {
		graph.setColor(Color.CYAN);
		graph.fillRect(area.x, area.y, area.width, area.height);
		graph.setColor(Color.BLACK);
		graph.drawRect(area.x, area.y, area.width, area.height);
	}

	/**
	 * Paint the bars that can be used to move the map
	 * 
	 * @param graph
	 *            Graphics to paint the bars on
	 */
	private void paintNavigationBars(Graphics graph) {
		Rectangle clip_rect;
		Rectangle bounds;
		Rectangle bar = new Rectangle();

		/* --- Get size of the control --- */
		bounds = this.getBounds();

		/* --- Get the area that has to be repainted --- */
		clip_rect = graph.getClipBounds();

		/* --- Check the upper bar --- */
		bar.setBounds(NAV_BAR_SIZE, 0, bounds.width - 2 * NAV_BAR_SIZE - 1, NAV_BAR_SIZE);
		if (bar.intersects(clip_rect))
			paintNavBar(graph, bar, DIR.UP);

		/* --- Check the left bar --- */
		bar.setBounds(0, NAV_BAR_SIZE, NAV_BAR_SIZE, bounds.height - 2 * NAV_BAR_SIZE - 1);
		if (bar.intersects(clip_rect))
			paintNavBar(graph, bar, DIR.LEFT);

		/* --- Check the right bar --- */
		bar
				.setBounds(bounds.width - NAV_BAR_SIZE - 1, NAV_BAR_SIZE, NAV_BAR_SIZE, bounds.height - 2
						* NAV_BAR_SIZE - 1);
		if (bar.intersects(clip_rect))
			paintNavBar(graph, bar, DIR.RIGHT);

		/* --- Check the lower bar --- */
		bar
				.setBounds(NAV_BAR_SIZE, bounds.height - NAV_BAR_SIZE - 1, bounds.width - 2 * NAV_BAR_SIZE - 1,
						NAV_BAR_SIZE);
		if (bar.intersects(clip_rect))
			paintNavBar(graph, bar, DIR.DOWN);
	}

	/**
	 * Paint a navigation bar
	 * 
	 * @param graph
	 *            Graphics background
	 * @param bar
	 *            rectangle where the bar to paint
	 * @param direction
	 *            direction of the arrow on the bar
	 */
	private void paintNavBar(Graphics graph, Rectangle bar, DIR direction) {
		int mid_x, mid_y;
		int offset;

		/* --- Paint the empty bar --- */
		graph.setColor(Color.CYAN);
		graph.fillRect(bar.x, bar.y, bar.width, bar.height);
		graph.setColor(Color.BLACK);
		graph.drawRect(bar.x, bar.y, bar.width, bar.height);

		/* --- Find the middle of the bar --- */
		mid_x = bar.x + bar.width / 2;
		mid_y = bar.y + bar.height / 2;

		/* --- Calculate the offset from the middle to arrow corners --- */
		offset = (NAV_BAR_SIZE - 4) / 2;

		/* --- Draw the arrow around the middle --- */
		switch (direction) {
		case UP:
			graph.drawLine(mid_x - offset, mid_y + offset, mid_x, mid_y - offset);
			graph.drawLine(mid_x + offset, mid_y + offset, mid_x, mid_y - offset);
			break;

		case DOWN:
			graph.drawLine(mid_x - offset, mid_y - offset, mid_x, mid_y + offset);
			graph.drawLine(mid_x + offset, mid_y - offset, mid_x, mid_y + offset);
			break;

		case LEFT:
			graph.drawLine(mid_x + offset, mid_y - offset, mid_x - offset, mid_y);
			graph.drawLine(mid_x + offset, mid_y + offset, mid_x - offset, mid_y);
			break;

		case RIGHT:
			graph.drawLine(mid_x - offset, mid_y - offset, mid_x + offset, mid_y);
			graph.drawLine(mid_x - offset, mid_y + offset, mid_x + offset, mid_y);
			break;
		}
	}

	/**
	 * Paint the whole map area
	 * 
	 * @param graph
	 */
	private void paintMap(Graphics graph) {
		int current_x, current_y;
		Rectangle bounds;
		Rectangle tile_bounds;
		Rectangle clip_rect;
		Rectangle visible_bounds;
		Rectangle source_bounds;

		tile_bounds = new Rectangle();

		/* --- Get the rectangle that has to be painted --- */
		clip_rect = graph.getClipBounds();

		/* --- Get the size of the whole control --- */
		bounds = getBounds();

		/* --- Calculate position and dimensions of the map area --- */
		bounds.x = NAV_BAR_SIZE;
		bounds.y = NAV_BAR_SIZE;
		bounds.width -= 2 * NAV_BAR_SIZE;
		bounds.height -= 2 * NAV_BAR_SIZE;

		/* --- Iterate over all x and y until we leave the map area --- */
		for (current_x = 0; current_x * 256 - tileOffsetX < bounds.width; current_x++) {
			for (current_y = 0; current_y * 256 - tileOffsetY < bounds.height; current_y++) {
				/* --- Calculate the position of the tile on the screen --- */
				tile_bounds.x = NAV_BAR_SIZE + current_x * 256 - tileOffsetX;
				tile_bounds.y = NAV_BAR_SIZE + current_y * 256 - tileOffsetY;
				tile_bounds.width = 256;
				tile_bounds.height = 256;

				/* --- Ignore the tile if it is not in the repaint area --- */
				if (!clip_rect.intersects(tile_bounds))
					continue;

				/* --- Calculate the visible area of the tile --- */
				visible_bounds = tile_bounds.intersection(bounds);

				/*
				 * --- The visible bounds are relative to the upper left corner
				 * of the control. Move it to the upper left corner of the tile
				 */
				source_bounds = new Rectangle(visible_bounds);
				source_bounds.translate(0 - tile_bounds.x, 0 - tile_bounds.y);

				/* --- Load and paint the tile --- */
				loadAndPaint(current_x + originX, current_y + originY, graph, visible_bounds, source_bounds);
			}
		}
	}

	/**
	 * Load a tile from the web and paint it in the graphics screen
	 * 
	 * @param x
	 *            X-number of the tile
	 * @param y
	 *            Y-number of the tile
	 * @param graph
	 *            Graphics device to draw on
	 * @param dest
	 *            Destination Rectangle
	 * @param source
	 *            area in source rectangle
	 */
	private void loadAndPaint(int x, int y, Graphics graph, Rectangle dest, Rectangle source) {
		String filename;
		BufferedImage image;
		int maxtile;

		/* --- Build path and name of the image file --- */
		filename = String.format("%d/%d/%d.png", zoomLevel, x, y);

		synchronized (loadedImages) {

			/*
			 * --- Create a black image if the coordinates are outside the
			 * maximum ---
			 */
			maxtile = (int) Math.pow(2, zoomLevel);
			if (x >= maxtile || y >= maxtile)
				image = createBlack();
			else {
				/* --- Check if the image is already loaded --- */
				image = loadedImages.get(filename);
			}

			/*
			 * --- If not loaded, the request it if not already requested. Do
			 * not place requests, while we are an a drag operation ---
			 */
			if (image == null && dragStartX == 0) {
				synchronized (sync) {
					if (!requestedImages.contains(filename)) {
						requestedImages.addLast(filename);
						sync.notify();
					}
				}
			}

			/* --- If there is an image, the draw it --- */
			if (image != null) {
				graph.drawImage(image, dest.x, dest.y, dest.x + dest.width, dest.y + dest.height, source.x, source.y,
						source.x + source.width, source.y + source.height, null // Image
						// observer
						);
			} else {
				/* --- If there is no image, then just paint light gray --- */
				graph.setColor(Color.LIGHT_GRAY);
				graph.fillRect(dest.x, dest.y, dest.width, dest.height);
			}
		}
	}

	/**
	 * Background processing function. Load all requested tiles from the online
	 * service
	 */
	public void run() {
		String filename;
		BufferedImage img;

		/* --- Work until we get the stop signal --- */
		while (!mustStop) {
			/* --- Check if there is something to do --- */
			synchronized (sync) {
				try {
					if (requestedImages.isEmpty())
						sync.wait(500);
				} catch (InterruptedException e) {
					// Nothing
				}

				if (!requestedImages.isEmpty())
					filename = requestedImages.removeFirst();
				else
					continue;
			}

			/* --- Load file from the host service --- */
			try {
				img = ImageIO.read(new URL(host + filename));
			} catch (IOException e) {
				/* --- Create a black image --- */
				img = createBlack();

				// TODO: test only
				Graphics graph = img.getGraphics();
				graph.setColor(Color.WHITE);
				graph.drawString(filename, 10, 120);
				graph.drawRect(0, 0, 255, 255);
			}

			/* --- Add the block to the finished blocks --- */
			synchronized (loadedImages) {
				loadedImages.put(filename, img);

				/* --- Signal a repaint of the control --- */
				this.repaint();
			}
		}
	}

	/**
	 * create a black Tile
	 * 
	 * @return image of black square
	 */
	private BufferedImage createBlack() {
		BufferedImage img;

		/* --- Create a screen compatible environment --- */
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gs.getDefaultConfiguration();

		// Create an image that does not support transparency
		img = gc.createCompatibleImage(256, 256, Transparency.OPAQUE);

		/* --- Create a graphics context from the image --- */
		Graphics graph = img.getGraphics();

		/* --- Fill it with black --- */
		graph.setColor(Color.BLACK);
		graph.fillRect(0, 0, 256, 256);

		return img;
	}

	public void mouseClicked(MouseEvent event) {
		// Ignored
	}

	public void mouseEntered(MouseEvent arg0) {
		// ignored
	}

	public void mouseExited(MouseEvent arg0) {
		// ignored
	}

	public void mousePressed(MouseEvent event) {
		int x, y;
		Rectangle control;

		/*
		 * --- Get Mouse-Click coordinates. The coordinates are relative to the
		 * control ---
		 */
		x = event.getX();
		y = event.getY();

		/* --- get the size of the control --- */
		control = this.getBounds();

		/* --- Click on the left button --- */
		if (x < NAV_BAR_SIZE && y > NAV_BAR_SIZE && y < control.height - NAV_BAR_SIZE)
			setOriginX(originX - 1);

		/* --- Click on the right button --- */
		else if (x > control.width - NAV_BAR_SIZE && y > NAV_BAR_SIZE && y < control.height - NAV_BAR_SIZE)
			setOriginX(originX + 1);

		/* --- Click on the up button --- */
		else if (y < NAV_BAR_SIZE && x > NAV_BAR_SIZE && x < control.width - NAV_BAR_SIZE)
			setOriginY(originY - 1);

		/* --- Click on the down button --- */
		else if (y > control.height - NAV_BAR_SIZE && x > NAV_BAR_SIZE && x < control.width - NAV_BAR_SIZE)
			setOriginY(originY + 1);

		/* --- Otherwise it is the start or a drag operation --- */
		else {
			dragStartX = x;
			dragStartY = y;
			dragStartOffsetX = tileOffsetX;
			dragStartOffsetY = tileOffsetY;
		}
	}

	public void mouseReleased(MouseEvent event) {
		/*
		 * --- If it is the end of a drag operation, then set the new positions
		 * ---
		 */
		if (dragStartX != 0) {
			setOriginX(originX + (tileOffsetX / 256));
			setOriginY(originY + (tileOffsetY / 256));
			tileOffsetX %= 256;
			tileOffsetY %= 256;
			dragStartX = 0;

			/*
			 * --- If the tile offset is negative, then correct the offset, so
			 * we have a positive one ---
			 */
			if (tileOffsetX < 0 && originX > 0) {
				setOriginX(originX - 1);
				tileOffsetX += 256;
			}

			if (tileOffsetX < 0 && originX == 0)
				tileOffsetX = 0;

			if (tileOffsetY < 0 && originY > 0) {
				setOriginY(originY - 1);
				tileOffsetY += 256;
			}

			if (tileOffsetY < 0 && originY == 0) {
				tileOffsetY = 0;
			}
		}
	}

	public void mouseDragged(MouseEvent event) {
		int x, y;

		/* --- Get Current Mouse Position --- */
		x = event.getX();
		y = event.getY();

		/* --- Calculate tile offset from mouse position --- */
		tileOffsetX = dragStartOffsetX + dragStartX - x;
		tileOffsetY = dragStartOffsetY + dragStartY - y;

		this.repaint();
	}

	public void mouseMoved(MouseEvent event) {
		// Ignored
	}
}
