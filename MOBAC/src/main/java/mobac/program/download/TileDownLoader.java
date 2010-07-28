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
package mobac.program.download;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import mobac.exceptions.DownloadFailedException;
import mobac.exceptions.UnrecoverableDownloadException;
import mobac.program.atlascreators.tileprovider.DownloadedTileProvider;
import mobac.program.model.Settings;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.TileStoreEntry;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndexedArchive;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource.TileUpdate;

public class TileDownLoader {

	public static String ACCEPT = "text/html, image/png, image/jpeg, image/gif, */*;q=0.1";

	static {
		System.setProperty("sun.net.client.defaultReadTimeout", "15000");
		System.setProperty("http.maxConnections", "20");
	}

	private static Logger log = Logger.getLogger(TileDownLoader.class);

	private static Settings settings = Settings.getInstance();

	public static int getImage(int layer, int x, int y, int zoom, MapSource mapSource,
			TarIndexedArchive tileArchive) throws IOException, InterruptedException,
			UnrecoverableDownloadException {

		MapSpace mapSpace = mapSource.getMapSpace();
		int maxTileIndex = mapSpace.getMaxPixels(zoom) / mapSpace.getTileSize();
		if (x > maxTileIndex)
			throw new RuntimeException("Invalid tile index x=" + x + " for zoom " + zoom);
		if (y > maxTileIndex)
			throw new RuntimeException("Invalid tile index y=" + y + " for zoom " + zoom);

		TileStore ts = TileStore.getInstance();

		// Thread.sleep(2000);

		// Test code for creating random download failures
		// if (Math.random()>0.7) throw new
		// IOException("intentionally download error");

		Settings s = Settings.getInstance();
		String tileFileName = String.format(DownloadedTileProvider.TILE_FILENAME_PATTERN, layer, x,
				y);

		TileStoreEntry tile = null;
		if (s.tileStoreEnabled) {

			// Copy the file from the persistent tilestore instead of
			// downloading it from internet.
			tile = ts.getTile(x, y, zoom, mapSource);
			boolean expired = isTileExpired(tile);
			if (tile != null) {
				if (expired) {
					log.trace("Expired: " + mapSource.getName() + " " + tile);
				} else {
					synchronized (tileArchive) {
						log.trace("Tile used from tilestore");
						tileArchive.writeFileFromData(tileFileName, tile.getData());
					}
					return 0;
				}
			}
		}
		byte[] data = null;
		if (tile == null) {
			data = downloadTileAndUpdateStore(x, y, zoom, mapSource);
		} else {
			updateStoredTile(tile, mapSource);
			data = tile.getData();
		}
		if (data == null)
			return 0;
		synchronized (tileArchive) {
			tileArchive.writeFileFromData(tileFileName, data);
		}
		return data.length;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @param mapSource
	 * @return
	 * @throws UnrecoverableDownloadException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static byte[] downloadTileAndUpdateStore(int x, int y, int zoom, MapSource mapSource)
			throws UnrecoverableDownloadException, IOException, InterruptedException {
		HttpURLConnection conn = mapSource.getTileUrlConnection(zoom, x, y);
		if (conn == null)
			throw new UnrecoverableDownloadException("Tile x=" + x + " y=" + y + " zoom=" + zoom
					+ " is not a valid tile in map source " + mapSource);

		log.trace("Downloading " + conn.getURL());

		conn.setRequestMethod("GET");

		Settings s = Settings.getInstance();
		conn.setConnectTimeout(1000 * s.httpConnectionTimeout);
		conn.setReadTimeout(1000 * s.httpReadTimeout);
		conn.addRequestProperty("User-agent", s.getUserAgent());
		conn.setRequestProperty("Accept", ACCEPT);
		conn.connect();

		int code = conn.getResponseCode();
		byte[] data = loadBodyDataInBuffer(conn);

		if (code != HttpURLConnection.HTTP_OK)
			throw new DownloadFailedException(conn, code);
		String contentType = conn.getContentType();
		if (contentType != null && !contentType.startsWith("image/"))
			throw new UnrecoverableDownloadException(
					"Content type of the loaded image is unknown: " + contentType);

		String eTag = conn.getHeaderField("ETag");
		long timeLastModified = conn.getLastModified();
		long timeExpires = conn.getExpiration();

		Utilities.checkForInterruption();
		String imageFormat = Utilities.getImageDataFormat(data);
		if (imageFormat == null)
			throw new UnrecoverableDownloadException("The returned image is of unknown format");
		if (mapSource.allowFileStore() && s.tileStoreEnabled) {
			TileStore.getInstance().putTileData(data, x, y, zoom, mapSource, timeLastModified,
					timeExpires, eTag);
		}
		Utilities.checkForInterruption();
		return data;
	}

	public static byte[] updateStoredTile(TileStoreEntry tile, MapSource mapSource)
			throws UnrecoverableDownloadException, IOException, InterruptedException {
		final int x = tile.getX();
		final int y = tile.getY();
		final int zoom = tile.getZoom();
		final TileUpdate tileUpdate = mapSource.getTileUpdate();

		switch (tileUpdate) {
		case ETag: {
			boolean unchanged = hasTileETag(tile, mapSource);
			if (unchanged) {
				if (log.isTraceEnabled())
					log.trace("Data unchanged on server (eTag): " + mapSource + " " + tile);
				return null;
			}
			break;
		}
		case LastModified: {
			boolean isNewer = isTileNewer(tile, mapSource);
			if (!isNewer) {
				if (log.isTraceEnabled())
					log.trace("Data unchanged on server (LastModified): " + mapSource + " " + tile);
				return null;
			}
			break;
		}
		}
		HttpURLConnection conn = mapSource.getTileUrlConnection(zoom, x, y);
		if (conn == null)
			throw new UnrecoverableDownloadException("Tile x=" + x + " y=" + y + " zoom=" + zoom
					+ " is not a valid tile in map source " + mapSource);

		if (log.isTraceEnabled())
			log.trace(String.format("Checking %s %s", mapSource.getName(), tile));

		conn.setRequestMethod("GET");

		boolean conditionalRequest = false;

		switch (tileUpdate) {
		case IfNoneMatch: {
			if (tile.geteTag() != null) {
				conn.setRequestProperty("If-None-Match", tile.geteTag());
				conditionalRequest = true;
			}
			break;
		}
		case IfModifiedSince: {
			if (tile.getTimeLastModified() > 0) {
				conn.setIfModifiedSince(tile.getTimeLastModified());
				conditionalRequest = true;
			}
			break;
		}
		}

		Settings s = Settings.getInstance();
		conn.setConnectTimeout(1000 * s.httpConnectionTimeout);
		conn.setReadTimeout(1000 * s.httpReadTimeout);
		conn.addRequestProperty("User-agent", s.getUserAgent());
		conn.setRequestProperty("Accept", ACCEPT);
		conn.connect();

		int code = conn.getResponseCode();

		if (conditionalRequest && code == HttpURLConnection.HTTP_NOT_MODIFIED) {
			// Data unchanged on server
			if (mapSource.allowFileStore() && s.tileStoreEnabled) {
				tile.update(conn.getExpiration());
				TileStore.getInstance().putTile(tile, mapSource);
			}
			if (log.isTraceEnabled())
				log.trace("Data unchanged on server: " + mapSource + " " + tile);
			return null;
		}
		byte[] data = loadBodyDataInBuffer(conn);

		if (code != HttpURLConnection.HTTP_OK)
			throw new DownloadFailedException(conn, code);
		String contentType = conn.getContentType();
		if (contentType != null && !contentType.startsWith("image/"))
			throw new UnrecoverableDownloadException(
					"Content type of the loaded image is unknown: " + contentType);

		String eTag = conn.getHeaderField("ETag");
		long timeLastModified = conn.getLastModified();
		long timeExpires = conn.getExpiration();

		Utilities.checkForInterruption();
		String imageFormat = Utilities.getImageDataFormat(data);
		if (imageFormat == null)
			throw new UnrecoverableDownloadException("The returned image is of unknown format");
		if (mapSource.allowFileStore() && s.tileStoreEnabled) {
			TileStore.getInstance().putTileData(data, x, y, zoom, mapSource, timeLastModified,
					timeExpires, eTag);
		}
		Utilities.checkForInterruption();
		return data;
	}

	public static boolean isTileExpired(TileStoreEntry tileStoreEntry) {
		if (tileStoreEntry == null)
			return true;
		long expiredTime = tileStoreEntry.getTimeExpires();
		if (expiredTime >= 0) {
			// server had set an expiration time
			long maxExpirationTime = settings.tileMaxExpirationTime
					+ tileStoreEntry.getTimeDownloaded();
			long minExpirationTime = settings.tileMinExpirationTime
					+ tileStoreEntry.getTimeDownloaded();
			expiredTime = Math.max(minExpirationTime, Math.min(maxExpirationTime, expiredTime));
		} else {
			// no expiration time set by server - use the default one
			expiredTime = tileStoreEntry.getTimeDownloaded() + settings.tileDefaultExpirationTime;
		}
		return (expiredTime < System.currentTimeMillis());
	}

	/**
	 * Reads all available data from the input stream of <code>conn</code> and
	 * returns it as byte array. If no input data is available the method
	 * returns <code>null</code>.
	 * 
	 * @param conn
	 * @return
	 * @throws IOException
	 */
	protected static byte[] loadBodyDataInBuffer(HttpURLConnection conn) throws IOException {
		InputStream input = conn.getInputStream();
		int bufSize = Math.max(input.available(), 32768);
		ByteArrayOutputStream bout = new ByteArrayOutputStream(bufSize);
		byte[] buffer = new byte[2048];
		boolean finished = false;
		do {
			int read = input.read(buffer);
			if (read >= 0)
				bout.write(buffer, 0, read);
			else
				finished = true;
		} while (!finished);
		log.trace("Retrieved " + bout.size() + " bytes for a HTTP " + conn.getResponseCode());
		if (bout.size() == 0)
			return null;
		return bout.toByteArray();
	}

	/**
	 * Performs a <code>HEAD</code> request for retrieving the
	 * <code>LastModified</code> header value.
	 */
	protected static boolean isTileNewer(TileStoreEntry tile, MapSource mapSource)
			throws IOException {
		long oldLastModified = tile.getTimeLastModified();
		if (oldLastModified <= 0) {
			log.warn("Tile age comparison not possible: "
					+ "tile in tilestore does not contain lastModified attribute");
			return true;
		}
		HttpURLConnection conn = mapSource.getTileUrlConnection(tile.getZoom(), tile.getX(), tile
				.getY());
		conn.setRequestMethod("HEAD");
		conn.setRequestProperty("Accept", ACCEPT);
		long newLastModified = conn.getLastModified();
		if (newLastModified == 0)
			return true;
		return (newLastModified > oldLastModified);
	}

	protected static boolean hasTileETag(TileStoreEntry tile, MapSource mapSource)
			throws IOException {
		String eTag = tile.geteTag();
		if (eTag == null || eTag.length() == 0) {
			log.warn("ETag check not possible: "
					+ "tile in tilestore does not contain ETag attribute");
			return true;
		}
		HttpURLConnection conn = mapSource.getTileUrlConnection(tile.getZoom(), tile.getX(), tile
				.getY());
		conn.setRequestMethod("HEAD");
		conn.setRequestProperty("Accept", ACCEPT);
		String onlineETag = conn.getHeaderField("ETag");
		if (onlineETag == null || onlineETag.length() == 0)
			return true;
		return (onlineETag.equals(eTag));
	}
}
