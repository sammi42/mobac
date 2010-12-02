package mobac.mapsources.mappacks.google;

import mobac.program.interfaces.MapPackManager;
import mobac.program.interfaces.MapSource;

public class Manager implements MapPackManager {

	private static final MapSource[] MAP_SOURCES = new MapSource[] { //
	//
			new GoogleMaps(), // 
			new GoogleMapMaker(), //
			new GoogleMapsChina(), // 
			new GoogleMapsKorea(), // 
			new GoogleEarth(), //
			new GoogleHybrid(), // 
			new GoogleTerrain(), // 
	};

	public MapSource[] getMapSources() {
		return MAP_SOURCES;
	}

}
