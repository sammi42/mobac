package mobac.program.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mobac.mapsources.MapSourcesManager;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class MapSourceAdapter extends XmlAdapter<String, MapSource> {

	@Override
	public String marshal(MapSource mapSource) throws Exception {
		return mapSource.getName();
	}

	@Override
	public MapSource unmarshal(String name) throws Exception {
		return MapSourcesManager.getInstance().getSourceByName(name);
	}

}
