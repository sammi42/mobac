package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.TileImageCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderJobCreator;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

import tac.program.download.TileDownLoader;
import tac.tilestore.TileStore;
import tac.tilestore.TileStoreEntry;

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

	protected TileStore tileStore;

	public OsmFileCacheTileLoader(TileLoaderListener map) {
		super(map);
		tileStore = TileStore.getInstance();
	}

	public Runnable createTileLoaderJob(final MapSource source, final int tilex, final int tiley,
			final int zoom) {
		return new FileLoadJob(source, tilex, tiley, zoom);
	}

	protected class FileLoadJob implements Runnable {

		final int tilex, tiley, zoom;
		final MapSource mapSource;
		Tile tile;
		boolean fileTilePainted = false;
		protected TileStoreEntry tileStoreEntry;

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

				byte[] buffer;
				if (tileStoreEntry == null)
					buffer = TileDownLoader.downloadTileAndUpdateStore(tilex, tiley, zoom,
							mapSource);
				else {
					TileDownLoader.updateStoredTile(tileStoreEntry, mapSource);
					buffer = tileStoreEntry.getData();
				}
				if (buffer != null) {
					tile.loadImage(new ByteArrayInputStream(buffer));
					tile.setLoaded(true);
					listener.tileLoadingFinished(tile, true);
				} else {
					tile.setLoaded(true);
				}
			} catch (Exception e) {
				log.trace("Downloading of tile " + tile + " failed", e);
				tile.setImage(Tile.ERROR_IMAGE);
				listener.tileLoadingFinished(tile, false);
			} finally {
				tile.loading = false;
				tile.setLoaded(true);
			}
		}

		protected boolean loadTileFromStore() {
			try {
				tileStoreEntry = tileStore.getTile(tilex, tiley, zoom, mapSource);
				if (tileStoreEntry == null)
					return false;
				tile.loadImage(new ByteArrayInputStream(tileStoreEntry.getData()));
				listener.tileLoadingFinished(tile, true);
				if (TileDownLoader.isTileExpired(tileStoreEntry))
					return false;
				// fileAge = tileFile.lastModified();
				// boolean oldTile = System.currentTimeMillis() - fileAge >
				// maxCacheFileAge;
				// if (!oldTile) {
				// tile.setLoaded(true);
				// listener.tileLoadingFinished(tile, true);
				// fileTilePainted = true;
				// return true;
				// }
				fileTilePainted = true;
				return true;
			} catch (Exception e) {
				log.error("", e);
			}
			return false;
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
			HttpURLConnection urlConn = tile.getUrlConnection();
			urlConn.setRequestMethod("HEAD");
			urlConn.setReadTimeout(30000); // 30 seconds read timeout
			long lastModified = urlConn.getLastModified();
			if (lastModified == 0)
				return true; // no LastModified time returned
			return (lastModified > fileAge);
		}

		protected boolean hasOsmTileETag(String eTag) throws IOException {
			HttpURLConnection urlConn = tile.getUrlConnection();
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

}
