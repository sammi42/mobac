package moller.preview;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class GoogleTileSource {

	public static final String GOOGLE_MAPS = "http://mt%d.google.com/mt/v=w2.83&hl=%s&x=%d&y=%d&z=%d";
	public static final String GOOGLE_EARTH = "http://khm%d.google.com/kh/v=32&hl=%s&x=%d&y=%d&z=%d";

	public static class GoogleMaps implements TileSource {

		private static int SERVER_NUM = 0;

		public int getMaxZoom() {
			return 17;
		}

		public String getName() {
			return "Google Maps";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.None;
		}

		private int getNextServerNum() {
			int x = SERVER_NUM;
			SERVER_NUM = (SERVER_NUM + 1) % 4;
			return x;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(GOOGLE_MAPS, new Object[] {
					getNextServerNum(), "de", x, y, zoom});
		}
	}

	public static class GoogleEarth implements TileSource {

		private static int SERVER_NUM = 0;

		public int getMaxZoom() {
			return 16;
		}

		public String getName() {
			return "Google Earth";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.None;
		}

		private int getNextServerNum() {
			int x = SERVER_NUM;
			SERVER_NUM = (SERVER_NUM + 1) % 4;
			return x;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return String.format(GOOGLE_EARTH, new Object[] {
					getNextServerNum(), "de", x, y, zoom });
		}

	}

}
