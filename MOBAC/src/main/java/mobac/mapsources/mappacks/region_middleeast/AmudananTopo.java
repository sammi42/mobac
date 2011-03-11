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
package mobac.mapsources.mappacks.region_middleeast;

import java.net.HttpURLConnection;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.model.TileImageType;

/**
 * 
 * http://amudanan.co.il
 * 
 * https://sourceforge.net/tracker/?func=detail&atid=1105497&aid=3205769&group_id=238075
 * 
 * @author yizhak (sf.net user)
 */
public class AmudananTopo extends AbstractHttpMapSource {

	public AmudananTopo() {
		super("AmudananTopo", 7, 15, TileImageType.PNG, TileUpdate.None);
	}

	@Override
	public String getTileUrl(int zoom, int x, int y) {
		return "http://amudanan.co.il/tiles/I50_0908_4b/Z" + zoom + "/" + y + "/" + x + ".png";
	}

	@Override
	protected void prepareTileUrlConnection(HttpURLConnection conn) {
		super.prepareTileUrlConnection(conn);
		conn.setRequestProperty("Referer", "http://amudanan.co.il/");
	}

	@Override
	public String toString() {
		return "Isreal Topo (Amudanan)";
	}

}
