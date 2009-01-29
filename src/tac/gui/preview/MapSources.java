package tac.gui.preview;

import org.openstreetmap.gui.jmapviewer.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class MapSources {

	private static TileSource[] MAP_SOURCES = { new GoogleMaps(), new GoogleMapsChina(),
			new GoogleEarth(), new GoogleTerrain(), new YahooMaps(), new Mapnik(),
			new TilesAtHome(), new CycleMap(), new MicrosoftMaps(), new MicrosoftVirtualEarth(),
			new MicrosoftHybrid(), new OutdooractiveCom() };

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

	public static class YahooMaps implements TileSource {

		public int getMaxZoom() {
			return 16;
		}

		public int getMinZoom() {
			return 1;
		}

		public String getName() {
			return "Yahoo Maps";
		}

		public String getTileType() {
			return "png";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.None;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			int yahooTileY = (((1 << zoom) - 2) / 2) - tiley;
			int yahooZoom = getMaxZoom() - zoom + 2;
			return "http://maps.yimg.com/hw/tile?locale=en&imgtype=png&yimgv=1.2&v=4.1&x=" + tilex
					+ "&y=" + yahooTileY + "+6163&z=" + yahooZoom;
		}

		@Override
		public String toString() {
			return getName();
		}

	}

	public static abstract class Microsoft implements TileSource {

		protected int serverNum = 0;
		protected int serverNumMax = 4;
		protected char mapTypeChar;
		protected String tileType = "jpeg";

		public int getMaxZoom() {
			return 19;
		}

		public int getMinZoom() {
			return 1;
		}

		public String getTileType() {
			return "jpg";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.None;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String t = "";
			for (int i = 0; i < zoom; i++) {
				int num = (tilex % 2) | ((tiley % 2) << 1);
				t = num + t;
				tilex >>= 1;
				tiley >>= 1;
			}
			t = "http://" + mapTypeChar + serverNum + ".ortho.tiles.virtualearth.net/tiles/"
					+ mapTypeChar + t + "." + tileType + "?g=45";
			return t;
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	public static class MicrosoftMaps extends Microsoft {

		public MicrosoftMaps() {
			super();
			mapTypeChar = 'r';
			tileType = "png";
		}

		public String getName() {
			return "Microsoft Maps";
		}

		@Override
		public String getTileType() {
			return "png";
		}
		
	}

	public static class MicrosoftVirtualEarth extends Microsoft {

		public MicrosoftVirtualEarth() {
			super();
			mapTypeChar = 'a';
			tileType = "jpeg";
		}

		public String getName() {
			return "Microsoft Virtual Earth";
		}
	}

	public static class MicrosoftHybrid extends Microsoft {

		public MicrosoftHybrid() {
			super();
			mapTypeChar = 'h';
			tileType = "jpeg";
		}

		public String getName() {
			return "Microsoft Hybrid";
		}

		@Override
		public String toString() {
			return "Microsoft Maps/Earth Hybrid";
		}

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

	public static abstract class GoogleSource implements TileSource {

		private static int SERVER_NUM = 0;

		public static String LANG = "en";

		public int getMinZoom() {
			return 0;
		}

		protected int getNextServerNum() {
			int x = SERVER_NUM;
			SERVER_NUM = (SERVER_NUM + 1) % 4;
			return x;
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	public static class GoogleMaps extends GoogleSource {

		public static final String SERVER_URL = "http://mt%d.google.com/mt?v=w2.88&hl=%s&x=%d&y=%d&z=%d";

		// private static int GALILEO_NUM = 0;

		public int getMaxZoom() {
			return 17;
		}

		public String getName() {
			return "Google Maps";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			// String g = "Galileo".substring(0, GALILEO_NUM);
			// GALILEO_NUM = (GALILEO_NUM + 1) % 5;
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), LANG, x, y, zoom });
		}

		public String getTileType() {
			return "png";
		}

	}

	public static class GoogleHybrid extends GoogleSource {

		public static final String SERVER_URL = "http://mt%d.google.com/mt?v=w2t.87&hl=%s&x=%d&y=%d&z=%d";

		public int getMaxZoom() {
			return 17;
		}

		public String getName() {
			return "Google Hybrid";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), LANG, x, y, zoom });
		}

		public String getTileType() {
			return "jpg";
		}

		@Override
		public String toString() {
			return "Google Maps/Earth Hybrid";
		}
	}

	public static class GoogleTerrain extends GoogleSource {

		public static final String SERVER_URL = "http://mt%d.google.com/mt?v=w2p.87&hl=%s&x=%d&y=%d&z=%d";

		public int getMaxZoom() {
			return 15;
		}

		public String getName() {
			return "Google Terrain";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), LANG, x, y, zoom });
		}

		public String getTileType() {
			return "png";
		}

	}

	public static class GoogleMapsChina extends GoogleSource {

		public static final String SERVER_URL = "http://mt%d.google.cn/mt?v=cn1.5&hl=zh-CN&x=%d&y=%d&z=%d";

		public int getMaxZoom() {
			return 17;
		}

		public String getName() {
			return "Google Maps China";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), x, y, zoom });
		}

		public String getTileType() {
			return "png";
		}

		@Override
		public String toString() {
			return "Google Maps China (Ditu)";
		}

	}

	public static class GoogleEarth extends GoogleSource {

		public static final String SERVER_URL = "http://khm%d.google.com/kh/v=33&hl=%s&x=%d&y=%d&z=%d&s=%s";

		public int getMaxZoom() {
			return 20;
		}

		public String getName() {
			return "Google Earth";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), LANG, x, y, zoom,
					"Galileo" });
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
