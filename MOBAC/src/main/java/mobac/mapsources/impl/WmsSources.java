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
package mobac.mapsources.impl;

import mobac.program.interfaces.MapSource;

public class WmsSources {

	static final double ORIGIN_SHIFT = Math.PI * 6378137; // 20037508.3427892430765884088807

	public static long[] tileToMeters(MapSource mapSource, int zoom, int px, int py) {
		// "Converts pixel coordinates in given zoom level of pyramid to EPSG:900913"

		// 2 * math.pi * 6378137 / self.tileSize
		double res = 2 * Math.PI * 6378137 / (2 << zoom);

		System.out.println(res);

		double mx = px * res - ORIGIN_SHIFT;
		double mx2 = (px + 1) * res - ORIGIN_SHIFT;
		double my = py * res - ORIGIN_SHIFT;
		double my2 = (py + 1) * res - ORIGIN_SHIFT;

		return new long[] { (long) mx, (long) my, (long) mx2, (long) my2 };
	}

}
