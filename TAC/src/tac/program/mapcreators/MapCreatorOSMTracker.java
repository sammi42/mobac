package tac.program.mapcreators;

import java.io.File;

import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;

/**
 * Created by didoa [at] users.sourceforge.net
 */
public class MapCreatorOSMTracker extends MapCreatorAndNav {

	public MapCreatorOSMTracker(MapInterface map, TarIndex tarTileIndex, File atlasDir) {
		super(map, tarTileIndex, atlasDir);
		this.additionalFileExt = "";
	}

}
