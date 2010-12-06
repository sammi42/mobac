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
/**
 * 
 */
package mobac.mapsources.mappacks.region_asia;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class YahooMapsTaiwan extends AbstractHttpMapSource {

	public YahooMapsTaiwan() {
		super("Yahoo Maps Taiwan", 1, 18, TileImageType.PNG, HttpMapSource.TileUpdate.None);
	}

	public String getTileUrl(int zoom, int x, int y) {
		int yahooY = (((1 << zoom) - 2) / 2) - y;
		int yahooZoom = 16 - zoom + 2;
		return "http://l.yimg.com/kp/tl?x=" + x + "&y=" + yahooY + "&z=" + yahooZoom;
	}
}
