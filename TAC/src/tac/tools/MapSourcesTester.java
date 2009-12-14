package tac.tools;

import static tac.tools.Cities.BERLIN;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.mapsources.MapSourcesManager;
import tac.mapsources.MultiLayerMapSource;
import tac.mapsources.impl.LocalhostTestSource;
import tac.mapsources.impl.Google.GoogleMapMaker;
import tac.mapsources.impl.Google.GoogleMapsChina;
import tac.mapsources.impl.Google.GoogleMapsKorea;
import tac.mapsources.impl.MiscMapSources.YandexMap;
import tac.mapsources.impl.MiscMapSources.YandexSat;
import tac.mapsources.impl.OsmMapSources.OpenPisteMap;
import tac.mapsources.impl.RegionalMapSources.AustrianMap;
import tac.mapsources.impl.RegionalMapSources.Cykloatlas;
import tac.mapsources.impl.RegionalMapSources.DoCeluPL;
import tac.mapsources.impl.RegionalMapSources.FreemapSlovakia;
import tac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHiking;
import tac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHikingHillShade;
import tac.mapsources.impl.RegionalMapSources.HubermediaBavaria;
import tac.mapsources.impl.RegionalMapSources.NearMap;
import tac.mapsources.impl.RegionalMapSources.StatkartTopo2;
import tac.program.Logging;
import tac.program.download.TileDownLoader;
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

	private static HashMap<Class<? extends MapSource>, EastNorthCoordinate> testCoordinates;

	private static HashSet<String> testedMapSources;

	static {
		testCoordinates = new HashMap<Class<? extends MapSource>, EastNorthCoordinate>();
		testedMapSources = new HashSet<String>();
		testCoordinates.put(GoogleMapMaker.class, Cities.BANGALORE);
		testCoordinates.put(Cykloatlas.class, Cities.PRAHA);
		testCoordinates.put(GoogleMapsChina.class, Cities.SHANGHAI);
		testCoordinates.put(GoogleMapsKorea.class, Cities.SEOUL);
		testCoordinates.put(DoCeluPL.class, Cities.WARSZAWA);
		testCoordinates.put(AustrianMap.class, Cities.VIENNA);
		testCoordinates.put(FreemapSlovakia.class, Cities.BRATISLAVA);
		testCoordinates.put(FreemapSlovakiaHiking.class, Cities.BRATISLAVA);
		testCoordinates.put(FreemapSlovakiaHikingHillShade.class, Cities.BRATISLAVA);
		testCoordinates.put(NearMap.class, Cities.SYDNEY);
		testCoordinates.put(YandexMap.class, Cities.MOSCOW);
		testCoordinates.put(YandexSat.class, Cities.MOSCOW);
		testCoordinates.put(HubermediaBavaria.class, Cities.MUNICH);
		testCoordinates.put(OpenPisteMap.class, Cities.MUNICH);
		testCoordinates.put(StatkartTopo2.class, Cities.OSLO);
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
			if (mapSource instanceof LocalhostTestSource)
				continue;
			try {
				String name = mapSource.getStoreName();
				StringBuilder sb = new StringBuilder(40);
				sb.append(name);
				while (sb.length() < 40)
					sb.append('.');
				System.out.print(sb.toString() + ": ");
				if (testMapSource(mapSource))
					System.out.println("OK");
				else
					System.out.println("Skipped");
			} catch (MapSourceTestFailed e) {
				System.out.println("Failed: " + e.httpResponseCode);
				if (e.httpResponseCode != 404)
					log.error("Error: ", e);
			} catch (ConnectException e) {
				System.out.println(e);
			} catch (SocketTimeoutException e) {
				System.out.println(e);
			} catch (Exception e) {
				System.out.println(e);
				log.error("", e);
			}
		}
	}

	public static boolean testMapSource(Class<? extends MapSource> mapSourceClass) throws Exception {
		MapSource mapSource = mapSourceClass.newInstance();
		boolean b = testMapSource(mapSource);
		if (mapSource instanceof MultiLayerMapSource)
			b |= testMapSource(((MultiLayerMapSource) mapSource).getBackgroundMapSource());
		return b;
	}

	public static boolean testMapSource(MapSource mapSource) throws Exception {
		Class<? extends MapSource> mc = mapSource.getClass();
		if (testedMapSources.contains(mapSource.getStoreName()))
			return false; // map source already tested
		testedMapSources.add(mapSource.getStoreName());
		EastNorthCoordinate coordinate = testCoordinates.get(mc);
		if (coordinate == null)
			coordinate = C_DEFAULT;
		testMapSource(mapSource, coordinate);
		return true;
	}

	public static void testMapSource(MapSource mapSource, EastNorthCoordinate coordinate)
			throws Exception {
		int zoom = mapSource.getMaxZoom();

		MapSpace mapSpace = mapSource.getMapSpace();
		int tilex = mapSpace.cLonToX(coordinate.lon, zoom) / mapSpace.getTileSize();
		int tiley = mapSpace.cLatToY(coordinate.lat, zoom) / mapSpace.getTileSize();

		HttpURLConnection c = mapSource.getTileUrlConnection(zoom, tilex, tiley);
		c.setReadTimeout(10000);
		c.addRequestProperty("User-agent", Settings.getInstance().getUserAgent());
		c.setRequestProperty("Accept", TileDownLoader.ACCEPT);

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
