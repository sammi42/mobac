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

public class YahooMapsJapan extends AbstractHttpMapSource {

	public YahooMapsJapan() {
		super("Yahoo Maps Japan", 1, 19, TileImageType.PNG, HttpMapSource.TileUpdate.IfModifiedSince);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		int yahooY = (((1 << zoom) - 2) / 2) - tiley;
		int yahooZoom = zoom + 1;
		return "http://ta.map.yahoo.co.jp/yta/map?v=4.3&r=1&x=" + tilex + "&y=" + yahooY + "&z=" + yahooZoom;
	}

}
