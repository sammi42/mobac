package mobac.mapsources;

import java.util.Vector;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public abstract class MapSourcesManager {

	protected static MapSourcesManager INSTANCE = null;

	public static MapSourcesManager getInstance() {
		return INSTANCE;
	}

	public abstract Vector<MapSource> getAllMapSources();

	public abstract Vector<MapSource> getEnabledMapSources();

	public abstract MapSource getDefaultMapSource();

	public abstract MapSource getSourceByName(String name);
}
