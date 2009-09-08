package tac.tools;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.mapsources.MapSourcesManager;
import tac.mapsources.impl.Google.GoogleMapMaker;
import tac.mapsources.impl.Google.GoogleMapsChina;
import tac.mapsources.impl.RegionalMapSources.Cykloatlas;
import tac.mapsources.impl.RegionalMapSources.DoCeluPL;
import tac.program.Logging;
import tac.program.model.EastNorthCoordinate;

/**
 * Small tool that tests every available map source for operability. The
 * operability test consists of the download of one map tile at the highest
 * available zoom level of the map source. By default the map tile to be
 * downloaded is located in the middle of Berlin (at the coordinate of
 * {@link #C_BERLIN}). As some map providers do not cover Berlin for each
 * {@link MapSource} a different test coordinate can be specified using
 * {@link #testCoordinates}.
 * 
 */
public class MapSourcesTester {
	public static final EastNorthCoordinate C_NEY_YORK = new EastNorthCoordinate(40.75, -73.88);
	public static final EastNorthCoordinate C_BERLIN = new EastNorthCoordinate(52.50, 13.39);
	public static final EastNorthCoordinate C_PRAHA = new EastNorthCoordinate(50.00, 14.41);
	public static final EastNorthCoordinate C_BANGALORE = new EastNorthCoordinate(12.95, 77.616667);
	public static final EastNorthCoordinate C_SHANGHAI = new EastNorthCoordinate(31.2333, 121.4666);
	public static final EastNorthCoordinate C_WARSZAWA = new EastNorthCoordinate(52.2166, 21.0333);

	public static final EastNorthCoordinate C_DEFAULT = C_BERLIN;

	// private static Logger log = Logger.getLogger(MapSourcesTester.class);

	private static HashMap<Class<?>, EastNorthCoordinate> testCoordinates;

	static {
		testCoordinates = new HashMap<Class<?>, EastNorthCoordinate>();
		testCoordinates.put(GoogleMapMaker.class, C_BANGALORE);
		testCoordinates.put(Cykloatlas.class, C_PRAHA);
		testCoordinates.put(GoogleMapsChina.class, C_SHANGHAI);
		testCoordinates.put(DoCeluPL.class, C_WARSZAWA);
	}

	public static void main(String[] args) {
		Logging.configureConsoleLogging();
		Logger.getRootLogger().setLevel(Level.ERROR);
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
		c.connect();
		c.disconnect();
		if (c.getResponseCode() != 200)
			throw new MapSourceTestFailed(mapSource, c.getResponseCode());
	}

	public static class MapSourceTestFailed extends Exception {
		int httpResponseCode;

		public MapSourceTestFailed(MapSource mapSource, int httpResponseCode) {
			super("MapSource test failed: " + mapSource.toString() + " HTTP " + httpResponseCode);
			this.httpResponseCode = httpResponseCode;
		}

		public MapSourceTestFailed(Class<? extends MapSource> mapSourceClass, int httpResponseCode) {
			super("MapSource test failed: " + mapSourceClass.toString() + " HTTP "
					+ httpResponseCode);
			this.httpResponseCode = httpResponseCode;
		}

		public int getHttpResponseCode() {
			return httpResponseCode;
		}

	}
}
