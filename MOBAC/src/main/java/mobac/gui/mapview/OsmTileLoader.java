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
package mobac.gui.mapview;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import mobac.gui.mapview.Tile.TileState;
import mobac.gui.mapview.interfaces.TileImageCache;
import mobac.gui.mapview.interfaces.TileLoaderJobCreator;
import mobac.gui.mapview.interfaces.TileLoaderListener;
import mobac.program.interfaces.MapSource;

import org.apache.log4j.Logger;

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
					if (tile == null || tile.tileState == TileState.TS_LOADED
							|| tile.tileState == TileState.TS_ERROR)
						return;
				}
				try {
					// Thread.sleep(500);
					input = loadTileFromOsm(tile).getInputStream();
					tile.loadImage(input);
					tile.setTileState(TileState.TS_LOADED);
					listener.tileLoadingFinished(tile, true);
					input.close();
					input = null;
				} catch (Exception e) {
					tile.setErrorImage();
					listener.tileLoadingFinished(tile, false);
					if (input == null)
						log.error("failed loading " + zoom + "/" + tilex + "/" + tiley + " "
								+ e.getMessage());
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
