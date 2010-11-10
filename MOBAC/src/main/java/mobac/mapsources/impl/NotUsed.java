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
package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.program.model.TileImageType;

public class NotUsed {

	public static class MapSurfer extends AbstractMapSource {
		public static final String URL = "http://tiles1.mapsurfer.net/tms_r.ashx?";

		public MapSurfer() {
			super("MapSurfer", 0, 19, TileImageType.PNG);
			tileUpdate = TileUpdate.LastModified;
		}

		@Override
		public String toString() {
			return "MapSurfer.net";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return URL + "x=" + tilex + "&y=" + tiley + "&z=" + zoom;
		}

	}

	/**
	 * hubermedia http://maps.hubermedia.de/
	 */
	public static class Hubermedia extends AbstractMapSource {

		String mapUrl;

		public Hubermedia() {
			super("Hubermedia", 12, 15, TileImageType.PNG, TileUpdate.IfNoneMatch);
			mapUrl = "http://t1.hubermedia.de/TK50/AT/Kompass_Neu//Z{$z}/{$y}/{$x}.png";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MapSourceTools.formatMapUrl(mapUrl, zoom, tilex, tiley);
		}

	}
}
