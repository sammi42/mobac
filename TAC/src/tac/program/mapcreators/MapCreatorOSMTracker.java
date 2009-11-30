package tac.program.mapcreators;

import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;

/**
 * Created by didoa [at] users.sourceforge.net
 */
public class MapCreatorOSMTracker extends MapCreatorAndNav {

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		this.additionalFileExt = "";
	}

}
