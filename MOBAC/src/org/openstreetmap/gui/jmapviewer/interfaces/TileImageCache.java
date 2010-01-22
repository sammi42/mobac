package org.openstreetmap.gui.jmapviewer.interfaces;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Tile;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * Implement this interface for creating a synchronous tile image cache for
 * {@link JMapViewer}. Synchronous in this context means that {@link JMapViewer}
 * expects the result of {@link #getTile(MapSource, int, int, int)} as fast as
 * possible (only some milliseconds). Otherwise map painting will be delayed.
 */
public interface TileImageCache {

	/**
	 * Retrieves a tile from the cache if present, otherwise <code>null</code>
	 * will be returned.
	 * 
	 * @param source
	 * @param x
	 *            tile number on the x axis of the tile to be retrieved
	 * @param y
	 *            tile number on the y axis of the tile to be retrieved
	 * @param z
	 *            zoom level of the tile to be retrieved
	 * @return the requested tile or <code>null</code> if the tile is not
	 *         present in the cache
	 */
	public Tile getTile(MapSource source, int x, int y, int z);

	/**
	 * Adds a tile to the cache. How long after adding a tile can be retrieved
	 * via {@link #getTile(int, int, int)} is unspecified and depends on the
	 * implementation.
	 * 
	 * @param tile
	 */
	public void addTile(Tile tile);

	/**
	 * @return the number of tiles hold by the cache
	 */
	public int getTileCount();
}
