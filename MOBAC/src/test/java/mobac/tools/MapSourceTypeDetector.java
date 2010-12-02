/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.tools;

import static mobac.tools.Cities.BERLIN;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import mobac.mapsources.DefaultMapSourcesManager;
import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.MapSourcesUpdater;
import mobac.mapsources.impl.MiscMapSources.Topomapper;
import mobac.program.Logging;
import mobac.program.download.TileDownLoader;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.Settings;
import mobac.program.model.TileImageType;
import mobac.utilities.Utilities;

public class MapSourceTypeDetector {

	public static final SecureRandom RND = new SecureRandom();

	public static final EastNorthCoordinate C_DEFAULT = BERLIN;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logging.configureLogging();
		DefaultMapSourcesManager.initialize();
		MapSourcesManager.getInstance().getAllMapSources();
		MapSourcesUpdater.loadMapSourceProperties();

		testMapSource(Topomapper.class);
	}

	public static void testMapSource(Class<? extends HttpMapSource> mapSourceClass) {
		testMapSource(mapSourceClass, Cities.getTestCoordinate(mapSourceClass, C_DEFAULT));
	}

	public static void testMapSource(Class<? extends HttpMapSource> mapSourceClass, EastNorthCoordinate coordinate) {
		if (coordinate == null)
			throw new NullPointerException("Coordinate not set for " + mapSourceClass.getSimpleName());
		MapSourceTypeDetector mstd;
		try {
			mstd = new MapSourceTypeDetector(mapSourceClass);
			mstd.testMapSource(coordinate);
		} catch (Exception e) {
			System.err.println("Error while testing map source: " + e.getMessage());
		}
	}

	public static void testMapSource(String mapSourceName, EastNorthCoordinate coordinate) {
		MapSourceTypeDetector mstd = new MapSourceTypeDetector(mapSourceName);
		mstd.testMapSource(coordinate);
		System.out.println(mstd);
	}

	private final HttpMapSource mapSource;
	private URL url;
	private HttpURLConnection c;

	private boolean eTagSupported = false;
	private boolean expirationTimePresent = false;
	private boolean lastModifiedTimePresent = false;

	private boolean ifModifiedSinceSupported = false;
	private boolean ifNoneMatchSupported = false;

	public MapSourceTypeDetector(Class<? extends HttpMapSource> mapSourceClass) throws InstantiationException,
			IllegalAccessException {
		this(mapSourceClass.newInstance());
	}

	public MapSourceTypeDetector(String mapSourceName) {
		this((HttpMapSource) MapSourcesManager.getInstance().getSourceByName(mapSourceName));
	}

	public MapSourceTypeDetector(HttpMapSource mapSource) {
		this.mapSource = mapSource;
		if (mapSource == null)
			throw new NullPointerException("MapSource not set");
	}

	public void testMapSource(EastNorthCoordinate coordinate) {
		int zoomMed = mapSource.getMinZoom() + ((mapSource.getMaxZoom() - mapSource.getMinZoom()) / 2);
		testMapSource(coordinate, zoomMed);
		System.out.println(this);
		testMapSource(coordinate, mapSource.getMinZoom());
		System.out.println(this);
		testMapSource(coordinate, mapSource.getMaxZoom());
		System.out.println(this);
	}

	public void testMapSource(EastNorthCoordinate coordinate, int zoom) {
		try {
			System.out.println("Testing " + mapSource.toString());

			MapSpace mapSpace = mapSource.getMapSpace();
			int tilex = mapSpace.cLonToX(coordinate.lon, zoom) / mapSpace.getTileSize();
			int tiley = mapSpace.cLatToY(coordinate.lat, zoom) / mapSpace.getTileSize();

			c = mapSource.getTileUrlConnection(zoom, tilex, tiley);
			url = c.getURL();
			System.out.println("Sample url: " + c.getURL());
			System.out.println("Connecting...");
			c.setReadTimeout(10000);
			c.addRequestProperty("User-agent", Settings.getInstance().getUserAgent());
			c.setRequestProperty("Accept", TileDownLoader.ACCEPT);
			c.connect();
			System.out.println("Connection established - response HTTP " + c.getResponseCode());
			if (c.getResponseCode() != 200)
				return;

			// printHeaders();

			byte[] content = Utilities.getInputBytes(c.getInputStream());
			TileImageType detectedContentType = Utilities.getImageType(content);

			String contentType = c.getContentType();
			contentType = contentType.substring(6);
			if ("png".equals(contentType))
				contentType = "png";
			else if ("jpeg".equals(contentType))
				contentType = "jpg";
			else
				contentType = "unknown: " + c.getContentType();
			if (contentType.equals(detectedContentType.getFileExt()))
				contentType += " (verified)";
			else
				contentType += " (unverified)";
			System.out.print("Image format          : " + contentType);

			String eTag = c.getHeaderField("ETag");
			eTagSupported = (eTag != null);
			if (eTagSupported) {
				// System.out.println("eTag                  : " + eTag);
				testIfNoneMatch(content);
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
			lastModifiedTimePresent = (c.getHeaderField("last-modified") != null) && (modified != 0);
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

	private void testIfNoneMatch(byte[] content) throws Exception {
		String eTag = c.getHeaderField("ETag");
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		byte[] digest = md5.digest(content);
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
		c2.disconnect();
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
		sw.append("Mapsource.........: " + mapSource.getName() + "\n");
		sw.append("Current TileUpdate: " + mapSource.getTileUpdate() + "\n");
		sw.append("If-None-Match.....: " + b2s(ifNoneMatchSupported) + "\n");
		sw.append("ETag..............: " + b2s(eTagSupported) + "\n");
		sw.append("If-Modified-Since.: " + b2s(ifModifiedSinceSupported) + "\n");
		sw.append("LastModified......: " + b2s(lastModifiedTimePresent) + "\n");
		sw.append("Expires...........: " + b2s(expirationTimePresent) + "\n");

		return sw.toString();
	}

	private static String b2s(boolean b) {
		if (b)
			return "supported";
		else
			return "-";
	}

	static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
			(byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e',
			(byte) 'f' };

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
