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
package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

/**
 * A simple implementation of the {@link MapMarker} interface. Each map marker
 * is painted as a circle with a black border line and filled with a specified
 * color.
 * 
 * @author Jan Peter Stotz
 * 
 */
public class MapMarkerDot implements MapMarker {

	protected double lat;
	protected double lon;
	protected Color color;

	public MapMarkerDot(double lat, double lon) {
		this(Color.YELLOW, lat, lon);
	}

	public MapMarkerDot(Color color, double lat, double lon) {
		super();
		this.color = color;
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public void paint(Graphics2D g, Point position) {
		int size_h = 5;
		int size = size_h * 2;
		g.setColor(color);
		g.fillOval(position.x - size_h, position.y - size_h, size, size);
		g.setColor(Color.BLACK);
		g.drawOval(position.x - size_h, position.y - size_h, size, size);
	}

	@Override
	public String toString() {
		return "MapMarker at " + lat + " " + lon;
	}

}
