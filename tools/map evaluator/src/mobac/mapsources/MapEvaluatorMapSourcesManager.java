package mobac.mapsources;

import java.util.Vector;

import mobac.mapsources.mappacks.openstreetmap.Mapnik;
import mobac.program.interfaces.MapSource;

public class MapEvaluatorMapSourcesManager extends MapSourcesManager {

	private Vector<MapSource> mapSources = new Vector<MapSource>();

	public static void initialitze() {
		MapSourcesManager.INSTANCE = new MapEvaluatorMapSourcesManager();
	}

	private MapEvaluatorMapSourcesManager() {
		mapSources.add(new Mapnik());
	}

	@Override
	public void addMapSource(MapSource mapSource) {
	}

	@Override
	public Vector<MapSource> getAllAvailableMapSources() {
		return mapSources;
	}

	@Override
	public Vector<MapSource> getAllLayerMapSources() {
		return mapSources;
	}

	@Override
	public Vector<MapSource> getAllMapSources() {
		return mapSources;
	}

	@Override
	public MapSource getDefaultMapSource() {
		return mapSources.get(0);
	}

	@Override
	public Vector<MapSource> getDisabledMapSources() {
		return new Vector<MapSource>();
	}

	@Override
	public Vector<MapSource> getEnabledOrderedMapSources() {
		return mapSources;
	}

	@Override
	public MapSource getSourceByName(String name) {
		throw new RuntimeException("Not implemented");
	}

}
