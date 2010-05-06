package unittests.helper;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import mobac.exceptions.MapCreationException;
import mobac.program.atlascreators.AtlasCreator;

/**
 * A simple {@link AtlasCreator} implementation that does nothing. Can be used
 * in case only tile downloading and saving in the tile store is the test
 * target.
 */
public class DummyAtlasCreator extends AtlasCreator {

	public DummyAtlasCreator() {
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
	}

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return true;
	}

}
