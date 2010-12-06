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
import mobac.program.model.TileImageType;

/**
 * 
 * Requires known user agent, and something else otherwise we get only a HTTP 403
 * <p>
 * map provider does not work --> currently unused
 * </p>
 */
public class TuristikaMapSk extends AbstractHttpMapSource {

	public TuristikaMapSk() {
		super("TuristikaMapSk (Slovakia)", 12, 15, TileImageType.PNG);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		String sx = String.format("%09d", tilex);
		String sy = String.format("%09d", tiley);
		sx = sx.substring(0, 3) + "/" + sx.substring(3, 6) + "/" + sx.substring(6, 9);
		sy = sy.substring(0, 3) + "/" + sy.substring(3, 6) + "/" + sy.substring(6, 9);

		String s = "http://www.turistickamapa.sk/tiles/sr50/" + zoom + "/" + sx + "/" + sy + ".png";
		System.out.println(s);
		return s;
	}

}
