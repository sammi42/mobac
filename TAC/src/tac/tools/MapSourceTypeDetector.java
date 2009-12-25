package tac.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.mapsources.MapSourcesManager;
import tac.mapsources.impl.RegionalMapSources;
import tac.program.Logging;
import tac.program.model.EastNorthCoordinate;

public class MapSourceTypeDetector {

	public static final SecureRandom RND = new SecureRandom();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logging.configureLogging();
		MapSourcesManager.loadMapSourceProperties();
		testMapSource(RegionalMapSources.EniroCom.class, Cities.OSLO);
	}

	public static void testMapSource(Class<? extends MapSource> mapSourceClass,
			EastNorthCoordinate coordinate) {
		MapSourceTypeDetector mstd;
		try {
			mstd = new MapSourceTypeDetector(mapSourceClass);
			mstd.testMapSource(coordinate);
			System.out.println(mstd);
		} catch (Exception e) {
			System.err.println("Error while testing map source: " + e.getMessage());
		}
	}

	public static void testMapSource(String mapSourceName, EastNorthCoordinate coordinate) {
		MapSourceTypeDetector mstd = new MapSourceTypeDetector(mapSourceName);
		mstd.testMapSource(coordinate);
		System.out.println(mstd);
	}

	private final MapSource mapSource;
	private URL url;
	private HttpURLConnection c;

	private boolean eTagSupported = false;
	private boolean expirationTimePresent = false;
	private boolean lastModifiedTimePresent = false;

	private boolean ifModifiedSinceSupported = false;
	private boolean ifNoneMatchSupported = false;

	public MapSourceTypeDetector(Class<? extends MapSource> mapSourceClass)
			throws InstantiationException, IllegalAccessException {
		this(mapSourceClass.newInstance());
	}

	public MapSourceTypeDetector(String mapSourceName) {
		this(MapSourcesManager.getSourceByName(mapSourceName));
	}

	public MapSourceTypeDetector(MapSource mapSource) {
		this.mapSource = mapSource;
		if (mapSource == null)
			throw new NullPointerException("MapSource not set");
	}

	public void testMapSource(EastNorthCoordinate coordinate) {
		try {
			System.out.println("Testing " + mapSource.toString());
			int zoom = mapSource.getMinZoom()
					+ ((mapSource.getMaxZoom() - mapSource.getMinZoom()) / 2);

			MapSpace mapSpace = mapSource.getMapSpace();
			int tilex = mapSpace.cLonToX(coordinate.lon, zoom) / mapSpace.getTileSize();
			int tiley = mapSpace.cLatToY(coordinate.lat, zoom) / mapSpace.getTileSize();

			c = mapSource.getTileUrlConnection(zoom, tilex, tiley);
			url = c.getURL();
			System.out.println("Sample url: " + c.getURL());
			System.out.println("Connecting...");
			c.connect();
			System.out.println("Connection established - response HTTP " + c.getResponseCode());
			if (c.getResponseCode() != 200)
				return;

			// printHeaders();

			String contentType = c.getContentType();
			System.out.print("Image format          : ");
			if ("image/png".equals(contentType))
				System.out.println("png");
			else if ("image/jpeg".equals(contentType))
				System.out.println("jpg");
			else
				System.out.println("unknown");

			String eTag = c.getHeaderField("ETag");
			eTagSupported = (eTag != null);
			if (eTagSupported) {
				// System.out.println("eTag                  : " + eTag);
				testIfNoneMatch();
			}
			// else System.out.println("eTag                  : -");

			// long date = c.getDate();
			// if (date == 0)
			// System.out.println("Date time             : -");
			// else
			// System.out.println("Date time             : " + new Date(date));

			long exp = c.getExpiration();
			expirationTimePresent = (c.getHeaderField("expires") != null) && (exp != 0);
			if (exp == 0) {
				// System.out.println("Expiration time       : -");
			} else {
				// long diff = (exp - System.currentTimeMillis()) / 1000;
				// System.out.println("Expiration time       : " + new Date(exp)
				// + " => "
				// + Utilities.formatDurationSeconds(diff));
			}
			long modified = c.getLastModified();
			lastModifiedTimePresent = (c.getHeaderField("last-modified") != null)
					&& (modified != 0);
			// if (modified == 0)
			// System.out.println("Last modified time    : not set");
			// else
			// System.out.println("Last modified time    : " + new
			// Date(modified));

			testIfModified();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n");
	}

	private void testIfNoneMatch() throws Exception {
		String eTag = c.getHeaderField("ETag");
		InputStream in = c.getInputStream();
		byte[] buffer = new byte[1024];
		int read = 0;
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.reset();
		do {
			read = in.read(buffer);
			if (read > 0)
				md5.update(buffer, 0, read);
		} while (read < 0);
		byte[] digest = md5.digest();
		String hexDigest = getHexString(digest);
		// System.out.println("content MD5           : " + hexDigest);
		if (hexDigest.equals(eTag))
			System.out.print("eTag content          : md5 hex string");
		String quotedHexDigest = "\"" + hexDigest + "\"";
		if (quotedHexDigest.equals(eTag))
			System.out.print("eTag content          : quoted md5 hex string");

		HttpURLConnection c2 = (HttpURLConnection) url.openConnection();
		c2.addRequestProperty("If-None-Match", eTag);
		c2.connect();
		int code = c2.getResponseCode();
		boolean supported = (code == 304);
		ifNoneMatchSupported = supported;
		// System.out.print("If-None-Match response: ");
		// System.out.println(b2s(supported) + " - " + code + " (" +
		// c2.getResponseMessage() + ")");
	}

	private void testIfModified() throws IOException {
		HttpURLConnection c2 = (HttpURLConnection) url.openConnection();
		c2.setIfModifiedSince(System.currentTimeMillis() + 1000); // future date
		c2.connect();
		int code = c2.getResponseCode();
		boolean supported = (code == 304);
		ifModifiedSinceSupported = supported;
		// System.out.print("If-Modified-Since     : ");
		// System.out.println(b2s(supported) + " - " + code + " (" +
		// c2.getResponseMessage() + ")");
	}

	protected void printHeaders() {
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

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		sw.append("Mapsource........: " + mapSource.getName() + "\n");
		sw.append("If-None-Match....: " + b2s(ifNoneMatchSupported) + "\n");
		sw.append("eTag.............: " + b2s(eTagSupported) + "\n");
		sw.append("If-Modified-Since: " + b2s(ifModifiedSinceSupported) + "\n");
		sw.append("LastModified.....: " + b2s(lastModifiedTimePresent) + "\n");
		sw.append("Expires..........: " + b2s(expirationTimePresent) + "\n");

		return sw.toString();
	}

	private static String b2s(boolean b) {
		if (b)
			return "supported";
		else
			return "-";
	}

	static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2', (byte) '3',
			(byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a',
			(byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f' };

	public static String getHexString(byte[] raw) throws UnsupportedEncodingException {
		byte[] hex = new byte[2 * raw.length];
		int index = 0;

		for (byte b : raw) {
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}
		return new String(hex, "ASCII");
	}
}
