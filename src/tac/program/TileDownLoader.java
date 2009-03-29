package tac.program;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.utilities.Utilities;

public class TileDownLoader {

	static {
		System.setProperty("http.maxConnections", "10");
	}

	private static int MAX_BUFFER_SIZE = 64 * 1024; // 64 KB receive & copy
	// buffer

	private static Logger log = Logger.getLogger(TileDownLoader.class);

	public static int getImage(int x, int y, int zoom, File destinationDirectory,
			TileSource tileSource, boolean isAtlasDownload) throws IOException,
			InterruptedException {

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
		String tileFileName = "y" + y + "x" + x + "." + tileSource.getTileType();
		File destFile = new File(destinationDirectory, tileFileName);

		if (s.isTileStoreEnabled()) {

			// Copy the file from the persistent tilestore instead of
			// downloading it from internet.
			try {
				if (ts.copyStoredTileTo(destFile, x, y, zoom, tileSource)) {
					log.trace("Tile used from tilestore");
					return 0;
				}
			} catch (IOException e) {
			}
		}

		String url = tileSource.getTileUrl(zoom, x, y);

		log.trace("Downloading " + url);
		URL u = new URL(url);
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();

		huc.setRequestMethod("GET");

		huc.setConnectTimeout(1000 * s.getConnectionTimeout());
		huc.addRequestProperty("User-agent", s.getUserAgent());
		huc.connect();

		InputStream is = huc.getInputStream();
		int code = huc.getResponseCode();

		if (code != HttpURLConnection.HTTP_OK)
			throw new IOException("Invaild HTTP response: " + code);

		File tileFile = new File(destinationDirectory, tileFileName);
		File tilestoreFile = null;
		OutputStream tileFileStream = new FileOutputStream(tileFile);
		OutputStream tilestoreFileStream = null;
		if (s.isTileStoreEnabled()) {
			// We are writing simultaneously to the target file
			// and the file in the tile store
			tilestoreFile = ts.getTileFile(x, y, zoom, tileSource);
			tilestoreFileStream = new FileOutputStream(tilestoreFile, false);
		}

		int bytesRead = 0;
		int sumBytes = 0;

		boolean success = false;
		try {
			int bufferSize = huc.getContentLength();
			if (bufferSize <= 1024)
				// Content length returned by server is invalid or very short
				bufferSize = MAX_BUFFER_SIZE;
			else
				bufferSize = Math.min(bufferSize, MAX_BUFFER_SIZE);
			byte[] buffer = new byte[bufferSize];
			while ((bytesRead = is.read(buffer)) > 0) {
				sumBytes += bytesRead;
				tileFileStream.write(buffer, 0, bytesRead);
				if (tilestoreFileStream != null)
					tilestoreFileStream.write(buffer, 0, bytesRead);
			}
			success = true;
		} finally {
			Utilities.closeStream(tileFileStream);
			Utilities.closeStream(tilestoreFileStream);
			if ((!success) || (sumBytes == 0)) {
				// In case of an error while download or an empty file we have
				// an invalid tile image we don't want -> delete it
				tileFile.delete();
				if (tilestoreFile != null)
					tilestoreFile.delete();
			}
		}

		return sumBytes;
	}
}