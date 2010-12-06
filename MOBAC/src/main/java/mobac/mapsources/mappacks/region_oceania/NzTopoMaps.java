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
package mobac.mapsources.mappacks.region_oceania;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * http://www.nztopomaps.com
 * 
 * @author Tobias Wulff (camel69)
 */
public class NzTopoMaps extends AbstractHttpMapSource {

	public NzTopoMaps() {
		super("New Zealand Topographic Maps", 7, 15, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
	}

	public String getTileUrl(int zoom, int x, int y) {
		// nzy = 2^zoom - 1 - y
		int nzy = (1 << zoom) - 1 - y;
		return "http://cx.nztopomaps.com/" + zoom + "/" + x + "/" + nzy + ".png";
	}

	@Override
	public String toString() {
		return "New Zealand Topographic Maps";
	}

}
