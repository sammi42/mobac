package tac.mapsources.impl;

import tac.mapsources.AbstractMapSource;

public class UnusedMapSources {

	public static class OpenArialMap extends AbstractMapSource {
	
		public OpenArialMap() {
			super("OpenArialMap", 0, 18, "jpg");
		}
	
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://tile.openaerialmap.org/tiles/1.0.0/openaerialmap-900913/" + zoom + "/"
					+ tilex + "/" + tiley + ".jpg";
		}
	
	}

	public static class MapPlus extends AbstractMapSource {
	
		public MapPlus() {
			super("Map+ (Swiss only)", 7, 16, "jpg");
		}
	
		public String getTileUrl(int zoom, int tilex, int tiley) {
			int z = 17 - zoom;
			return "http://mp1.mapplus.ch/kacache/" + z + "/def/def/t" + tiley + "/l" + tilex
					+ "/t" + tiley + "l" + tilex + ".jpg";
		}
	
	}

}
