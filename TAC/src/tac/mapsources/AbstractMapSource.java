package tac.mapsources;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.mapsources.mapspace.Power2MapSpace;

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

	public MapSpace getMapSpace() {
		return Power2MapSpace.INSTANCE;
	}

}
