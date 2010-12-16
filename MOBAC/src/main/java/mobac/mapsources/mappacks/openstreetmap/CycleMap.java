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
package mobac.mapsources.mappacks.openstreetmap;

import mobac.program.interfaces.HttpMapSource;

public class CycleMap extends AbstractOsmTileSource {

	private static final String PATTERN = "http://%s.andy.sandbox.cloudmade.com/tiles/cycle/%d/%d/%d.png";

	private static final String[] SERVER = { "a", "b", "c" };

	private int SERVER_NUM = 0;

	public CycleMap() {
		super("OSM Cycle Map");
		this.maxZoom = 17;
		this.tileUpdate = HttpMapSource.TileUpdate.ETag;
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		String url = String.format(PATTERN, new Object[] { SERVER[SERVER_NUM], zoom, tilex, tiley });
		SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
		return url;
	}

	@Override
	public String toString() {
		return "OpenStreetMap Cyclemap";
	}

}
