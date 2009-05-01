package org.openstreetmap.gui.jmapviewer;

import java.awt.Graphics;

import org.openstreetmap.gui.jmapviewer.interfaces.MapTileLayer;

/**
 * A simple layer that paints the tile borders.
 */
public class MapGridLayer implements MapTileLayer {

	public void paintTile(Graphics g, int gx, int gy, int tilex, int tiley, int zoom) {
		g.drawRect(gx, gy, Tile.SIZE, Tile.SIZE);
	}

}
