package tac.mapsources;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

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
		this.name = name;
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
		this.tileType = tileType;
		tileUpdate = TileUpdate.None;
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
	

}
