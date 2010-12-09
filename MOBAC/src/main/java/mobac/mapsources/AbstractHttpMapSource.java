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
package mobac.mapsources;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import mobac.exceptions.MapSourceInitializationException;
import mobac.exceptions.TileException;
import mobac.gui.mapview.JMapViewer;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.download.TileDownLoader;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSourceListener;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.TileStoreEntry;

import org.apache.log4j.Logger;

/**
 * Abstract base class for map sources.
 */
public abstract class AbstractHttpMapSource implements HttpMapSource {

	protected Logger log;
	private boolean initialized = false;
	protected String name;
	protected int minZoom;
	protected int maxZoom;
	protected TileImageType tileType;
	protected HttpMapSource.TileUpdate tileUpdate;
	protected MapSpace mapSpace = MercatorPower2MapSpace.INSTANCE_256;

	public AbstractHttpMapSource(String name, int minZoom, int maxZoom, TileImageType tileType) {
		this(name, minZoom, maxZoom, tileType, HttpMapSource.TileUpdate.None);
	}

	public AbstractHttpMapSource(String name, int minZoom, int maxZoom, TileImageType tileType,
			HttpMapSource.TileUpdate tileUpdate) {
		log = Logger.getLogger(this.getClass());
		this.name = name;
		this.minZoom = minZoom;
		this.maxZoom = Math.min(maxZoom, JMapViewer.MAX_ZOOM);
		this.tileType = tileType;
		this.tileUpdate = tileUpdate;
	}

	public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley) throws IOException {
		String url = getTileUrl(zoom, tilex, tiley);
		if (url == null)
			return null;
		return (HttpURLConnection) new URL(url).openConnection();
	}

	public abstract String getTileUrl(int zoom, int tilex, int tiley);

	/**
	 * Can be used to e.g. retrieve the url pattern before the first call
	 */
	protected final void initialize() {
		if (initialized)
			return;
		// Prevent multiple initializations in case of multi-threaded access
		try {
			synchronized (this.getClass()) {
				if (initialized)
					// Another thread has already completed initialization while this one was blocked
					return;
				initialized = true;
				initernalInitialize();
				log.debug("Map source has been initialized");
			}
		} catch (Exception e) {
			log.error("Map source initialization failed: "+e.getMessage(),e);
			// TODO: inform user 
		}
	}

	protected void initernalInitialize() throws MapSourceInitializationException {
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		if (loadMethod == LoadMethod.CACHE) {
			TileStoreEntry entry = TileStore.getInstance().getTile(x, y, zoom, this);
			if (entry == null)
				return null;
			byte[] data = entry.getData();
			if (Thread.currentThread() instanceof MapSourceListener) {
				((MapSourceListener) Thread.currentThread()).tileDownloaded(data.length);
			}
			return data;
		} else {
			initialize();
			return TileDownLoader.getImage(x, y, zoom, this);
		}
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		byte[] data = getTileData(zoom, x, y, loadMethod);
		if (data == null)
			return null;
		return ImageIO.read(new ByteArrayInputStream(data));
	}

	public int getMaxZoom() {
		return maxZoom;
	}

	public int getMinZoom() {
		return minZoom;
	}

	public String getName() {
		return name;
	}

	public String getStoreName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public TileImageType getTileImageType() {
		return tileType;
	}

	public HttpMapSource.TileUpdate getTileUpdate() {
		return tileUpdate;
	}

	public boolean allowFileStore() {
		return true;
	}

	public MapSpace getMapSpace() {
		return mapSpace;
	}

	public Color getBackgroundColor() {
		return Color.BLACK;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MapSource))
			return false;
		MapSource other = (MapSource) obj;
		return other.getName().equals(getName());
	}

}
