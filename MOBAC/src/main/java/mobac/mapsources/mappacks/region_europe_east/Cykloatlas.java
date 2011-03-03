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

import java.net.HttpURLConnection;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * CykloServer http://www.cykloserver.cz/cykloatlas/index.php
 */
public class Cykloatlas extends AbstractHttpMapSource {

	public Cykloatlas() {
		super("Cykloatlas", 7, 16, TileImageType.PNG, HttpMapSource.TileUpdate.LastModified);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		String z = Integer.toString(zoom);
		if (zoom >= 13)
			z += "c";
		return "http://services.tmapserver.cz/tiles/gm/shc/" + z + "/" + tilex + "/" + tiley + ".png";
	}

	@Override
	protected void prepareTileUrlConnection(HttpURLConnection conn) {
		super.prepareTileUrlConnection(conn);
		conn.addRequestProperty("Referer", "http://www.cykloserver.cz/cykloatlas/");
	}

	@Override
	public String toString() {
		return getName() + " (CZ, SK)";
	}

}
