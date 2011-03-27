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
package mobac.mapsources.mappacks.openstreetmap;

import mobac.program.interfaces.HttpMapSource;

public class MapQuest extends AbstractOsmMapSource {
	
	private static final String BASE_URL = "http://otile1.mqcdn.com/tiles/1.0.0/osm/";

	public MapQuest() {
		super("MapQuest");
		this.maxZoom = 18;
		this.tileUpdate = HttpMapSource.TileUpdate.IfModifiedSince;
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		return BASE_URL + super.getTileUrl(zoom, tilex, tiley);
	}

	@Override
	public String toString() {
		return "OpenStreetMap MapQuest";
	}

}
