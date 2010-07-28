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
package org.openstreetmap.gui.jmapviewer.interfaces;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.Graphics2D;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 * Interface to be implemented by all elements that can be displayed on the map.
 * 
 * @author Jan Peter Stotz
 * @see JMapViewer#addMapMarker(MapMarker)
 * @see JMapViewer#getMapMarkerList()
 */
public interface MapMarker {

	/**
	 * @return Latitude of the map marker position
	 */
	public double getLat();

	/**
	 * @return Longitude of the map marker position
	 */
	public double getLon();

	/**
	 * Paints the map marker on the map. The <code>position</code> specifies the
	 * coordinates within <code>g</code>
	 * 
	 * @param g
	 * @param position
	 */
	public void paint(Graphics2D g, Point position);
}
