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
package mobac.mapsources.mappacks.region_europe_east;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * http://map.navitel.su
 * 
 * @version 1.1
 * @author Andrey Raygorodskiy (andrey(dot)raygorodskiy(at)gmail(dot)com)
 * @author r_x
 */
public class Navitel extends AbstractHttpMapSource {

	private static final String BASE_URL = "http://maps.navitel.su/navitms.fcgi?t=%08d,%08d,%02d";

	public Navitel() {
		super("Navitel.su", 3, 17, TileImageType.PNG, HttpMapSource.TileUpdate.None);
	}

	@Override
	public String toString() {
		return "Navitel (Russian)";
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		tiley = (1 << zoom) - tiley - 1;
		return String.format(BASE_URL, tilex, tiley, zoom);
	}
}
