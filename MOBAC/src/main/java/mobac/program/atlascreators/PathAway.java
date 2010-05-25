package mobac.program.atlascreators;

import java.io.File;
import java.io.IOException;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.impl.OsmMapSources;
import mobac.mapsources.impl.Google.GoogleEarth;
import mobac.mapsources.impl.Google.GoogleMaps;
import mobac.mapsources.impl.Google.GoogleTerrain;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

/**
 * Creates a tile cache structure as used by <a href="http://www.pathaway.com/">PathAway</a> (for WindowsMobile,
 * Symbian, Palm)
 */
public class PathAway extends OSMTracker {

	public PathAway() {
		super();
		tileFileNamePattern = "%02X/%04X/%04X.%s";
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);

		MapSource mapSource = map.getMapSource();
		String shortMapDir = null;
		if (mapSource.getClass().equals(GoogleMaps.class))
			shortMapDir = "G1";
		else if (mapSource.getClass().equals(GoogleEarth.class))
			shortMapDir = "G2";
		else if (mapSource.getClass().equals(GoogleTerrain.class))
			shortMapDir = "G3";
		else if (mapSource.getClass().equals(OsmMapSources.Mapnik.class))
			shortMapDir = "OSM1";
		else if (mapSource.getClass().equals(OsmMapSources.CycleMap.class))
			shortMapDir = "OCM1";
		if (shortMapDir != null)
			mapDir = new File(atlasDir, shortMapDir);
	}

	public void createMap() throws MapCreationException, InterruptedException {
		// This means there should not be any resizing of the tiles.
		if (mapTileWriter == null)
			mapTileWriter = new PathAwayTileWriter();
		createTiles();
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				if (map.getZoom() > 17)
					throw new AtlasTestException("resolution too high - " + "highest possible zoom level is 17");
			}
		}
	}

	protected class PathAwayTileWriter extends OSMTileWriter {

		@Override
		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {
			File file = new File(mapDir, String.format(tileFileNamePattern, 17 - zoom, tilex, tiley, tileType));
			writeTile(file, tileData);
		}

	}
}
