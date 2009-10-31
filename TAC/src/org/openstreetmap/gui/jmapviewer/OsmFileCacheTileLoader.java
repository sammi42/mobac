package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.TileImageCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderJobCreator;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

import tac.program.TileDownLoader;
import tac.tilestore.TileStore;

/**
 * A {@link TileLoaderJobCreator} implementation that loads tiles from OSM via
 * HTTP and saves all loaded files in a directory located in the the temporary
 * directory. If a tile is present in this file cache it will not be loaded from
 * OSM again.
 * 
 * @author Jan Peter Stotz
 */
public class OsmFileCacheTileLoader extends OsmTileLoader {

	private static final Logger log = Logger.getLogger(OsmFileCacheTileLoader.class);

	public static final long FILE_AGE_ONE_DAY = 1000 * 60 * 60 * 24;
	public static final long FILE_AGE_ONE_WEEK = FILE_AGE_ONE_DAY * 7;

	protected TileStore tileStore;

	protected long maxCacheFileAge = FILE_AGE_ONE_WEEK;
	protected long recheckAfter = FILE_AGE_ONE_DAY;

	public OsmFileCacheTileLoader(TileLoaderListener map) {
		super(map);
		tileStore = TileStore.getInstance();
	}

	public Runnable createTileLoaderJob(final MapSource source, final int tilex, final int tiley,
			final int zoom) {
		return new FileLoadJob(source, tilex, tiley, zoom);
	}

	protected class FileLoadJob implements Runnable {
		InputStream input = null;

		int tilex, tiley, zoom;
		Tile tile;
		MapSource mapSource;
		boolean fileTilePainted = false;

		public FileLoadJob(MapSource source, int tilex, int tiley, int zoom) {
			super();
			this.mapSource = source;
			this.tilex = tilex;
			this.tiley = tiley;
			this.zoom = zoom;
		}

		public void run() {
			TileImageCache cache = listener.getTileImageCache();
			synchronized (cache) {
				tile = cache.getTile(mapSource, tilex, tiley, zoom);
				if (tile == null || tile.isLoaded() || tile.loading)
					return;
				tile.loading = true;
			}
			if (loadTileFromStore())
				return;
			if (fileTilePainted) {
				Runnable job = new Runnable() {

					public void run() {
						loadOrUpdateTile();
					}
				};
				JobDispatcher.getInstance().addJob(job);
			} else {
				loadOrUpdateTile();
			}
		}

		protected void loadOrUpdateTile() {

			try {

				// log.finest("Loading tile from OSM: " + tile);
				// HttpURLConnection urlConn = loadTileFromOsm(tile);
				// if (tileFile != null) {
				// switch (mapSource.getTileUpdate()) {
				// case IfModifiedSince:
				// urlConn.setIfModifiedSince(fileAge);
				// break;
				// case LastModified:
				// if (!isOsmTileNewer(fileAge)) {
				// log.finest("LastModified test: local version is up to date: "
				// + tile);
				// tile.setLoaded(true);
				// tileFile.setLastModified(System.currentTimeMillis() -
				// maxCacheFileAge
				// + recheckAfter);
				// return;
				// }
				// break;
				// }
				// }
				// if (mapSource.getTileUpdate() == TileUpdate.ETag
				// || mapSource.getTileUpdate() == TileUpdate.IfNoneMatch) {
				// if (tileFile != null) {
				// String fileETag = loadETagfromFile();
				// if (fileETag != null) {
				// switch (mapSource.getTileUpdate()) {
				// case IfNoneMatch:
				// urlConn.addRequestProperty("If-None-Match", fileETag);
				// break;
				// case ETag:
				// if (hasOsmTileETag(fileETag)) {
				// tile.setLoaded(true);
				// tileFile.setLastModified(System.currentTimeMillis()
				// - maxCacheFileAge + recheckAfter);
				// return;
				// }
				// }
				// }
				// }
				//
				// String eTag = urlConn.getHeaderField("ETag");
				// saveETagToFile(eTag);
				// }
				// if (urlConn.getResponseCode() == 304) {
				// // If we are isModifiedSince or If-None-Match has been set
				// // and the server answers with a HTTP 304 = "Not Modified"
				// log.finest("ETag test: local version is up to date: " +
				// tile);
				// tile.setLoaded(true);
				// tileFile.setLastModified(System.currentTimeMillis() -
				// maxCacheFileAge
				// + recheckAfter);
				// return;
				// }

				byte[] buffer = TileDownLoader.downloadTileAndUpdateStore(tilex, tiley, zoom,
						mapSource);
				if (buffer != null) {
					tile.loadImage(new ByteArrayInputStream(buffer));
					tile.setLoaded(true);
					listener.tileLoadingFinished(tile, true);
				} else {
					tile.setLoaded(true);
				}
			} catch (Exception e) {
				tile.setImage(Tile.ERROR_IMAGE);
				listener.tileLoadingFinished(tile, false);
				if (input == null)
					log.error("failed loading " + zoom + "/" + tilex + "/" + tiley + " "
							+ e.getMessage());
			} finally {
				tile.loading = false;
				tile.setLoaded(true);
			}
		}

		protected boolean loadTileFromStore() {
			try {
				byte[] tileData = tileStore.getTileData(tilex, tiley, zoom, mapSource);
				if (tileData == null)
					return false;
				tile.loadImage(new ByteArrayInputStream(tileData));
				// fileAge = tileFile.lastModified();
				// boolean oldTile = System.currentTimeMillis() - fileAge >
				// maxCacheFileAge;
				// if (!oldTile) {
				// tile.setLoaded(true);
				// listener.tileLoadingFinished(tile, true);
				// fileTilePainted = true;
				// return true;
				// }
				listener.tileLoadingFinished(tile, true);
				fileTilePainted = true;
				return true;
			} catch (Exception e) {
				log.error("", e);
			}
			return false;
		}

		protected byte[] loadTileInBuffer(URLConnection urlConn) throws IOException {
			input = urlConn.getInputStream();
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

		/**
		 * Performs a <code>HEAD</code> request for retrieving the
		 * <code>LastModified</code> header value.
		 * 
		 * Note: This does only work with servers providing the
		 * <code>LastModified</code> header:
		 * <ul>
		 * <li>{@link OsmTileLoader#MAP_OSMA} - supported</li>
		 * <li>{@link OsmTileLoader#MAP_MAPNIK} - not supported</li>
		 * </ul>
		 * 
		 * @param fileAge
		 * @return <code>true</code> if the tile on the server is newer than the
		 *         file
		 * @throws IOException
		 */
		protected boolean isOsmTileNewer(long fileAge) throws IOException {
			URL url;
			url = new URL(tile.getUrl());
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			prepareHttpUrlConnection(urlConn);
			urlConn.setRequestMethod("HEAD");
			urlConn.setReadTimeout(30000); // 30 seconds read timeout
			long lastModified = urlConn.getLastModified();
			if (lastModified == 0)
				return true; // no LastModified time returned
			return (lastModified > fileAge);
		}

		protected boolean hasOsmTileETag(String eTag) throws IOException {
			URL url;
			url = new URL(tile.getUrl());
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			prepareHttpUrlConnection(urlConn);
			urlConn.setRequestMethod("HEAD");
			urlConn.setReadTimeout(30000); // 30 seconds read timeout
			String osmETag = urlConn.getHeaderField("ETag");
			if (osmETag == null)
				return true;
			return (osmETag.equals(eTag));
		}

		// protected void saveETagToFile(String eTag) {
		// try {
		// FileOutputStream f = new FileOutputStream(tileCacheDir + "/" +
		// tile.getZoom() + "_"
		// + tile.getXtile() + "_" + tile.getYtile() + ETAG_FILE_EXT);
		// f.write(eTag.getBytes(ETAG_CHARSET.name()));
		// f.close();
		// } catch (Exception e) {
		// System.err.println("Failed to save ETag: " +
		// e.getLocalizedMessage());
		// }
		// }

		// protected String loadETagfromFile() {
		// try {
		// FileInputStream f = new FileInputStream(tileCacheDir + "/" +
		// tile.getZoom() + "_"
		// + tile.getXtile() + "_" + tile.getYtile() + ETAG_FILE_EXT);
		// byte[] buf = new byte[f.available()];
		// f.read(buf);
		// f.close();
		// return new String(buf, ETAG_CHARSET.name());
		// } catch (Exception e) {
		// return null;
		// }
		// }

	}

	public long getMaxFileAge() {
		return maxCacheFileAge;
	}

	/**
	 * Sets the maximum age of the local cached tile in the file system. If a
	 * local tile is older than the specified file age
	 * {@link OsmFileCacheTileLoader} will connect to the tile server and check
	 * if a newer tile is available using the mechanism specified for the
	 * selected tile source/server.
	 * 
	 * @param maxFileAge
	 *            maximum age in milliseconds
	 * @see #FILE_AGE_ONE_DAY
	 * @see #FILE_AGE_ONE_WEEK
	 * @see MapSource#getTileUpdate()
	 */
	public void setCacheMaxFileAge(long maxFileAge) {
		this.maxCacheFileAge = maxFileAge;
	}

}
