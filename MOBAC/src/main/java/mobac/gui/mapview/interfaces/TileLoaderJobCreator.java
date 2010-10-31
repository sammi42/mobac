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

import mobac.program.interfaces.MapSource;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * Interface for implementing an asynchronous tile loader. Tiles are usually
 * loaded via HTTP or from a file.
 * 
 * @author Jan Peter Stotz
 */
public interface TileLoaderJobCreator {

	/**
	 * A typical {@link #createTileLoaderJob(int, int, int)} implementation
	 * should create and return a new {@link Job} instance that performs the
	 * load action.
	 * 
	 * @param mapSource
	 * @param tilex
	 * @param tiley
	 * @param zoom
	 * @returns {@link Runnable} implementation that performs the desired load
	 *          action.
	 */
	public Runnable createTileLoaderJob(MapSource mapSource, int tilex, int tiley, int zoom);
}
