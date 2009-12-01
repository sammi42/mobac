package tac.program.atlascreators;

import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;

/**
 * Creates maps using the <a
 * href="http://www.codesector.com/maverick.php">Maverick</a> atlas format
 * (Android application).
 */
public class Maverick extends AndNav {

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		this.additionalFileExt = ".tile";
	}

}
