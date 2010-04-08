package mobac.program.atlascreators;

import java.io.File;
import java.io.IOException;

import mobac.exceptions.AtlasTestException;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;

/**
 * Creates a tile cache structure as used by <a
 * href="http://www.pathaway.com/">PathAway</a> (for WindowsMobile, Symbian,
 * Palm)
 */
public class PathAway extends OSMTracker {

	public PathAway() {
		super();
		tileFileNamePattern = "%02X/%04X/%04X.%s";
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas) throws IOException, InterruptedException,
			AtlasTestException {
		super.startAtlasCreation(atlas);
		int mapCount = 0;
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				mapCount++;
				if (map.getZoom() > 16)
					throw new AtlasTestException("resolution too high - "
							+ "highest possible zoom level is 16");
			}
		}
	}

	@Override
	protected File getTileFile(int x, int y, int zoom) {
		return new File(mapDir, String.format(tileFileNamePattern, 16 - zoom, x, y, tileType));
	}

}
