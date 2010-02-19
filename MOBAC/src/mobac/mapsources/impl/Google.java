package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.MultiLayerMapSource;
import mobac.mapsources.UpdatableMapSource;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class Google {

	public static String LANG = "en";

	public static abstract class GoogleSource extends AbstractMapSource implements
			UpdatableMapSource {

		private int serverNum = 0;

		public String serverUrl;

		public GoogleSource(String name, int minZoom, int maxZoom, String tileType,
				TileUpdate tileUpdate) {
			super(name, minZoom, maxZoom, tileType, tileUpdate);
			update();
		}

		public void update() {
			serverUrl = MapSourceTools.loadMapUrl(this, "url");
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
			super("Google Maps", 0, 19, "png", TileUpdate.None);
		}

	}

	/**
	 * "Google Map Maker" Source Class http://www.google.com/mapmaker
	 */
	public static class GoogleMapMaker extends GoogleSource {

		public GoogleMapMaker() {
			super("Google Map Maker", 1, 17, "png", TileUpdate.LastModified);
		}

	}

	public static class GoogleTerrain extends GoogleSource {

		public GoogleTerrain() {
			super("Google Terrain", 0, 15, "jpg", TileUpdate.None);
		}

	}

	/**
	 * Google Maps China (Ditu) http://ditu.google.com/
	 */
	public static class GoogleMapsChina extends GoogleSource {

		public GoogleMapsChina() {
			super("Google Maps China", 0, 19, "png", TileUpdate.None);
		}

		@Override
		public String toString() {
			return "Google Maps China (Ditu)";
		}

	}

	/**
	 * <a href="http://maps.google.com/?ie=UTF8&ll=36.279707,128.204956&spn=3.126164,4.932861&z=8"
	 * >Google Maps Korea</a>
	 * 
	 */
	public static class GoogleMapsKorea extends GoogleSource {

		public GoogleMapsKorea() {
			super("Google Maps Korea", 0, 18, "png", TileUpdate.None);
		}

		@Override
		public String toString() {
			return "Google Maps Korea";
		}

	}

	public static class GoogleEarth extends GoogleSource {

		public GoogleEarth() {
			super("Google Earth", 0, 20, "jpg", TileUpdate.None);
		}

	}

	public static class GoogleEarthMapsOverlay extends GoogleSource {

		public GoogleEarthMapsOverlay() {
			super("Google Earth Maps Overlay", 0, 20, "png", TileUpdate.None);
		}

	}

	public static class GoogleHybrid extends GoogleEarthMapsOverlay implements MultiLayerMapSource {

		private final MapSource background = new GoogleEarth();

		public GoogleHybrid() {
			super();
		}

		@Override
		public String getName() {
			return "Google Hybrid";
		}

		@Override
		public String toString() {
			return "Google Hybrid";
		}

		public MapSource getBackgroundMapSource() {
			return background;
		}

	}
}
