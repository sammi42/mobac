package tac.mapsources.impl;

import tac.mapsources.AbstractMapSource;
import tac.mapsources.MapSourcesTools;

public class MiscMapSources {

	/**
	 * Map from Multimap.com - incomplete for high zoom levels Uses Quad-Tree
	 * coordinate notation
	 */
	public static class MultimapCom extends AbstractMapSource {

		public MultimapCom() {
			// zoom level supported:
			// 0 (fixed url) world.png
			// 1-5 "mergend binary encoding"
			// 6-? uses MS MAP tiles at some parts of the world
			super("Multimap.com", 1, 17, "png");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {

			String tileNum = MapSourcesTools.encodeQuadTree(zoom, tilex, tiley);
			if (tileNum.length() > 12)
				tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6, 12) + "/"
						+ tileNum.substring(12);
			else if (tileNum.length() > 6)
				tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6);

			String base;
			if (zoom < 6)
				base = "http://mc1.tiles-cdn.multimap.com/ptiles/map/mi915/";
			else if (zoom < 14)
				base = "http://mc2.tiles-cdn.multimap.com/ptiles/map/mi917/";
			else
				base = "http://mc3.tiles-cdn.multimap.com/ptiles/map/mi931/";
			tileNum = base + (zoom + 1) + "/" + tileNum
					+ ".png?client=public_api&service_seq=14458";
			return tileNum;
		}
	}

	public static class MultimapOSUkCom extends AbstractMapSource {

		public MultimapOSUkCom() {
			// zoom level supported:
			// 0 (fixed url) world.png
			// 1-5 "mergend binary encoding"
			// 6-? uses MS MAP tiles at some parts of the world
			super("Multimap UK OS Map", 1, 16, "png");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String tileNum = MapSourcesTools.encodeQuadTree(zoom, tilex, tiley);
			if (tileNum.length() > 12)
				tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6, 12) + "/"
						+ tileNum.substring(12);
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

			tileNum = base + (zoom + 1) + "/" + tileNum
					+ ".png?client=public_api&service_seq=14458";
			return tileNum;
		}
	}

	public static class YahooMaps extends AbstractMapSource {

		public YahooMaps() {
			super("Yahoo Maps", 1, 16, "jpg");
			tileUpdate = TileUpdate.IfModifiedSince;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			int yahooTileY = (((1 << zoom) - 2) / 2) - tiley;
			int yahooZoom = getMaxZoom() - zoom + 2;
			return "http://maps.yimg.com/hw/tile?locale=en&imgtype=png&yimgv=1.2&v=4.1&x=" + tilex
					+ "&y=" + yahooTileY + "+6163&z=" + yahooZoom;
		}

	}

}
