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
package mobac.mapsources.mappacks.misc_worldwide;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class YahooMaps extends AbstractHttpMapSource {

	public YahooMaps() {
		super("Yahoo Maps", 1, 16, TileImageType.JPG, HttpMapSource.TileUpdate.IfModifiedSince);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		int yahooTileY = (((1 << zoom) - 2) / 2) - tiley;
		int yahooZoom = getMaxZoom() - zoom + 2;
		return "http://maps.yimg.com/hw/tile?locale=en&imgtype=png&yimgv=1.2&v=4.1&x=" + tilex + "&y=" + yahooTileY
				+ "+6163&z=" + yahooZoom;
	}

}
