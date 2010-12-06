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

public class MicrosoftOrdnanceSurveyExplorer extends AbstractHttpMapSource {

	public MicrosoftOrdnanceSurveyExplorer() {
		super("Ordnance Survey Explorer Maps (UK)", 12, 16, TileImageType.PNG, HttpMapSource.TileUpdate.IfModifiedSince);
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
		String urlAppend = "?g=41&productSet=mmOS";
		return "http://ecn.t2.tiles.virtualearth.net/tiles/r" + tileNum + "." + tileType + urlAppend;
	}

}
