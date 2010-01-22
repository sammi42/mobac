package mobac.program.atlascreators;

import mobac.program.interfaces.MapInterface;
import mobac.utilities.tar.TarIndex;

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
