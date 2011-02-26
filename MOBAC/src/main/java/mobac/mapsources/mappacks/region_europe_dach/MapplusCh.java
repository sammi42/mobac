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

import java.net.HttpURLConnection;
import java.util.Random;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * http://www.mapplus.ch/
 */
public class MapplusCh extends AbstractHttpMapSource {

	String referer;

	private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static final int[] SERVER_NUMS = { 1, 2, 3 };

	private int serverNum = 0;

	public MapplusCh() {
		super("MapplusCh", 7, 16, TileImageType.JPG, HttpMapSource.TileUpdate.ETag);
		char[] sessID = new char[32];
		Random rnd = new Random();
		for (int i = 0; i < sessID.length; i++)
			sessID[i] = hex[rnd.nextInt(hex.length)];
		// example sessID = "12ea56827487e927d4b202ad48248109";
		referer = "http://www.mapplus.ch/NeapoljsMapPage.php?uid=public&group=public&sessID=" + new String(sessID);
	}

	@Override
	public String toString() {
		return "Map+ (Switzerland)";
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		serverNum = (serverNum++) % SERVER_NUMS.length;
		int server = SERVER_NUMS[serverNum];
		return "http://mp" + server + ".mapplus.ch/tydcache/tydswisstopo/" + zoom + "/" + tilex + "/" + tiley + ".jpg";
	}

	@Override
	protected void prepareTileUrlConnection(HttpURLConnection conn) {
		super.prepareTileUrlConnection(conn);
		// http request property "Referer" is required - otherwise we only get "tranparentpixel.gif"
		conn.setRequestProperty("Referer", referer);
	}

}
