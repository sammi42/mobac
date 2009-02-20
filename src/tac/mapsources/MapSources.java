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

	private static TileSource[] MAP_SOURCES = { new GoogleMaps(), new GoogleMapMaker(), new GoogleMapsChina(),
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
