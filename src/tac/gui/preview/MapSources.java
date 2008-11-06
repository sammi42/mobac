package tac.gui.preview;

import org.openstreetmap.gui.jmapviewer.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class MapSources {

	private static TileSource[] MAP_SOURCES = { new GoogleMaps(), new GoogleEarth(), new Mapnik(),
			new TilesAtHome(), new CycleMap(), new OutdooractiveCom() };

	public static TileSource[] getMapSources() {
		return MAP_SOURCES;
	}

	public static String getDefaultMapSourceName() {
		return getMapSources()[0].getName();
	}

	public static TileSource getSourceByName(String name) {
		for (TileSource t : MAP_SOURCES) {
			if (t.getName().equals(name))
				return t;
		}
		return MAP_SOURCES[0];
	}

	public static class OutdooractiveCom implements TileSource {

		private static int SERVER_NUM = 0;

		public int getMaxZoom() {
			return 16;
		}

		public int getMinZoom() {
			return 8;
		}

		public String getName() {
			return "Outdooractive.com";
		}

		public String getTileType() {
			return "png";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.None;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			if (zoom < 8)
				throw new RuntimeException("Zoom level not suported");
			String s = "http://t" + SERVER_NUM + ".outdooractive.com/portal/map/" + zoom + "/"
					+ tilex + "/" + tiley + ".png";
			SERVER_NUM = (SERVER_NUM + 1) % 4;
			return s;
		}

		@Override
		public String toString() {
			return getName() + " (Germany only)";
		}

	}

	public static class GoogleMaps implements TileSource {

		public static final String SERVER_URL = "http://mt%d.google.com/mt?v=w2.86&hl=%s&x=%d&y=%d&z=%d&s=%s";

		private static int SERVER_NUM = 0;

		public int getMaxZoom() {
			return 17;
		}

		public int getMinZoom() {
			return 0;
		}

		public String getName() {
			return "Google Maps";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		private int getNextServerNum() {
			int x = SERVER_NUM;
			SERVER_NUM = (SERVER_NUM + 1) % 4;
			return x;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), "en", x, y, zoom,
					"Galileo" });
		}

		@Override
		public String toString() {
			return getName();
		}

		public String getTileType() {
			return "png";
		}

	}

	public static class GoogleEarth implements TileSource {

		public static final String SERVER_URL = "http://khm%d.google.com/kh/v=33&hl=%s&x=%d&y=%d&z=%d&s=%s";

		private static int SERVER_NUM = 0;

		public int getMaxZoom() {
			return 20;
		}

		public int getMinZoom() {
			return 0;
		}

		public String getName() {
			return "Google Earth";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		private int getNextServerNum() {
			int x = SERVER_NUM;
			SERVER_NUM = (SERVER_NUM + 1) % 4;
			return x;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), "en", x, y, zoom,
					"Galileo" });
		}

		@Override
		public String toString() {
			return getName();
		}

		public String getTileType() {
			return "jpg";
		}
	}

	/**
	 * Custom tile store provider, configurable via constructor.
	 */
	public static class Custom implements TileSource {

		int maxZoom;
		String name;
		String fileExt;
		TileUpdate tileUpdate;
		String url;

		/**
		 * 
		 * @param name
		 *            Map name
		 * @param url
		 *            with variables $y, $y and $zoom (will be replaced on
		 *            request) <code>http://server/path-$x-$y-$zoom</code>
		 * @param maxZoom
		 * @param fileExt
		 *            specifie the image type of the tiles: usually "png" or
		 *            "jpg"
		 * @param tileUpdate
		 *            on of the {@link TileUpdate} values. If you don't know,
		 *            use {@link TileUpdate#None}
		 */
		public Custom(String name, String url, int maxZoom, String fileExt, TileUpdate tileUpdate) {
			super();
			this.name = name;
			this.url = url;
			this.maxZoom = maxZoom;
			this.fileExt = fileExt;
			this.tileUpdate = tileUpdate;
		}

		public int getMaxZoom() {
			return maxZoom;
		}

		public int getMinZoom() {
			return 0;
		}

		public String getName() {
			return name;
		}

		public String getTileType() {
			return null;
		}

		public TileUpdate getTileUpdate() {
			return tileUpdate;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String tmp = url;
			tmp = tmp.replace("$x", Integer.toString(tilex));
			tmp = tmp.replace("$y", Integer.toString(tiley));
			tmp = tmp.replace("$zoom", Integer.toString(zoom));
			return tmp;
		}
	}

	public static class Mapnik extends OsmTileSource.Mapnik {

		@Override
		public String toString() {
			return "OpenStreetMap Mapnik";
		}

	}

	public static class TilesAtHome extends OsmTileSource.TilesAtHome {
		@Override
		public String toString() {
			return "OpenStreetMap Osmarenderer";
		}
	}

	public static class CycleMap extends OsmTileSource.CycleMap {
		@Override
		public String toString() {
			return "OpenStreetMap Cyclemap";
		}
	}

}
