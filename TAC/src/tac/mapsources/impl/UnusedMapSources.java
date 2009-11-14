package tac.mapsources.impl;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.mapsources.AbstractMapSource;
import tac.mapsources.mapspace.MercatorPower2MapSpace;

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

	/**
	 * Uses 512x512 tiles - not fully supported at the moment!
	 */
	public static class Turaterkep extends AbstractMapSource {

		private MapSpace space = new MercatorPower2MapSpace(512);

		public Turaterkep() {
			super("Turaterkep", 7, 16, "png", TileUpdate.IfNoneMatch);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://turaterkep.hostcity.hu/tiles/" + zoom + "/" + tilex + "/" + tiley
					+ ".png";
		}

		@Override
		public MapSpace getMapSpace() {
			return space;
		}

		@Override
		public String toString() {
			return "Turaterkep (Hungary, experimental)";
		}

	}
}
