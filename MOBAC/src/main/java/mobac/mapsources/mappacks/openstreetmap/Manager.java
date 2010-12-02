package mobac.mapsources.mappacks.openstreetmap;

import mobac.program.interfaces.MapPackManager;
import mobac.program.interfaces.MapSource;

public class Manager implements MapPackManager {

	private static final MapSource[] MAP_SOURCES = new MapSource[] { //
	//
	};

	public MapSource[] getMapSources() {
		return MAP_SOURCES;
	}

}
