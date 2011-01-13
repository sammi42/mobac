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
import mobac.program.model.TileImageType;

/**
 * http://www.arcgis.com/home/webmap/viewer.html?useExisting=1
 */
public class ArcGISSatellite extends AbstractHttpMapSource {

	private static final String BASE_URL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/";

	public ArcGISSatellite() {
		super("ArcGISSatellite", 0, 19, TileImageType.JPG, TileUpdate.IfModifiedSince);
	}

	public String getTileUrl(int zoom, int x, int y) {
		return BASE_URL + zoom + "/" + y + "/" + x;
	}

	@Override
	public String toString() {
		return "ArcGIS Satellite";
	}

}
