package tac.mapsources;

import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.Main;
import tac.mapsources.Google.GoogleEarth;
import tac.mapsources.Google.GoogleMapMaker;
import tac.mapsources.Google.GoogleMaps;
import tac.mapsources.Google.GoogleMapsChina;
import tac.mapsources.Google.GoogleTerrain;
import tac.mapsources.Microsoft.MicrosoftHybrid;
import tac.mapsources.Microsoft.MicrosoftMaps;
import tac.mapsources.Microsoft.MicrosoftMapsChina;
import tac.mapsources.Microsoft.MicrosoftVirtualEarth;
import tac.mapsources.RegionalMapSources.Cykloatlas;
import tac.mapsources.RegionalMapSources.DoCeluPL;
import tac.mapsources.RegionalMapSources.OutdooractiveCom;
import tac.mapsources.RegionalMapSources.UmpWawPl;
import tac.mapsources.WmsSources.TerraserverUSA;
import tac.program.model.Settings;
import tac.utilities.Utilities;

public class MapSources {

	private static final Logger log = Logger.getLogger(MapSources.class);

	public static final MapSource DEFAULT = new Mapnik();
	private static MapSource[] MAP_SOURCES;

	static {
		loadMapSourceProperties();
		MAP_SOURCES = new MapSource[] { //
				//
				new GoogleMaps(), new GoogleMapMaker(), new GoogleMapsChina(), new GoogleEarth(),
				new GoogleTerrain(), new YahooMaps(), DEFAULT, new TilesAtHome(), new CycleMap(),
				new OsmHikingMap(), /* new OpenArialMap(), */new MicrosoftMaps(),
				new MicrosoftMapsChina(), new MicrosoftVirtualEarth(), new MicrosoftHybrid(),
				new OutdooractiveCom(), new MultimapCom(), new Cykloatlas(), new TerraserverUSA(),
				new UmpWawPl(), new DoCeluPL(),
		// The following map sources do not work because of an unknown
		// protection - cookie?
		// new TuristikaMapSk()
		// new MapPlus()
		};
	}

	public static Vector<MapSource> getAllMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>();
		if (Settings.getInstance().isDevModeEnabled())
			mapSources.add(new LocalhostTestSource());
		for (MapSource ms : MAP_SOURCES)
			mapSources.add(ms);
		for (MapSource ms : Settings.getInstance().customMapSources)
			mapSources.add(ms);
		return mapSources;
	}

	public static Vector<MapSource> getEnabledMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>();
		if (Settings.getInstance().isDevModeEnabled())
			mapSources.add(new LocalhostTestSource());
		TreeSet<String> disabledMapSources = new TreeSet<String>(Settings.getInstance()
				.getDisabledMapSources());
		for (MapSource ms : MAP_SOURCES) {
			if (!disabledMapSources.contains(ms.getName()))
				mapSources.add(ms);
		}
		for (MapSource ms : Settings.getInstance().customMapSources)
			mapSources.add(ms);
		return mapSources;
	}

	public static String getDefaultMapSourceName() {
		return DEFAULT.getName();
	}

	public static MapSource getSourceByName(String name) {
		for (MapSource ms : MAP_SOURCES) {
			if (ms.getName().equals(name))
				return ms;
		}
		for (MapSource ms : Settings.getInstance().customMapSources) {
			if (ms.getName().equals(name))
				return ms;
		}
		return null;
	}

	/**
	 * Map from Multimap.com - incomplete for high zoom levels Uses Quad-Tree
	 * coordinate notation?
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
			return "http://topo.geofabrik.de/trails/" + zoom + "/" + tilex + "/" + tiley + ".png";
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

	/**
	 * Merges the mapsources property into the system property bundle
	 */
	public static void loadMapSourceProperties() {
		try {
			URL mapResUrl = Main.class.getResource("mapsources.properties");
			File mapFile = new File(Settings.getUserDir(), "mapsources.properties");
			Properties resProps = new Properties();
			Properties fileProps = new Properties();
			Utilities.loadProperties(resProps, mapResUrl);
			Properties selectedProps;
			if (mapFile.isFile()) {
				Utilities.loadProperties(fileProps, mapFile);
				selectedProps = getNewestProperties(resProps, fileProps);
			} else {
				selectedProps = resProps;
			}
			if (selectedProps == resProps)
				log.debug("Used mapsources.properties: resource");
			else
				log.debug("Used mapsources.properties: file");
			Properties systemProps = System.getProperties();
			systemProps.putAll(selectedProps);

		} catch (Exception e) {
			log.error("Error while reading mapsources.properties: ", e);
		}
	}

	private static Properties getNewestProperties(Properties resProps, Properties fileProps) {
		String revRes = resProps.getProperty("mapsources.Rev");
		String revFile = fileProps.getProperty("mapsources.Rev");

		if (revFile == null)
			return resProps;

		final Pattern SVN_REV = Pattern.compile("\\$Rev\\:\\s*(\\d*)\\s*\\$");

		Matcher m = SVN_REV.matcher(revRes);
		if (!m.matches())
			throw new RuntimeException(
					"Revision information in mapsources.properties (resoure) missing");
		revRes = m.group(1);

		m = SVN_REV.matcher(revFile);
		if (!m.matches()) {
			log.error("External mapsources.properties file does not contain a valid revision");
			return resProps;
		}
		revFile = m.group(1);

		int revisionFile = Integer.parseInt(revFile);
		int revisionRes = Integer.parseInt(revRes);

		if (revisionFile > revisionRes)
			return fileProps;
		else
			return resProps;
	}
}
