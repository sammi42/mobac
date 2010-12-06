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
package mobac.mapsources.mappacks.region_europe_north;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class EniroComMap extends AbstractHttpMapSource {

	private String mapType;

	public EniroComMap() {
		this("map");
	}

	protected EniroComMap(String mapType) {
		super("Eniro.com-" + mapType, 0, 20, TileImageType.PNG, HttpMapSource.TileUpdate.IfModifiedSince);
		this.mapType = mapType;
	}

	@Override
	public String toString() {
		return "Eniro Map (SE, NO, FI)";
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		int y = (1 << zoom) - 1 - tiley;
		return "http://map.eniro.com/geowebcache/service/tms1.0.0/" + mapType + "/" + zoom + "/" + tilex + "/" + y
				+ ".png";
	}
}
