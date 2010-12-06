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

import java.util.Locale;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * http://www.topomapper.com/
 * 
 * @author "leo-kn"
 */
public class Topomapper extends AbstractHttpMapSource {

	private static final String URL = "http://78.46.61.141/cgi-bin/tilecache-2.10/tilecache.py?"
			+ "LAYERS=topomapper_gmerc&SERVICE=WMS&BBOX=%6f,%6f,%6f,%6f";

	public Topomapper() {
		super("Topomapper.com", 0, 13, TileImageType.JPG, HttpMapSource.TileUpdate.None);
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {

		double f = 40075018.6855784862 / Math.pow(2, zoom);

		double x1 = -20037508.3427892431 + tilex * f;
		double x2 = -20037508.3427892431 + (tilex + 1) * f;
		double y1 = 20037508.3427892431 - (tiley + 1) * f;
		double y2 = 20037508.3427892431 - (tiley + 2) * f;

		return String.format(Locale.ENGLISH, URL, x1, y1, x2, y2);
	}
}
