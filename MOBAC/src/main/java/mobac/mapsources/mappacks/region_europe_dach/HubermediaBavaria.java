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

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * Hubermedia Bavaria map http://maps.hubermedia.de/
 */
public class HubermediaBavaria extends AbstractHttpMapSource {

	String[] mapUrls;

	int serverNum = 0;

	public HubermediaBavaria() {
		super("Hubermedia Bavaria", 10, 16, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
		mapUrls = new String[17];

		mapUrls[10] = "http://t0.hubermedia.de/TK500/DE/Bayern/";
		mapUrls[11] = mapUrls[10];
		mapUrls[12] = "http://t{$servernum}.wms.hubermedia.de/tk200/de/bayern//Z{$z}/{$y}/{$x}.png";
		mapUrls[13] = "http://t{$servernum}.hubermedia.de/TK50/DE/Bayern//Z{$z}/{$y}/{$x}.png";
		mapUrls[14] = mapUrls[13];
		mapUrls[15] = "http://t{$servernum}.hubermedia.de/TK25/DE/Bayern//Z{$z}/{$y}/{$x}.png";
		mapUrls[16] = "http://t{$servernum}.hubermedia.de/DOK/DE/Bayern//Z{$z}/{$y}/{$x}.png";
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		serverNum = (serverNum + 1) % 3;
		if (zoom >= 12) {
			return MapSourceTools.formatMapUrl(mapUrls[zoom], serverNum, zoom, tilex, tiley);
		} else {
			String tc = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
			return mapUrls[zoom] + zoom + "/" + tc + ".png";
		}
	}
}
