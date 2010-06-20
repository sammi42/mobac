package unittests.helper;

import java.util.Vector;

import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.impl.LocalhostTestSource;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class TestMapSourcesManager extends MapSourcesManager {

	private final MapSource theMapSource;

	public TestMapSourcesManager(int port, String tileType) {
		super();
		theMapSource = new LocalhostTestSource("Localhost test", port, tileType, false);
		install();
	}

	public TestMapSourcesManager(MapSource mapSource) {
		super();
		theMapSource = mapSource;
		install();
	}

	public void install() {
		INSTANCE = this;
	}

	@Override
	public Vector<MapSource> getAllMapSources() {
		Vector<MapSource> v = new Vector<MapSource>(1);
		v.add(theMapSource);
		return v;
	}

	@Override
	public MapSource getDefaultMapSource() {
		return theMapSource;
	}

	@Override
	public Vector<MapSource> getEnabledMapSources() {
		return getAllMapSources();
	}

	@Override
	public MapSource getSourceByName(String name) {
		return theMapSource;
	}

}
