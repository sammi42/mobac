package tac.program.atlascreators;

import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;

/**
 * Created by didoa [at] users.sourceforge.net
 */
public class OSMTracker extends AndNav {

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		this.additionalFileExt = "";
	}

}
