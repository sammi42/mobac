package tac.program;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
import tac.utilities.Utilities;

public class TileDownLoader {

	static {
		System.setProperty("http.maxConnections", "10");
	}

	private static int MAX_DOWNLOAD_SIZE = 1024 * 1024; // 1 MB max tile size to
	// download

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

		if (s.tileStoreEnabled) {

			// Copy the file from the persistent tilestore instead of
			// downloading it from internet.
			byte[] data = ts.getTileData(x, y, zoom, mapSource);
			if (data != null) {
				synchronized (tileArchive) {
					log.trace("Tile used from tilestore");
					tileArchive.writeFileFromData(tileFileName, data);
				}
				return 0;
			}
		}
		byte[] data = downloadTileAndUpdateStore(maxTileIndex, y, zoom, mapSource);
		synchronized (tileArchive) {
			tileArchive.writeFileFromData(tileFileName, data);
		}
		return (data == null) ? 0 : data.length;
	}

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

		int bytesRead = 0;
		InputStream is = huc.getInputStream();
		byte[] data = null;
		try {

			int contentLen = huc.getContentLength();
			if (contentLen > MAX_DOWNLOAD_SIZE)
				throw new UnrecoverableDownloadException("Remote resource '" + url + "' of size "
						+ contentLen + "bytes exceeds the maximum download size of "
						+ MAX_DOWNLOAD_SIZE + " bytes.");
			if (contentLen < 0) {
				byte[] b = new byte[8192];
				ByteArrayOutputStream buf = new ByteArrayOutputStream(8192);
				while ((bytesRead = is.read(b)) > 0) {
					buf.write(b, 0, bytesRead);
				}
				data = buf.toByteArray();
			} else {
				DataInputStream din = new DataInputStream(is);
				data = new byte[contentLen];
				din.readFully(data);
				if (din.read() >= 0)
					throw new IOException("Data after content end available!");
			}
			Utilities.checkForInterruption();
			if (mapSource.allowFileStore() && s.tileStoreEnabled) {
				// We are writing simultaneously to the target file
				// and the file in the tile store
				TileStore.getInstance().putTileData(data, x, y, zoom, mapSource, timeLastModified,
						timeExpires, eTag);
			}
			Utilities.checkForInterruption();
		} finally {
			Utilities.closeStream(is);
		}
		return data;
	}
}