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
package mobac.mapsources.mappacks.region_europe_south;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.model.TileImageType;

/**
 * https://sourceforge.net/tracker/?func=detail&aid=3071972&group_id=238075&atid=1105496
 */
public class Sigpac extends AbstractHttpMapSource {

	private static String sources[] = { "", "", "", "", "", "MTNSIGPAC", "MTNSIGPAC", "MTN2000", "MTN2000",
			"MTN2000", "MTN2000", "MTN200", "MTN200", "MTN200", "MTN25", "MTN25", "ORTOFOTOS", "ORTOFOTOS" };

	public Sigpac() {
		// In some places ORTOFOTOS reaches zoom 18,
		// but only level 17 covers the entire country
		super("SIGPAC", 5, 17, TileImageType.JPG);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		int j = (1 << zoom) - tiley - 1;

		// The tiles are downloaded from kmlserver interface,
		// as tilesserver.mapa.es serves only UTM projections
		return "http://sigpac.mapa.es/kmlserver/raster/" + sources[zoom] + "@3785/" + zoom + "." + tilex + "." + j
				+ ".img";
	}

	@Override
	public String toString() {
		return "SIGPAC Mercator (Spain only)";
	}
}
