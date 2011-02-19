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
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.Settings;
import mobac.program.model.TileImageType;

/**
 * Uses QuadTree coordinate system for addressing a tile. See <a
 * href="http://msdn.microsoft.com/en-us/library/bb259689.aspx">Virtual Earth Tile System</a> for details.
 */
public abstract class AbstractMicrosoft extends AbstractHttpMapSource {

	protected String urlBase = ".ortho.tiles.virtualearth.net/tiles/";
	protected String urlAppend = "?g=45";
	protected int serverNum = 0;
	protected int serverNumMax = 4;
	protected char mapTypeChar;

	public AbstractMicrosoft(String name, TileImageType tileType, char mapTypeChar, HttpMapSource.TileUpdate tileUpdate) {
		super(name, 1, 19, tileType, tileUpdate);
		this.mapTypeChar = mapTypeChar;
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
		serverNum = (serverNum + 1) % serverNumMax;
		String lang = "&mkt=" + Settings.getInstance().bingLanguage;
		return "http://" + mapTypeChar + serverNum + urlBase + mapTypeChar + tileNum + "." + tileType + urlAppend
				+ lang;
	}

	@Override
	public String toString() {
		return getName();
	}
}
