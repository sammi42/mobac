package tac.program;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.UnrecoverableDownloadException;
import tac.program.model.Settings;
import tac.tar.TarIndexedArchive;
import tac.tilestore.TileStore;
import tac.tilestore.TileStoreEntry;
import tac.utilities.Utilities;

public class TileDownLoader {

	public static final long FILE_AGE_ONE_DAY = 1000 * 60 * 60 * 24;
	public static final long FILE_AGE_ONE_WEEK = FILE_AGE_ONE_DAY * 7;

	static {
		System.setProperty("http.maxConnections", "15");
	}

	private static Logger log = Logger.getLogger(TileDownLoader.class);

	public static int getImage(int x, int y, int zoom, MapSource mapSource,
			TarIndexedArchive tileArchive) throws IOException, InterruptedException,
			UnrecoverableDownloadException {

		int maxTileIndex = 2 << zoom;
		if (x > maxTileIndex)
			throw new RuntimeException("Invalid tile index x=" + x + " for zoom " + zoom);
		if (y > maxTileIndex)
			throw new RuntimeException("Invalid tile index y=" + y + " for zoom " + zoom);

		TileStore ts = TileStore.getInstance();

		// Thread.sleep(2000);

		// Test code for creating random download failures
		// if (Math.random()>0.7) throw new
		// IOException("intentionally download error");

		/**
		 * If the desired tile already exist in the persistent tilestore and
		 * settings is to use the tile store
		 */
		Settings s = Settings.getInstance();
		String tileFileName = "y" + y + "x" + x + "." + mapSource.getTileType();

		TileStoreEntry tile = null;
		if (s.tileStoreEnabled) {

			// Copy the file from the persistent tilestore instead of
			// downloading it from internet.
			tile = ts.getTile(x, y, zoom, mapSource);
			boolean expired = isTileExpired(tile);
			if (tile != null) {
				if (expired)
					log.trace("Expired: " + mapSource.getName() + " " + tile);
				synchronized (tileArchive) {
					log.trace("Tile used from tilestore");
					tileArchive.writeFileFromData(tileFileName, tile.getData());
				}
				return 0;
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
		String url = mapSource.getTileUrl(zoom, x, y);
		if (url == null)
			throw new UnrecoverableDownloadException("Tile x=" + x + " y=" + y + " zoom=" + zoom
					+ " is not a valid tile in map source " + mapSource);

		log.trace("Downloading " + url);
		URL u = new URL(url);
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();

		huc.setRequestMethod("GET");

		Settings s = Settings.getInstance();
		huc.setConnectTimeout(1000 * s.getConnectionTimeout());
		huc.addRequestProperty("User-agent", s.getUserAgent());
		huc.connect();

		int code = huc.getResponseCode();

		if (code != HttpURLConnection.HTTP_OK)
			throw new IOException("Invaild HTTP response: " + code);

		String eTag = huc.getHeaderField("ETag");
		long timeLastModified = huc.getLastModified();
		long timeExpires = huc.getExpiration();

		byte[] data = loadTileInBuffer(huc);
		Utilities.checkForInterruption();
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

		String url = mapSource.getTileUrl(zoom, x, y);
		if (url == null)
			throw new UnrecoverableDownloadException("Tile x=" + x + " y=" + y + " zoom=" + zoom
					+ " is not a valid tile in map source " + mapSource);

		if (log.isTraceEnabled())
			log.trace(String.format("Downloading (zoom,x,y) %d/%d/%d %s", zoom, x, y, url));
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();

		conn.setRequestMethod("GET");

		boolean conditionalRequest = false;

		switch (mapSource.getTileUpdate()) {
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
		conn.setConnectTimeout(1000 * s.getConnectionTimeout());
		conn.addRequestProperty("User-agent", s.getUserAgent());
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

		if (code != HttpURLConnection.HTTP_OK)
			throw new IOException("Invaild HTTP response: " + code);

		String eTag = conn.getHeaderField("ETag");
		long timeLastModified = conn.getLastModified();
		long timeExpires = conn.getExpiration();

		byte[] data = loadTileInBuffer(conn);
		Utilities.checkForInterruption();
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
			// server had set an expiry time
			return (expiredTime < System.currentTimeMillis());
		} else {
			return (tileStoreEntry.getTimeDownloaded() + FILE_AGE_ONE_DAY > System
					.currentTimeMillis());
		}
	}

	protected static byte[] loadTileInBuffer(HttpURLConnection conn) throws IOException {
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
		if (bout.size() == 0)
			return null;
		return bout.toByteArray();
	}
}