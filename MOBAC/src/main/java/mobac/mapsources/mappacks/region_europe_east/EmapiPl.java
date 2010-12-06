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

import java.io.IOException;
import java.net.HttpURLConnection;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * Emapi - mapa internetowa firmy Emapa
 * <p>
 * <a href="http://emapi.pl/">emapi.pl</a>
 * </p>
 */
public class EmapiPl extends AbstractHttpMapSource {

	int[] servernums = { 1, 2, 3, 4 };

	int selectedServer = 0;

	public EmapiPl() {
		super("EmapiPl", 0, 19, TileImageType.PNG, HttpMapSource.TileUpdate.None);
	}

	public String getTileUrl(int zoom, int x, int y) {
		selectedServer = (selectedServer++) % servernums.length;
		return "http://img" + servernums[selectedServer] + ".emapi.pl/Default.aspx?tileX=" + x + "&tileY=" + y
				+ "&zoom=" + zoom + "&layer=std&fun=GetMap&userID=pasat";

	}

	@Override
	public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley) throws IOException {
		HttpURLConnection conn = super.getTileUrlConnection(zoom, tilex, tiley);
		conn.addRequestProperty("Cookie", "currentView=");
		conn.addRequestProperty("Referer", "http://emapi.pl/?referer=");
		return conn;
	}

	@Override
	public String toString() {
		return "Emapi.pl (Poland only)";
	}
}
