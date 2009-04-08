package tac.mapsources;

import org.openstreetmap.gui.jmapviewer.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.mapsources.Google.GoogleEarth;
import tac.mapsources.Google.GoogleMapMaker;
import tac.mapsources.Google.GoogleMaps;
import tac.mapsources.Google.GoogleMapsChina;
import tac.mapsources.Google.GoogleTerrain;
import tac.mapsources.Microsoft.MicrosoftHybrid;
import tac.mapsources.Microsoft.MicrosoftMaps;
import tac.mapsources.Microsoft.MicrosoftVirtualEarth;

public class MapSources {

	private static TileSource[] MAP_SOURCES = {
			// For debugging purposes
			// new tac.mapsources.LocalhostTestSource(), //
			new GoogleMaps(), new GoogleMapMaker(), new GoogleMapsChina(), new GoogleEarth(),
			new GoogleTerrain(), new YahooMaps(), new Mapnik(), new TilesAtHome(), new CycleMap(),
			new OpenArialMap(), new OsmHikingMap(), new MicrosoftMaps(),
			new MicrosoftVirtualEarth(), new MicrosoftHybrid(), new OutdooractiveCom(),
			new MultimapCom(), new Cycloatlas(),
	// new MapPlus() //does not work because of an unknown projection - cookie?
	};

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

	/**
	 * Map from Multimap.com - incomplete for high zoom levels
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

			int z = zoom + 1;
			// binary encoding using ones for tilex and twos for tiley.
			// if a bit is set in tilex and tiley we get a three
			char[] num = new char[zoom];
			for (int i = zoom - 1; i >= 0; i--) {
				int n = 0;
				if ((tilex & 1) == 1)
					n += 1;
				if ((tiley & 1) == 1)
					n += 2;
				num[i] = Integer.toString(n).charAt(0);
				tilex >>= 1;
				tiley >>= 1;
			}
			String s = new String(num);
			if (s.length() > 12)
				s = s.substring(0, 6) + "/" + s.substring(6, 12) + "/" + s.substring(12);
			else if (s.length() > 6)
				s = s.substring(0, 6) + "/" + s.substring(6);

			String base;
			if (zoom < 6)
				base = "http://mc1.tiles-cdn.multimap.com/ptiles/map/mi915/";
			else if (zoom < 14)
				base = "http://mc2.tiles-cdn.multimap.com/ptiles/map/mi917/";
			else
				base = "http://mc3.tiles-cdn.multimap.com/ptiles/map/mi931/";
			s = base + z + "/" + s + ".png?client=public_api&service_seq=14458";
			// System.out.println(s);
			return s;
		}
	}

	public static class MapPlus extends AbstractMapSource {

		public MapPlus() {
			super("Map+ (Swiss only)", 7, 16, "jpg");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			int z = 17 - zoom;
			return "http://mp1.mapplus.ch/kacache/" + z + "/def/def/t" + tiley + "/l" + tilex
					+ "/t" + tiley + "l" + tilex + ".jpg";
		}

	}

	public static class YahooMaps extends AbstractMapSource {

		public YahooMaps() {
			super("Yahoo Maps", 1, 16, "png");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			int yahooTileY = (((1 << zoom) - 2) / 2) - tiley;
			int yahooZoom = getMaxZoom() - zoom + 2;
			return "http://maps.yimg.com/hw/tile?locale=en&imgtype=png&yimgv=1.2&v=4.1&x=" + tilex
					+ "&y=" + yahooTileY + "+6163&z=" + yahooZoom;
		}

	}

	public static class Cycloatlas extends AbstractMapSource {

		public Cycloatlas() {
			super("Cycloatlas", 7, 14, "png");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String z = Integer.toString(zoom);
			if (zoom >= 13)
				z += "c";
			return "http://services.ezekiel-ngx.tmapserver.cz/tiles/gm/shc/" + zoom + "/" + tilex
					+ "/" + tiley + ".png";
		}

		@Override
		public String toString() {
			return getName() + " (Czech Republic only)";
		}

	}

	public static class OutdooractiveCom extends AbstractMapSource {

		private static int SERVER_NUM = 0;

		public OutdooractiveCom() {
			super("Outdooractive.com", 8, 16, "png");
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

	public static class OsmHikingMap extends AbstractMapSource {

		public OsmHikingMap() {
			super("OSM Hiking", 4, 15, "png");
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking (Germany only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://opentiles.com/nop/get.php?l=trails&z=" + zoom + "&x=" + tilex + "&y="
					+ tiley;
		}

	}

	public static class OpenArialMap extends AbstractMapSource {

		public OpenArialMap() {
			super("OpenArialMap", 0, 18, "jpg");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://tile.openaerialmap.org/tiles/1.0.0/openaerialmap-900913/" + zoom + "/"
					+ tilex + "/" + tiley + ".jpg";
		}

	}
}
