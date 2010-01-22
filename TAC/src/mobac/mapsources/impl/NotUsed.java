package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MapSourceTools;

public class NotUsed {

	public static class MapSurfer extends AbstractMapSource {
		public static final String URL = "http://tiles1.mapsurfer.net/tms_r.ashx?";

		public MapSurfer() {
			super("MapSurfer", 0, 19, "png");
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
			super("Hubermedia", 12, 15, "png", TileUpdate.IfNoneMatch);
			mapUrl = "http://t1.hubermedia.de/TK50/AT/Kompass_Neu//Z{$z}/{$y}/{$x}.png";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MapSourceTools.formatMapUrl(mapUrl, zoom, tilex, tiley);
		}

	}
}
