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
package mobac.mapsources.mappacks.google;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.UpdatableMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class GoogleMapSource extends AbstractHttpMapSource implements UpdatableMapSource {

	public static String LANG = "en";

	private int serverNum = 0;

	public String serverUrl;

	public GoogleMapSource(String name, int minZoom, int maxZoom, TileImageType tileType,
			HttpMapSource.TileUpdate tileUpdate) {
		super(name, minZoom, maxZoom, tileType, tileUpdate);
		update();
	}

	public void update() {
		serverUrl = MapSourceTools.loadMapUrl(this, "url");
	}

	protected int getNextServerNum() {
		int x = serverNum;
		serverNum = (serverNum + 1) % 4;
		return x;
	}

	public String getTileUrl(int zoom, int x, int y) {
		String tmp = serverUrl;
		tmp = tmp.replace("{$servernum}", Integer.toString(getNextServerNum()));
		tmp = tmp.replace("{$lang}", LANG);
		tmp = tmp.replace("{$x}", Integer.toString(x));
		tmp = tmp.replace("{$y}", Integer.toString(y));
		tmp = tmp.replace("{$z}", Integer.toString(zoom));
		return tmp;
	}

}
