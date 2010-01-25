package mobac.tools;

import static mobac.tools.Cities.BERLIN;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.bind.JAXBException;

import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.MapSourcesUpdater;
import mobac.mapsources.MultiLayerMapSource;
import mobac.mapsources.impl.LocalhostTestSource;
import mobac.mapsources.impl.Google.GoogleMapMaker;
import mobac.mapsources.impl.Google.GoogleMapsChina;
import mobac.mapsources.impl.Google.GoogleMapsKorea;
import mobac.mapsources.impl.MiscMapSources.MultimapCom;
import mobac.mapsources.impl.MiscMapSources.MultimapOSUkCom;
import mobac.mapsources.impl.OsmMapSources.OpenPisteMap;
import mobac.mapsources.impl.OsmMapSources.Turaterkep;
import mobac.mapsources.impl.RegionalMapSources.AustrianMap;
import mobac.mapsources.impl.RegionalMapSources.Bergfex;
import mobac.mapsources.impl.RegionalMapSources.Cykloatlas;
import mobac.mapsources.impl.RegionalMapSources.DoCeluPL;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakia;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHiking;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHikingHillShade;
import mobac.mapsources.impl.RegionalMapSources.HubermediaBavaria;
import mobac.mapsources.impl.RegionalMapSources.MapplusCh;
import mobac.mapsources.impl.RegionalMapSources.NearMap;
import mobac.mapsources.impl.RegionalMapSources.StatkartTopo2;
import mobac.program.Logging;
import mobac.program.download.TileDownLoader;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.Settings;
import mobac.utilities.Utilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


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
		testCoordinates.put(MultimapCom.class, Cities.LONDON);
		testCoordinates.put(MultimapOSUkCom.class, Cities.LONDON);
		testCoordinates.put(DoCeluPL.class, Cities.WARSZAWA);
		testCoordinates.put(AustrianMap.class, Cities.VIENNA);
		testCoordinates.put(FreemapSlovakia.class, Cities.BRATISLAVA);
		testCoordinates.put(FreemapSlovakiaHiking.class, Cities.BRATISLAVA);
		testCoordinates.put(FreemapSlovakiaHikingHillShade.class, Cities.BRATISLAVA);
		testCoordinates.put(NearMap.class, Cities.SYDNEY);
		testCoordinates.put(HubermediaBavaria.class, Cities.MUNICH);
		testCoordinates.put(OpenPisteMap.class, Cities.MUNICH);
		testCoordinates.put(StatkartTopo2.class, Cities.OSLO);
		testCoordinates.put(MapplusCh.class, Cities.BERN);
		testCoordinates.put(Turaterkep.class, Cities.BUDAPEST);
		testCoordinates.put(Bergfex.class, Cities.INNSBRUCK);
	}

	public static void main(String[] args) {
		HttpURLConnection.setFollowRedirects(false);
		Logging.configureConsoleLogging();
		Logger.getRootLogger().setLevel(Level.ERROR);
		log = Logger.getLogger(MapSourcesTester.class);
		try {
			Settings.load();
		} catch (JAXBException e1) {
			e1.printStackTrace();
			return;
		}
		MapSourcesUpdater.loadMapSourceProperties();

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
		if (c.getResponseCode() == 302) {
			log.debug(c.getResponseMessage());
		}
		if (c.getResponseCode() != 200) {
			throw new MapSourceTestFailed(mapSource, c);
		}
		byte[] imageData = Utilities.getInputBytes(c.getInputStream());
		if (imageData.length == 0)
			throw new MapSourceTestFailed(mapSource, "Image data empty", c);
		if (Utilities.getImageDataFormat(imageData) == null) {
			throw new MapSourceTestFailed(mapSource, "Image data of unknown format", c);
		}
	}

	public static class MapSourceTestFailed extends Exception {

		private static final long serialVersionUID = 1L;

		final int httpResponseCode;
		final URL url;

		public MapSourceTestFailed(MapSource mapSource, String msg, HttpURLConnection conn)
				throws IOException {
			super("MapSource test failed: " + msg + " " + mapSource.getStoreName() + " HTTP "
					+ conn.getResponseCode());
			this.url = conn.getURL();
			this.httpResponseCode = conn.getResponseCode();
		}

		public MapSourceTestFailed(MapSource mapSource, HttpURLConnection conn) throws IOException {
			this(mapSource, "", conn);
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
