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
package mobac.mapsources.mappacks.region_america_north;

import java.io.IOException;
import java.net.HttpURLConnection;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * 
 * http://www.mytopo.com/maps/
 * 
 * Funny: The URL indicates PNG images but the server provides JPEG files...
 * 
 */
public class MyTopo extends AbstractHttpMapSource {

	public MyTopo() {
		super("MyTopo", 6, 16, TileImageType.JPG, HttpMapSource.TileUpdate.None);
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		return "http://maps.mytopo.com/mytopoK55Zc3L/tilecache.py/1.0.0/topoG/" + zoom + "/" + tilex + "/" + tiley
				+ ".jpg";
	}

	@Override
	public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley) throws IOException {
		HttpURLConnection conn = super.getTileUrlConnection(zoom, tilex, tiley);
		conn.addRequestProperty("Referer", "http://www.mytopo.com/maps/");
		return conn;
	}

	@Override
	public String toString() {
		return "MyTopo (USA only)";
	}

}
