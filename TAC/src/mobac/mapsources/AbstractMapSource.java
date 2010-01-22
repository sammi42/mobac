package mobac.mapsources;

import java.awt.Color;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import mobac.mapsources.mapspace.MercatorPower2MapSpace;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


/**
 * Abstract base class for map sources.
 */
public abstract class AbstractMapSource implements MapSource {

	protected String name;
	protected int minZoom;
	protected int maxZoom;
	protected String tileType;
	protected TileUpdate tileUpdate;

	public AbstractMapSource(String name, int minZoom, int maxZoom, String tileType) {
		this(name, minZoom, maxZoom, tileType, TileUpdate.None);
	}

	public AbstractMapSource(String name, int minZoom, int maxZoom, String tileType,
			TileUpdate tileUpdate) {
		this.name = name;
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
		this.tileType = tileType;
		this.tileUpdate = tileUpdate;
	}

	public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley)
			throws IOException {
		String url = getTileUrl(zoom, tilex, tiley);
		if (url == null)
			return null;
		return (HttpURLConnection) new URL(url).openConnection();
	}

	public abstract String getTileUrl(int zoom, int tilex, int tiley);

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

	public String getTileType() {
		return tileType;
	}

	public TileUpdate getTileUpdate() {
		return tileUpdate;
	}

	public boolean allowFileStore() {
		return true;
	}

	public MapSpace getMapSpace() {
		return MercatorPower2MapSpace.INSTANCE_256;
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
