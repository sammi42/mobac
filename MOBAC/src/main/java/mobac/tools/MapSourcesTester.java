package mobac.tools;

import static mobac.tools.Cities.BERLIN;

import java.io.IOException;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.MapSourcesUpdater;
import mobac.mapsources.MultiLayerMapSource;
import mobac.mapsources.impl.LocalhostTestSource;
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

	private static HashSet<String> testedMapSources = new HashSet<String>();

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
		processAllMapSources();
		// processMapSource(new OsmMapSources.OsmHikingMapWithRelief());
	}

	public static void processAllMapSources() {
		for (MapSource mapSource : MapSourcesManager.getAllMapSources()) {
			if (mapSource instanceof LocalhostTestSource)
				continue;
			processMapSource(mapSource);
		}
	}

	public static void processMapSource(MapSource mapSource) {
		try {
			String name = mapSource.getStoreName();
			StringBuilder sb = new StringBuilder(40);
			sb.append(name);
			while (sb.length() < 40)
				sb.append('.');
			if (!testedMapSources.contains(mapSource.getStoreName())) {
				testedMapSources.add(mapSource.getStoreName());
				System.out.print(sb.toString() + ": ");
				testMapSource(mapSource);
				System.out.println("OK");
			}
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
		if (mapSource instanceof MultiLayerMapSource)
			processMapSource(((MultiLayerMapSource) mapSource).getBackgroundMapSource());
	}

	public static boolean testMapSource(Class<? extends MapSource> mapSourceClass) throws Exception {
		MapSource mapSource = mapSourceClass.newInstance();
		return testMapSource(mapSource);
	}

	public static boolean testMapSource(MapSource mapSource) throws Exception {
		EastNorthCoordinate coordinate = Cities.getTestCoordinate(mapSource, C_DEFAULT);
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
		try {
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
			switch (mapSource.getTileUpdate()) {
			case ETag:
			case IfNoneMatch:
				if (c.getHeaderField("ETag") == null) {
					throw new MapSourceTestFailed(mapSource,
							"No ETag present but map sources uses " + mapSource.getTileUpdate()
									+ "\n", c);
				}
				break;
			case LastModified:
				if (c.getHeaderField("Last-Modified") == null)
					throw new MapSourceTestFailed(mapSource,
							"No Last-Modified entry present but map sources uses "
									+ mapSource.getTileUpdate() + "\n", c);
				break;
			}
		} finally {
			c.disconnect();
		}
	}

	public static class MapSourceTestFailed extends Exception {

		private static final long serialVersionUID = 1L;

		final int httpResponseCode;
		final HttpURLConnection conn;
		final URL url;
		final Class<? extends MapSource> mapSourceClass;

		public MapSourceTestFailed(MapSource mapSource, String msg, HttpURLConnection conn)
				throws IOException {
			super(msg);
			this.mapSourceClass = mapSource.getClass();
			this.conn = conn;
			this.url = conn.getURL();
			this.httpResponseCode = conn.getResponseCode();
		}

		public MapSourceTestFailed(MapSource mapSource, HttpURLConnection conn) throws IOException {
			this(mapSource, "", conn);
		}

		public MapSourceTestFailed(Class<? extends MapSource> mapSourceClass, URL url,
				int httpResponseCode) {
			super();
			this.mapSourceClass = mapSourceClass;
			this.url = url;
			this.conn = null;
			this.httpResponseCode = httpResponseCode;
		}

		public int getHttpResponseCode() {
			return httpResponseCode;
		}

		@Override
		public String getMessage() {
			String msg = super.getMessage();
			msg = "MapSource test failed: " + msg + " " + mapSourceClass.getSimpleName() + " HTTP "
					+ httpResponseCode + "\n" + conn.getURL();
			if (conn != null)
				msg += "\n" + printHeaders(conn);
			return msg;
		}

		protected static String printHeaders(HttpURLConnection conn) {
			StringWriter sw = new StringWriter();
			sw.append("Headers:\n");
			for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
				String key = entry.getKey();
				for (String elem : entry.getValue()) {
					if (key != null)
						sw.append(key + " = ");
					sw.append(elem);
					sw.append("\n");
				}
			}
			return sw.toString();
		}
	}
}
