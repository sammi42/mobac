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
package mobac.mapsources.mappacks.region_europe_dach;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * 
 * 
 *
 */
public class Bergfex extends AbstractHttpMapSource {

	/**
	 * 2009-02-20: server 4 causes some problems - commented out
	 */
	static final byte[] SERVER_IDS = { 4, 5 };

	int SERVERNUM = 0;

	public Bergfex() {
		super("Bergfex", 8, 15, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
	}

	@Override
	public String toString() {
		return "Bergfex (Austria)";
	}

	@Override
	public String getTileUrl(int zoom, int x, int y) {
		String baseurl = "http://static" + SERVER_IDS[SERVERNUM] + ".bergfex.at/images/amap/";
		SERVERNUM = (SERVERNUM + 1) % SERVER_IDS.length;
		String xBase = "";
		if (zoom > 13)
			xBase = Integer.toString(x).substring(0, zoom - 12) + "/";
		return baseurl + zoom + "/" + xBase + zoom + "_" + x + "_" + y + ".png";
	}
}
