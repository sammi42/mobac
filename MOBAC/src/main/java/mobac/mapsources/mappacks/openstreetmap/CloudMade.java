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

import mobac.exceptions.MapSourceInitializationException;
import mobac.mapsources.MapSourceUrlUpdater;
import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.AbstractOsmTileSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.utilities.Charsets;

public class CloudMade extends AbstractOsmTileSource {

	private static final String INIT_REGEX = "\"api_key\"\\:\"([A-F0-9]+)\"";

	private final String styleID;

	private String apiKey = "";

	private static final String PATTERN = "http://%s.tile.cloudmade.com/%s/%s/256/%d/%d/%d.png";

	private static final String[] SERVER = { "a", "b", "c" };

	private int SERVER_NUM = 0;

	public CloudMade(String styleID) {
		super("OSM Cloutmade " + styleID);
		this.styleID = styleID;
		this.maxZoom = 18;
		this.tileUpdate = HttpMapSource.TileUpdate.ETag;
	}

	public CloudMade() {
		this("1");
	}

	@Override
	protected void initernalInitialize() throws MapSourceInitializationException {
		apiKey = MapSourceUrlUpdater.loadDocumentAndExtractGroup("http://maps.cloudmade.com/", Charsets.UTF_8,
				INIT_REGEX);
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		String url = String.format(PATTERN, new Object[] { SERVER[SERVER_NUM], apiKey, styleID, zoom, tilex, tiley });
		SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
		return url;
	}

	@Override
	public String toString() {
		return "OpenStreetMap Couldmate Style " + styleID;
	}

}
