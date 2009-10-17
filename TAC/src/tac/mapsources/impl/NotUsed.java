package tac.mapsources.impl;

import tac.mapsources.AbstractMapSource;

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
}
