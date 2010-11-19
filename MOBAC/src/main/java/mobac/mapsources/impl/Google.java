/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.mapsources.impl;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.AbstractMultiLayerMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.UpdatableMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageType;

public class Google {

	public static String LANG = "en";

	public static abstract class GoogleSource extends AbstractHttpMapSource implements UpdatableMapSource {

		private int serverNum = 0;

		public String serverUrl;

		public GoogleSource(String name, int minZoom, int maxZoom, TileImageType tileType,
				HttpMapSource.TileUpdate tileUpdate) {
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
			super("Google Maps", 0, 19, TileImageType.PNG, HttpMapSource.TileUpdate.None);
		}

	}

	/**
	 * "Google Map Maker" Source Class http://www.google.com/mapmaker
	 */
	public static class GoogleMapMaker extends GoogleSource {

		public GoogleMapMaker() {
			super("Google Map Maker", 1, 17, TileImageType.PNG, HttpMapSource.TileUpdate.LastModified);
		}

	}

	public static class GoogleTerrain extends GoogleSource {

		public GoogleTerrain() {
			super("Google Terrain", 0, 15, TileImageType.JPG, HttpMapSource.TileUpdate.None);
		}

	}

	/**
	 * Google Maps China (Ditu) http://ditu.google.com/
	 */
	public static class GoogleMapsChina extends GoogleSource {

		public GoogleMapsChina() {
			super("Google Maps China", 0, 19, TileImageType.PNG, HttpMapSource.TileUpdate.None);
		}

		@Override
		public String toString() {
			return "Google Maps China (Ditu)";
		}

	}

	/**
	 * <a href="http://maps.google.com/?ie=UTF8&ll=36.279707,128.204956&spn=3.126164,4.932861&z=8" >Google Maps
	 * Korea</a>
	 * 
	 */
	public static class GoogleMapsKorea extends GoogleSource {

		public GoogleMapsKorea() {
			super("Google Maps Korea", 0, 18, TileImageType.PNG, HttpMapSource.TileUpdate.None);
		}

		@Override
		public String toString() {
			return "Google Maps Korea";
		}

	}

	public static class GoogleEarth extends GoogleSource {

		public GoogleEarth() {
			super("Google Earth", 0, 20, TileImageType.JPG, HttpMapSource.TileUpdate.None);
		}

	}

	public static class GoogleEarthMapsOverlay extends GoogleSource {

		public GoogleEarthMapsOverlay() {
			super("Google Earth Maps Overlay", 0, 20, TileImageType.PNG, HttpMapSource.TileUpdate.None);
		}

	}

	public static class GoogleHybrid extends AbstractMultiLayerMapSource {

		public GoogleHybrid() {
			super("Google Hybrid", TileImageType.PNG);
			mapSources = new MapSource[] { new GoogleEarth(), new GoogleEarthMapsOverlay() };
			initializeValues();
		}

	}
}
