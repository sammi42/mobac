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
package mobac.mapsources.mappacks.bing;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.program.model.Settings;
import mobac.program.model.TileImageType;

public class BingHillshade extends AbstractHttpMapSource {

	// http://ecn.t2.tiles.virtualearth.net/tiles/r12020322?g=637&mkt=de-de&lbl=l1&stl=h&shading=hill&n=z
	protected int serverNum = 0;
	protected int serverNumMax = 4;

	public BingHillshade() {
		super("Bing Maps", 1, 19, TileImageType.JPG, TileUpdate.IfModifiedSince);
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
		serverNum = (serverNum + 1) % serverNumMax;
		String lang = Settings.getInstance().bingLanguage;
		return "http://ecn.t" + serverNum + ".tiles.virtualearth.net/tiles/r" + tileNum + "?g=637&mkt=" + lang
				+ "&lbl=l1&stl=h&shading=hill&n=z";
	}

}
