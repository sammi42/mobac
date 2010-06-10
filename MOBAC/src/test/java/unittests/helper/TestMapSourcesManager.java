package unittests.helper;

import java.util.Vector;

import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.impl.LocalhostTestSource;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class TestMapSourcesManager extends MapSourcesManager {

	private final MapSource localhostMapSource;

	public TestMapSourcesManager(int port) {
		super();
		localhostMapSource = new LocalhostTestSource("Localhost test", port, false);
		INSTANCE = this;
	}

	@Override
	public Vector<MapSource> getAllMapSources() {
		Vector<MapSource> v = new Vector<MapSource>(1);
		v.add(localhostMapSource);
		return v;
	}

	@Override
	public MapSource getDefaultMapSource() {
		return localhostMapSource;
	}

	@Override
	public Vector<MapSource> getEnabledMapSources() {
		return getAllMapSources();
	}

	@Override
	public MapSource getSourceByName(String name) {
		return localhostMapSource;
	}

}
