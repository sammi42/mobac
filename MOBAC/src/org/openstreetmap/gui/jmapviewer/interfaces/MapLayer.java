package org.openstreetmap.gui.jmapviewer.interfaces;

import java.awt.Graphics2D;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 * General purpose map layer
 */
public interface MapLayer {

	/**
	 * 
	 * @param map
	 * @param g
	 * @param zoom
	 *            current zoom level
	 * @param minX
	 *            top left x coordinate of the visible map region
	 * @param minYtop
	 *            left y coordinate of the visible map region
	 * @param maxX
	 *            bottom right x coordinate of the visible map region
	 * @param maxY
	 *            bottom right y coordinate of the visible map region
	 */
	public void paint(JMapViewer map, Graphics2D g, int zoom, int minX, int minY, int maxX, int maxY);

}
