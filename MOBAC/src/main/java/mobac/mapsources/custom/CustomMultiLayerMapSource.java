package mobac.mapsources.custom;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.mapsources.MultiLayerMapSource;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

/**
 * Custom tile store provider for multi-layer map sources, configurable via settings.xml.
 */
@XmlRootElement
public class CustomMultiLayerMapSource extends CustomMapSource implements MultiLayerMapSource {

	@XmlElement(required = true, name="backgroundMapSource")
	private CustomMapSource background = null;

	public MapSource getBackgroundMapSource() {
		return background;
	}

}
