package tac.program;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.UnrecoverableDownloadException;
import tac.program.model.Settings;
import tac.tar.TarIndexedArchive;
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

		if (s.isTileStoreEnabled()) {

			// Copy the file from the persistent tilestore instead of
			// downloading it from internet.
			try {
				File tsTileFile = ts.getTileFile(x, y, zoom, mapSource);
				if (tsTileFile!= null && tsTileFile.exists()) {
					byte[] data = Utilities.getFileBytes(tsTileFile);
					synchronized (tileArchive) {
						log.trace("Tile used from tilestore");
						tileArchive.writeFileFromData(tileFileName, data);
					}
					return 0;
				}
			} catch (IOException e) {
			}
		}

		String url = mapSource.getTileUrl(zoom, x, y);
		if (url == null)
			throw new UnrecoverableDownloadException("Tile x=" + x + " y=" + y + " zoom=" + zoom
					+ " is not a valid tile in map source " + mapSource);

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

		int bytesRead = 0;
		int sumBytes = 0;

		boolean success = false;
		int contentLen = huc.getContentLength();
		if (contentLen > MAX_DOWNLOAD_SIZE)
			throw new UnrecoverableDownloadException("Remote resource '" + url + "' of size "
					+ contentLen + "bytes exceeds the maximum download size of "
					+ MAX_DOWNLOAD_SIZE + " bytes.");
		byte[] data = null;
		if (contentLen < 0) {
			byte[] b = new byte[8192];
			ByteArrayOutputStream buf = new ByteArrayOutputStream(8192);
			while ((bytesRead = is.read(b)) > 0) {
				sumBytes += bytesRead;
				buf.write(b, 0, bytesRead);
			}
			data = buf.toByteArray();
		} else {
			DataInputStream din = new DataInputStream(is);
			data = new byte[contentLen];
			din.readFully(data);
			sumBytes = contentLen;
			if (din.read() >= 0)
				throw new IOException("Data after content end available!");
		}
		Utilities.checkForInterruption();
		File tilestoreFile = null;
		OutputStream tilestoreFileStream = null;
		if (mapSource.allowFileStore() && s.isTileStoreEnabled()) {
			// We are writing simultaneously to the target file
			// and the file in the tile store
			tilestoreFile = ts.getTileFile(x, y, zoom, mapSource);
			tilestoreFileStream = new FileOutputStream(tilestoreFile, false);
			tilestoreFileStream.write(data);
		}
		Utilities.checkForInterruption();
		synchronized (tileArchive) {
			tileArchive.writeFileFromData(tileFileName, data);
		}
		try {
			success = true;
		} finally {
			Utilities.closeStream(tilestoreFileStream);
			if ((!success) || (sumBytes == 0)) {
				// In case of an error while download or an empty file we have
				// an invalid tile image we don't want -> delete it
				if (tilestoreFile != null)
					tilestoreFile.delete();
			}
		}

		return sumBytes;
	}

}