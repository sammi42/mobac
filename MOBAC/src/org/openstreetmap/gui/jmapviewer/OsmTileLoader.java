package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.TileImageCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderJobCreator;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

/**
 * A {@link TileLoaderJobCreator} implementation that loads tiles from OSM via
 * HTTP.
 * 
 * @author Jan Peter Stotz
 */
public class OsmTileLoader implements TileLoaderJobCreator {

	private static final Logger log = Logger.getLogger(OsmTileLoader.class);

	protected TileLoaderListener listener;

	public OsmTileLoader(TileLoaderListener listener) {
		this.listener = listener;
	}

	public Runnable createTileLoaderJob(final MapSource source, final int tilex, final int tiley,
			final int zoom) {
		return new Runnable() {

			InputStream input = null;

			public void run() {
				TileImageCache cache = listener.getTileImageCache();
				Tile tile;
				synchronized (cache) {
					tile = cache.getTile(source, tilex, tiley, zoom);
					if (tile == null || tile.isLoaded() || tile.loading)
						return;
					tile.loading = true;
				}
				try {
					// Thread.sleep(500);
					input = loadTileFromOsm(tile).getInputStream();
					tile.loadImage(input);
					tile.setLoaded(true);
					listener.tileLoadingFinished(tile, true);
					input.close();
					input = null;
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

		};
	}

	protected HttpURLConnection loadTileFromOsm(Tile tile) throws IOException {
		HttpURLConnection urlConn = tile.getUrlConnection();
		urlConn.setReadTimeout(30000); // 30 seconds read timeout
		return urlConn;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
