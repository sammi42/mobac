/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.gui.mapview.interfaces;

import mobac.gui.mapview.JMapViewer;
import mobac.gui.mapview.Tile;
import mobac.program.interfaces.MapSource;


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
