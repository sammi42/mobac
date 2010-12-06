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
package mobac.mapsources.mappacks.region_europe_west;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class MultimapOSUkCom extends AbstractHttpMapSource {

	public MultimapOSUkCom() {
		// zoom level supported:
		// 0 (fixed url) world.png
		// 1-5 "mergend binary encoding"
		// 6-? uses MS MAP tiles at some parts of the world
		super("Multimap UK OS Map", 1, 16, TileImageType.PNG, HttpMapSource.TileUpdate.IfModifiedSince);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
		if (tileNum.length() > 12)
			tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6, 12) + "/" + tileNum.substring(12);
		else if (tileNum.length() > 6)
			tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6);

		String base;
		if (zoom < 6)
			base = "http://mc1.tiles-cdn.multimap.com/ptiles/map/mi915/";
		else if (zoom < 14)
			base = "http://mc2.tiles-cdn.multimap.com/ptiles/map/mi917/";
		else if (zoom < 15)
			base = "http://mc0.tiles-cdn.multimap.com/ptiles/map/mi904/";
		else
			base = "http://mc0.tiles-cdn.multimap.com/ptiles/map/mi932/";

		tileNum = base + (zoom + 1) + "/" + tileNum + ".png?client=public_api&service_seq=14458";
		return tileNum;
	}
}
