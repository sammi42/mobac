package tac.mapsources;

public class Google {

	public static abstract class GoogleSource extends AbstractMapSource {

		private int serverNum = 0;

		public static String LANG = "en";

		public GoogleSource(String name, int maxZoom, String tileType) {
			super(name, 0, maxZoom, tileType);
		}

		protected int getNextServerNum() {
			int x = serverNum;
			serverNum = (serverNum + 1) % 4;
			return x;
		}

	}

	public static class GoogleMaps extends GoogleSource {

		public static final String SERVER_URL = "http://mt%d.google.com/mt?v=w2.88&hl=%s&x=%d&y=%d&z=%d";

		public GoogleMaps() {
			super("Google Maps", 17, "png");
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), LANG, x, y, zoom });
		}

	}

	/**
	 * "Google Map Maker" Source Class
	 */
	public static class GoogleMapMaker extends GoogleSource {

		public static final String SERVER_URL = "http://gt%d.google.com/mt?n=404&hl=%s&v=gwm.burma&x=%d&y=%d&z=%d";

		public GoogleMapMaker() {
			super("Google Map Maker", 17, "png");
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), LANG, x, y, zoom });
		}

	}

	public static class GoogleTerrain extends GoogleSource {

		public static final String SERVER_URL = "http://mt%d.google.com/mt?v=w2p.87&hl=%s&x=%d&y=%d&z=%d";

		public GoogleTerrain() {
			super("Google Terrain", 15, "jpg");
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(SERVER_URL, new Object[] { getNextServerNum(), LANG, x, y, zoom });
		}

	}

	public static class GoogleMapsChina extends GoogleSource {

		public static final String SERVER_URL = "http://mt%d.google.cn/mt?v=cn1.5&hl=zh-CN&x=%d&y=%d&z=%d";

		public GoogleMapsChina() {
			super("Google Maps China", 17, "png");
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

		public static final String SERVER_URL = "http://khm%d.google.com/kh/v=33&hl=%s&x=%d&y=%d&z=%d&s=%s";

		public GoogleEarth() {
			super("Google Earth", 20, "jpg");
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
