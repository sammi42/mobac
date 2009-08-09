package tac.mapsources.impl;

import tac.mapsources.AbstractMapSource;

public class OsmMapSources {

	public static final String MAP_MAPNIK = "http://tile.openstreetmap.org";
	public static final String MAP_OSMA = "http://tah.openstreetmap.org/Tiles/tile";
	public static final String MAP_HIKING = "http://topo.geofabrik.de/trails/";

	protected static abstract class AbstractOsmTileSource extends AbstractMapSource {

		public AbstractOsmTileSource(String name) {
			super(name, 0, 18, "png");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		public String getTileType() {
			return "png";
		}

		public boolean allowFileStore() {
			return true;
		}
	}

	public static class Mapnik extends AbstractOsmTileSource {

		public Mapnik() {
			super("Mapnik");
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_MAPNIK + super.getTileUrl(zoom, tilex, tiley);
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfNoneMatch;
		}

		@Override
		public String toString() {
			return "OpenStreetMap Mapnik";
		}

	}

	public static class CycleMap extends AbstractOsmTileSource {

		private static final String PATTERN = "http://%s.andy.sandbox.cloudmade.com/tiles/cycle/%d/%d/%d.png";

		private static final String[] SERVER = { "a", "b", "c" };

		private int SERVER_NUM = 0;

		public CycleMap() {
			super("OSM Cycle Map");
			this.maxZoom = 17;
			this.tileUpdate = TileUpdate.LastModified;
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			String url = String.format(PATTERN, new Object[] { SERVER[SERVER_NUM], zoom, tilex,
					tiley });
			SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
			return url;
		}

		@Override
		public String toString() {
			return "OpenStreetMap Cyclemap";
		}

	}

	public static class TilesAtHome extends AbstractOsmTileSource {

		public TilesAtHome() {
			super("TilesAtHome");
			this.maxZoom = 17;
			this.tileUpdate = TileUpdate.IfModifiedSince;
			;
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_OSMA + super.getTileUrl(zoom, tilex, tiley);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Osmarenderer";
		}

	}

	public static class OsmHikingMap extends AbstractMapSource {

		public OsmHikingMap() {
			super("OSM Hiking", 4, 15, "png");
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking (Germany only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

}
