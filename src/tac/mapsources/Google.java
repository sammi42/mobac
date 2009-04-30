package tac.mapsources;

import org.apache.log4j.Logger;

public class Google {

	private static final Logger log = Logger.getLogger(MapSources.class);

	public static abstract class GoogleSource extends AbstractMapSource {

		private int serverNum = 0;

		public static String LANG = "en";

		public GoogleSource(String name, int minZoom, int maxZoom, String tileType) {
			super(name, minZoom, maxZoom, tileType);
		}

		protected String loadUrl() {
			String url = System.getProperty(this.getClass().getSimpleName() + ".url");
			if (url == null)
				log.error("Unable to load url for " + this.getClass().getSimpleName());
			return url;
		}

		protected int getNextServerNum() {
			int x = serverNum;
			serverNum = (serverNum + 1) % 4;
			return x;
		}

	}

	public static class GoogleMaps extends GoogleSource {

		public static String SERVER_URL;

		public GoogleMaps() {
			super("Google Maps", 0, 17, "png");
			SERVER_URL = loadUrl();
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), LANG, x, y, zoom });
		}

	}

	/**
	 * "Google Map Maker" Source Class http://www.google.com/mapmaker
	 */
	public static class GoogleMapMaker extends GoogleSource {

		public static String SERVER_URL;

		public GoogleMapMaker() {
			super("Google Map Maker", 1, 17, "png");
			SERVER_URL = loadUrl();
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), x, y, zoom });
		}

	}

	public static class GoogleTerrain extends GoogleSource {

		public static String SERVER_URL;

		public GoogleTerrain() {
			super("Google Terrain", 0, 15, "jpg");
			SERVER_URL = loadUrl();
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), LANG, x, y, zoom });
		}

	}

	/**
	 * Google Maps China (Ditu) http://ditu.google.com/
	 */
	public static class GoogleMapsChina extends GoogleSource {

		public static String SERVER_URL;

		public GoogleMapsChina() {
			super("Google Maps China", 0, 19, "png");
			SERVER_URL = loadUrl();
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), x, y, zoom });
		}

		@Override
		public String toString() {
			return "Google Maps China (Ditu)";
		}

	}

	public static class GoogleEarth extends GoogleSource {

		public static String SERVER_URL;

		public GoogleEarth() {
			super("Google Earth", 0, 20, "jpg");
			SERVER_URL = loadUrl();
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), LANG, x, y, zoom,
					"Galileo" });
		}

	}
}
