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

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.UpdatableMapSource;
import mobac.mapsources.mapspace.MercatorPower2MapSpaceEllipsoidal;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

public class MiscMapSources {

	/**
	 * Map from Multimap.com - incomplete for high zoom levels Uses Quad-Tree coordinate notation
	 */
	public static class MultimapCom extends AbstractMapSource {

		public MultimapCom() {
			// zoom level supported:
			// 0 (fixed url) world.png
			// 1-5 "mergend binary encoding"
			// 6-? uses MS MAP tiles at some parts of the world
			super("Multimap.com", 1, 17, "png", TileUpdate.IfModifiedSince);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {

			String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
			if (tileNum.length() > 12)
				tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6, 12) + "/" + tileNum.substring(12);
			else if (tileNum.length() > 6)
				tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6);

			String base;
			if (zoom < 6)
				base = "http://mc1.tiles-cdn.multimap.com/ptiles/map/mi915/";
			else if (zoom < 14)
				base = "http://mc2.tiles-cdn.multimap.com/ptiles/map/mi917/";
			else
				base = "http://mc3.tiles-cdn.multimap.com/ptiles/map/mi931/";
			tileNum = base + (zoom + 1) + "/" + tileNum + ".png?client=public_api&service_seq=14458";
			return tileNum;
		}
	}

	public static class MultimapOSUkCom extends AbstractMapSource {

		public MultimapOSUkCom() {
			// zoom level supported:
			// 0 (fixed url) world.png
			// 1-5 "mergend binary encoding"
			// 6-? uses MS MAP tiles at some parts of the world
			super("Multimap UK OS Map", 1, 16, "png", TileUpdate.IfModifiedSince);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
			if (tileNum.length() > 12)
				tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6, 12) + "/" + tileNum.substring(12);
			else if (tileNum.length() > 6)
				tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6);

			String base;
			if (zoom < 6)
				base = "http://mc1.tiles-cdn.multimap.com/ptiles/map/mi915/";
			else if (zoom < 14)
				base = "http://mc2.tiles-cdn.multimap.com/ptiles/map/mi917/";
			else if (zoom < 15)
				base = "http://mc0.tiles-cdn.multimap.com/ptiles/map/mi904/";
			else
				base = "http://mc0.tiles-cdn.multimap.com/ptiles/map/mi932/";

			tileNum = base + (zoom + 1) + "/" + tileNum + ".png?client=public_api&service_seq=14458";
			return tileNum;
		}
	}

	public static class YahooMaps extends AbstractMapSource {

		public YahooMaps() {
			super("Yahoo Maps", 1, 16, "jpg", TileUpdate.IfModifiedSince);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			int yahooTileY = (((1 << zoom) - 2) / 2) - tiley;
			int yahooZoom = getMaxZoom() - zoom + 2;
			return "http://maps.yimg.com/hw/tile?locale=en&imgtype=png&yimgv=1.2&v=4.1&x=" + tilex + "&y=" + yahooTileY
					+ "+6163&z=" + yahooZoom;
		}

	}

	public static class YahooMapsJapan extends AbstractMapSource {

		public YahooMapsJapan() {
			super("Yahoo Maps Japan", 1, 19, "png", TileUpdate.IfModifiedSince);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			int yahooY = (((1 << zoom) - 2) / 2) - tiley;
			int yahooZoom = zoom + 1;
			return "http://ta.map.yahoo.co.jp/yta/map?v=4.3&r=1&x=" + tilex + "&y=" + yahooY + "&z=" + yahooZoom;
		}

	}

	public static class YahooMapsTaiwan extends AbstractMapSource {

		public YahooMapsTaiwan() {
			super("Yahoo Maps Taiwan", 1, 18, "png", TileUpdate.None);
		}

		public String getTileUrl(int zoom, int x, int y) {
			int yahooY = (((1 << zoom) - 2) / 2) - y;
			int yahooZoom = 16 - zoom + 2;
			return "http://l.yimg.com/kp/tl?x=" + x + "&y=" + yahooY + "&z=" + yahooZoom;
		}
	}

	public static class OviMaps extends AbstractMapSource {

		public OviMaps() {
			super("Ovi Maps", 1, 18, "png");
			tileUpdate = TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int x, int y) {
			return "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day/" + zoom + "/" + x + "/" + y
					+ "/256/png8?token=...&referer=maps.ovi.com";
		}

		@Override
		public String toString() {
			return "Ovi/Nokia Maps";
		}

	}

	/**
	 * Yandex Maps
	 */
	public static class YandexMap extends AbstractMapSource implements UpdatableMapSource {
		// YandexMap.url=http://vec0{$servernum}.maps.yandex.ru/tiles?l=map&v=2.10.2&x={$x}&y={$y}&z={$z}

		int SERVER_NUM = 1;

		String urlPattern;

		public YandexMap() {
			super("YandexMap", 1, 17, "png", TileUpdate.IfModifiedSince);
			update();
		}

		@Override
		public MapSpace getMapSpace() {
			return MercatorPower2MapSpaceEllipsoidal.INSTANCE_256;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			SERVER_NUM = (SERVER_NUM % 3) + 3;
			String tmp = urlPattern;
			tmp = tmp.replace("{$servernum}", Integer.toString(SERVER_NUM));
			tmp = tmp.replace("{$x}", Integer.toString(tilex));
			tmp = tmp.replace("{$y}", Integer.toString(tiley));
			tmp = tmp.replace("{$z}", Integer.toString(zoom));
			return tmp;
		}

		@Override
		public String toString() {
			return "Yandex Map (Russia)";
		}

		public void update() {
			urlPattern = MapSourceTools.loadMapUrl(this, "url");
		}

	}

	/**
	 * Yandex Sat
	 */
	public static class YandexSat extends AbstractMapSource implements UpdatableMapSource {

		private static int SERVER_NUM = 1;

		private String urlPattern;

		public YandexSat() {
			super("YandexSat", 1, 18, "jpg", TileUpdate.IfModifiedSince);
			update();
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			SERVER_NUM = (SERVER_NUM % 3) + 3;
			String tmp = urlPattern;
			tmp = tmp.replace("{$servernum}", Integer.toString(SERVER_NUM));
			tmp = tmp.replace("{$x}", Integer.toString(tilex));
			tmp = tmp.replace("{$y}", Integer.toString(tiley));
			tmp = tmp.replace("{$z}", Integer.toString(zoom));
			return tmp;
		}

		@Override
		public MapSpace getMapSpace() {
			return MercatorPower2MapSpaceEllipsoidal.INSTANCE_256;
		}

		@Override
		public String toString() {
			return "Yandex Sat (Russia)";
		}

		public void update() {
			urlPattern = MapSourceTools.loadMapUrl(this, "url");
		}

	}

	/**
	 * http://map.navitel.su
	 * 
	 * @version 1.1
	 * @author Andrey Raygorodskiy (andrey(dot)raygorodskiy(at)gmail(dot)com)
	 * @author r_x
	 */
	public static class Navitel extends AbstractMapSource {

		private static final String BASE_URL = "http://maps.navitel.su/navitms.fcgi?t=%08d,%08d,%02d";

		public Navitel() {
			super("Navitel.su", 3, 17, "png", TileUpdate.None);
		}

		@Override
		public String toString() {
			return "Navitel (Russian)";
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			tiley = (1 << zoom) - tiley - 1;
			return String.format(BASE_URL, tilex, tiley, zoom);
		}
	}
}
