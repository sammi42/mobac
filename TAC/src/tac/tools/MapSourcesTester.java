package tac.tools;

import static tac.tools.Cities.BANGALORE;
import static tac.tools.Cities.BERLIN;
import static tac.tools.Cities.BRATISLAVA;
import static tac.tools.Cities.PRAHA;
import static tac.tools.Cities.SEOUL;
import static tac.tools.Cities.SHANGHAI;
import static tac.tools.Cities.VIENNA;
import static tac.tools.Cities.WARSZAWA;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.mapsources.MapSourcesManager;
import tac.mapsources.impl.Google.GoogleMapMaker;
import tac.mapsources.impl.Google.GoogleMapsChina;
import tac.mapsources.impl.Google.GoogleMapsKorea;
import tac.mapsources.impl.RegionalMapSources.AustrianMap;
import tac.mapsources.impl.RegionalMapSources.Cykloatlas;
import tac.mapsources.impl.RegionalMapSources.DoCeluPL;
import tac.mapsources.impl.RegionalMapSources.FreemapSlovakia;
import tac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHiking;
import tac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHikingHillShade;
import tac.program.Logging;
import tac.program.model.EastNorthCoordinate;
import tac.program.model.Settings;

/**
 * Small tool that tests every available map source for operability. The
 * operability test consists of the download of one map tile at the highest
 * available zoom level of the map source. By default the map tile to be
 * downloaded is located in the middle of Berlin (at the coordinate of
 * {@link #BERLIN}). As some map providers do not cover Berlin for each
 * {@link MapSource} a different test coordinate can be specified using
 * {@link #testCoordinates}.
 * 
 */
public class MapSourcesTester {

	public static Logger log;

	public static final EastNorthCoordinate C_DEFAULT = BERLIN;

	private static HashMap<Class<?>, EastNorthCoordinate> testCoordinates;

	static {
		testCoordinates = new HashMap<Class<?>, EastNorthCoordinate>();
		testCoordinates.put(GoogleMapMaker.class, BANGALORE);
		testCoordinates.put(Cykloatlas.class, PRAHA);
		testCoordinates.put(GoogleMapsChina.class, SHANGHAI);
		testCoordinates.put(GoogleMapsKorea.class, SEOUL);
		testCoordinates.put(DoCeluPL.class, WARSZAWA);
		testCoordinates.put(AustrianMap.class, VIENNA);
		testCoordinates.put(FreemapSlovakia.class, BRATISLAVA);
		testCoordinates.put(FreemapSlovakiaHiking.class, BRATISLAVA);
		testCoordinates.put(FreemapSlovakiaHikingHillShade.class, BRATISLAVA);
	}

	public static void main(String[] args) {
		Logging.configureConsoleLogging();
		Logger.getRootLogger().setLevel(Level.ERROR);
		log = Logger.getLogger(MapSourcesTester.class);
		try {
			Settings.load();
		} catch (JAXBException e1) {
			e1.printStackTrace();
			return;
		}
		MapSourcesManager.loadMapSourceProperties();

		for (MapSource mapSource : MapSourcesManager.getAllMapSources()) {
			try {
				String name = mapSource.toString();
				StringBuilder sb = new StringBuilder(40);
				sb.append(name);
				while (sb.length() < 40)
					sb.append('.');
				System.out.print(sb.toString() + ": ");
				testMapSource(mapSource);
				System.out.println("OK");
			} catch (MapSourceTestFailed e) {
				System.out.println("Failed: " + e.httpResponseCode);
				log.error("Error: ", e);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	public static void testMapSource(Class<? extends MapSource> mapSourceClass) throws Exception {
		testMapSource(mapSourceClass.newInstance());
	}

	public static void testMapSource(MapSource mapSource) throws Exception {
		EastNorthCoordinate coordinate = testCoordinates.get(mapSource.getClass());
		if (coordinate == null)
			coordinate = C_DEFAULT;
		testMapSource(mapSource, coordinate);
	}

	public static void testMapSource(MapSource mapSource, EastNorthCoordinate coordinate)
			throws Exception {
		int zoom = mapSource.getMaxZoom();

		MapSpace mapSpace = mapSource.getMapSpace();
		int tilex = mapSpace.cLonToX(coordinate.lon, zoom) / mapSpace.getTileSize();
		int tiley = mapSpace.cLatToY(coordinate.lat, zoom) / mapSpace.getTileSize();

		URL url = new URL(mapSource.getTileUrl(zoom, tilex, tiley));
		HttpURLConnection c = (HttpURLConnection) url.openConnection();
		c.addRequestProperty("User-agent", Settings.getInstance().getUserAgent());
		c.connect();
		c.disconnect();
		if (c.getResponseCode() != 200) {
			throw new MapSourceTestFailed(mapSource, c);
		}
	}

	public static class MapSourceTestFailed extends Exception {

		private static final long serialVersionUID = 1L;
		
		final int httpResponseCode;
		final URL url;

		public MapSourceTestFailed(MapSource mapSource, HttpURLConnection conn) throws IOException {
			this(mapSource.getClass(), conn.getURL(), conn.getResponseCode());
		}

		public MapSourceTestFailed(Class<? extends MapSource> mapSourceClass, URL url,
				int httpResponseCode) {
			super("MapSource test failed: " + mapSourceClass.toString() + " HTTP "
					+ httpResponseCode);
			this.url = url;
			this.httpResponseCode = httpResponseCode;
		}

		public int getHttpResponseCode() {
			return httpResponseCode;
		}

	}
}
