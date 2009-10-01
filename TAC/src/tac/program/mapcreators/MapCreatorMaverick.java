package tac.program.mapcreators;

import java.io.File;

import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;

/**
 * Creates maps using the <a
 * href="http://www.codesector.com/maverick.php">Maverick</a> atlas format
 * (Android application).
 */
public class MapCreatorMaverick extends MapCreatorAndNav {

	public MapCreatorMaverick(MapInterface map, TarIndex tarTileIndex, File atlasDir) {
		super(map, tarTileIndex, atlasDir);
		this.additionalFileExt = ".tile";
	}

}
