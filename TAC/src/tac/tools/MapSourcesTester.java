package tac.tools;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.mapsources.MapSourcesManager;
import tac.mapsources.impl.Google.GoogleMapMaker;
import tac.mapsources.impl.Google.GoogleMapsChina;
import tac.mapsources.impl.RegionalMapSources.Cykloatlas;
import tac.mapsources.impl.RegionalMapSources.DoCeluPL;
import tac.program.Logging;
import tac.program.model.EastNorthCoordinate;

public class MapSourcesTester {
	public static final EastNorthCoordinate C_NEY_YORK = new EastNorthCoordinate(40.75, -73.88);
	public static final EastNorthCoordinate C_BERLIN = new EastNorthCoordinate(52.50, 13.39);
	public static final EastNorthCoordinate C_PRAHA = new EastNorthCoordinate(50.00, 14.41);
	public static final EastNorthCoordinate C_BANGALORE = new EastNorthCoordinate(12.95, 77.616667);
	public static final EastNorthCoordinate C_SHANGHAI = new EastNorthCoordinate(31.2333, 121.4666);
	public static final EastNorthCoordinate C_WARSZAWA = new EastNorthCoordinate(52.2166, 21.0333);

	public static final EastNorthCoordinate C_DEFAULT = C_BERLIN;

	static Logger log = Logger.getLogger(MapSourcesTester.class);

	public static void main(String[] args) {
		Logging.configureLogging();
		Logger.getRootLogger().setLevel(Level.ERROR);
		MapSourcesManager.loadMapSourceProperties();

		HashMap<Class<?>, EastNorthCoordinate> testCoordinates = new HashMap<Class<?>, EastNorthCoordinate>();
		testCoordinates.put(GoogleMapMaker.class, C_BANGALORE);
		testCoordinates.put(Cykloatlas.class, C_PRAHA);
		testCoordinates.put(GoogleMapsChina.class, C_SHANGHAI);
		testCoordinates.put(DoCeluPL.class, C_WARSZAWA);

		for (MapSource mapSource : MapSourcesManager.getAllMapSources()) {

			EastNorthCoordinate coordinate = testCoordinates.get(mapSource.getClass());
			if (coordinate == null)
				coordinate = C_DEFAULT;
			try {
				testMapSource(mapSource, coordinate);
			} catch (Exception e) {
				log.error(mapSource.getName() + " failed", e);
			}
			// testMapSource("TilesAtHome", C_BERLIN);
		}
	}

	public static void testMapSource(MapSource mapSource, EastNorthCoordinate coordinate)
			throws Exception {
		int zoom = mapSource.getMaxZoom();

		int tilex = OsmMercator.LonToX(coordinate.lon, zoom) / Tile.SIZE;
		int tiley = OsmMercator.LatToY(coordinate.lat, zoom) / Tile.SIZE;

		URL url = new URL(mapSource.getTileUrl(zoom, tilex, tiley));
		HttpURLConnection c = (HttpURLConnection) url.openConnection();
		c.connect();
		String name = mapSource.toString();
		while (name.length() < 40)
			name += ".";
		System.out.println(name + " : " + c.getResponseCode());
		if (c.getResponseCode() != 200)
			return;
		c.disconnect();
	}

}
