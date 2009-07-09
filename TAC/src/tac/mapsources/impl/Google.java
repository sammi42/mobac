package tac.mapsources.impl;

import org.apache.log4j.Logger;

import tac.mapsources.AbstractMapSource;
import tac.mapsources.UpdatableMapSource;

public class Google {

	private static final Logger log = Logger.getLogger(Google.class);
	public static String LANG = "en";

	public static abstract class GoogleSource extends AbstractMapSource implements
			UpdatableMapSource {

		private int serverNum = 0;

		public String serverUrl;

		public GoogleSource(String name, int minZoom, int maxZoom, String tileType) {
			super(name, minZoom, maxZoom, tileType);
			update();
		}

		public void update() {
			String url = System.getProperty(this.getClass().getSimpleName() + ".url");
			if (url == null)
				log.error("Unable to load url for " + this.getClass().getSimpleName());
			serverUrl = url;
		}

		protected int getNextServerNum() {
			int x = serverNum;
			serverNum = (serverNum + 1) % 4;
			return x;
		}

		public String getTileUrl(int zoom, int x, int y) {
			String tmp = serverUrl;
			tmp = tmp.replace("{$servernum}", Integer.toString(getNextServerNum()));
			tmp = tmp.replace("{$lang}", Google.LANG);
			tmp = tmp.replace("{$x}", Integer.toString(x));
			tmp = tmp.replace("{$y}", Integer.toString(y));
			tmp = tmp.replace("{$z}", Integer.toString(zoom));
			return tmp;
		}

	}

	public static class GoogleMaps extends GoogleSource {

		public GoogleMaps() {
			super("Google Maps", 0, 17, "png");
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

	}

	/**
	 * "Google Map Maker" Source Class http://www.google.com/mapmaker
	 */
	public static class GoogleMapMaker extends GoogleSource {

		public GoogleMapMaker() {
			super("Google Map Maker", 1, 17, "png");
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

	}

	public static class GoogleTerrain extends GoogleSource {

		public GoogleTerrain() {
			super("Google Terrain", 0, 15, "jpg");
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

	}

	/**
	 * Google Maps China (Ditu) http://ditu.google.com/
	 */
	public static class GoogleMapsChina extends GoogleSource {

		public GoogleMapsChina() {
			super("Google Maps China", 0, 19, "png");
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

		@Override
		public String toString() {
			return "Google Maps China (Ditu)";
		}

	}

	public static class GoogleEarth extends GoogleSource {

		public GoogleEarth() {
			super("Google Earth", 0, 20, "jpg");
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

	}

	public static class GoogleEarthMapsOverlay extends GoogleSource {

		public GoogleEarthMapsOverlay() {
			super("Google Earth Maps Overlay", 0, 20, "png");
		}

		@Override
		public void update() {
			serverUrl = "http://mt{$servernum}.google.com/mt/v=w2t.92&hl=en&x={$x}&y={$y}&z={$z}";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfModifiedSince;
		}

	}

}
