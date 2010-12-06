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
package mobac.mapsources.mappacks.region_europe_dach;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class OutdooractiveGermany extends AbstractHttpMapSource {

	private static int SERVER_NUM = 0;

	protected String mapName = "portal";

	public OutdooractiveGermany() {
		super("Outdooractive.com", 8, 17, TileImageType.PNG, HttpMapSource.TileUpdate.LastModified);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		if (zoom < 8)
			throw new RuntimeException("Zoom level not suported");
		String s = "http://t" + SERVER_NUM + ".outdooractive.com/" + mapName + "/map/" + zoom + "/" + tilex + "/"
				+ tiley + ".png";
		SERVER_NUM = (SERVER_NUM + 1) % 4;
		return s;
	}

	@Override
	public String toString() {
		return "Outdooractive.com (Germany only)";
	}

}
