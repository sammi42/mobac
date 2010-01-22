package mobac.mapsources;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

/**
 * @author User ReRo forum.pocketnavigation.de
 */
public interface MultiLayerMapSource extends MapSource {

	public MapSource getBackgroundMapSource();

}
