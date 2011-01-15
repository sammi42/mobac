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
import mobac.program.model.TileImageType;

/**
 * http://vfr-bulletin.de/web20/index.htm
 * http://sourceforge.net/tracker/?func=detail&atid=1105497&aid=3139029&group_id=238075
 * 
 * @author xtra-nice (Chris)
 */
public class ICAOMapsGermany extends AbstractHttpMapSource {

	public ICAOMapsGermany() {
		super("ICAOMapsGermany", 4, 11, TileImageType.JPG, TileUpdate.IfNoneMatch);
	}

	@Override
	public String toString() {
		return "ICAO Maps (Germany)";
	}

	@Override
	public String getTileUrl(int zoom, int x, int y) {
		int tms_y = -1 * (y - ((int) Math.pow(2.0, zoom) - 1));
		return "http://vfr-bulletin.de/maps/ICAO/" + zoom + "/" + x + "/" + tms_y + ".jpg";
	}
}
