package tac.program.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.mapsources.MapSourcesManager;

public class MapSourceAdapter extends XmlAdapter<String, MapSource> {

	@Override
	public String marshal(MapSource mapSource) throws Exception {
		return mapSource.getName();
	}

	@Override
	public MapSource unmarshal(String name) throws Exception {
		return MapSourcesManager.getSourceByName(name);
	}

}
