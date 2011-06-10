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
package mobac.mapsources.mappacks.mymappack;

import java.net.HttpURLConnection;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.download.UserAgent;
import mobac.program.model.TileImageType;

/**
 * Example map source template.
 */
public class MyMapsourceTemplate extends AbstractHttpMapSource {

	/**
	 * Example URL - change to a valid base URL
	 */
	private static final String MAP_BASE_URL = "http://www.myosmtileserver.example/Tiles/tile";

	public MyMapsourceTemplate() {
		super("MyMapSource", 0, 17, TileImageType.PNG, TileUpdate.None);
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		return MAP_BASE_URL + "/" + zoom + "/" + tilex + "/" + tiley + ".png";
	}

	@Override
	protected void prepareTileUrlConnection(HttpURLConnection conn) {
		super.prepareTileUrlConnection(conn);
		// conn.setRequestProperty("User-agent", UserAgent.FF3_WIN7);
	}

	@Override
	public String toString() {
		return "My map source";
	}

}
