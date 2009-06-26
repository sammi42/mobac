package tac.tools;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.mapsources.MapSources;
import tac.program.model.EastNorthCoordinate;

public class MapSourceTypeDetector {

	public static final EastNorthCoordinate C_NEY_YORK = new EastNorthCoordinate(40.75, -73.88);
	public static final EastNorthCoordinate C_BERLIN = new EastNorthCoordinate(13.39, 52.50);

	static EastNorthCoordinate TEST_COORDINATE = C_BERLIN;

	public static final SecureRandom RND = new SecureRandom();

	static URL url;
	static HttpURLConnection c;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// testMapSource("Google Maps", C_BERLIN);
		// testMapSource("Yahoo Maps", C_BERLIN);
		testMapSource("Mapnik", C_BERLIN);
		testMapSource("TilesAtHome", C_BERLIN);
	}

	public static void testMapSource(String mapSourceName, EastNorthCoordinate coordinate) {
		try {
			MapSource ms = MapSources.getSourceByName(mapSourceName);
			System.out.println("Testing " + ms.toString());
			int zoom = RND.nextInt(ms.getMaxZoom());
			zoom = Math.max(zoom, ms.getMinZoom());

			int tilex = OsmMercator.LonToX(TEST_COORDINATE.lon, zoom) / Tile.SIZE;
			int tiley = OsmMercator.LatToY(TEST_COORDINATE.lat, zoom) / Tile.SIZE;

			url = new URL(ms.getTileUrl(zoom, tilex, tiley));
			System.out.println("Sample url: " + url);
			c = (HttpURLConnection) url.openConnection();
			System.out.println("Connecting...");
			c.connect();
			System.out.println("Connection established - response HTTP " + c.getResponseCode());

			printHeaders();
			String eTag = c.getHeaderField("ETag");
			boolean eTagSupported = (eTag != null);
			if (eTagSupported) {
				System.out.println("eTag                  : " + eTag);
				testETag();
			} else
				System.out.println("eTag                  : -");

			long date = c.getDate();
			if (date == 0)
				System.out.println("Date time             : -");
			else
				System.out.println("Date time             : " + new Date(date));

			long exp = c.getExpiration();
			if (exp == 0)
				System.out.println("Expiration time       : -");
			else
				System.out.println("Expiration time       : " + new Date(exp));

			long modified = c.getLastModified();
			if (modified == 0)
				System.out.println("Last modified time    : not set");
			else
				System.out.println("Last modified time    : " + new Date(modified));

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n");
	}

	private static void testETag() throws IOException {
		String eTag = c.getHeaderField("ETag");
		HttpURLConnection c2 = (HttpURLConnection) url.openConnection();
		c2.addRequestProperty("If-None-Match", eTag);
		c2.connect();
		int code = c2.getResponseCode();
		System.out
				.println("If-None-Match response: " + code + " (" + c2.getResponseMessage() + ")");
	}

	static void printHeaders() {
		System.out.println("\nHeaders:");
		for (Map.Entry<String, List<String>> entry : c.getHeaderFields().entrySet()) {
			String key = entry.getKey();
			for (String elem : entry.getValue()) {
				if (key != null)
					System.out.print(key + " = ");
				System.out.println(elem);
			}
		}
		System.out.println();
	}
}
