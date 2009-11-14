package org.openstreetmap.gui.jmapviewer.interfaces;

import java.awt.Graphics;

public interface MapTileLayer {

	public void startPainting(MapSource mapSource);
	
	/**
	 * Paints the tile identified by <code>tilex</code>/<code>tiley</code>/
	 * <code>zoom</code> onto the {@link Graphics} <code>g</code> with it's
	 * upper left corner at <code>gx</code>/<code>gy</code>. The size of each
	 * tile has to be 256 pixel x 256 pixel.
	 * 
	 * @param g
	 * @param gx
	 * @param gy
	 * @param tilex
	 * @param tiley
	 */
	public void paintTile(Graphics g, int gx, int gy, int tilex, int tiley, int zoom);
}
