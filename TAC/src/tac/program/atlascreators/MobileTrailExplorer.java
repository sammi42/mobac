package tac.program.atlascreators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.MapCreationException;
import tac.mapsources.mapspace.MercatorPower2MapSpace;
import tac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import tac.program.interfaces.MapInterface;
import tac.program.model.TileImageFormat;
import tac.tar.TarIndex;
import tac.utilities.Utilities;

/**
 * Creates maps using the Mobile Trail Explorer (MTE) / JTileDownloader atlas
 * format (converted to PNG, size 256x256 pixel).
 * 
 * Please note that this atlas format ignores the defined atlas structure. It
 * uses a separate directory for each used map source and inside one directory
 * for each zoom level.
 */
public class MobileTrailExplorer extends AtlasCreator {

	private File mapDir = null;
	private File mapZoomDir = null;

	protected String appendFileExt = "";

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		String mapName = map.getMapSource().getName().replaceAll(" ", "_");
		mapDir = new File(atlasDir, mapName);
		mapZoomDir = new File(mapDir, Integer.toString(map.getZoom()));
	}

	public void createMap() throws MapCreationException {
		try {
			Utilities.mkDir(mapDir);
			Utilities.mkDir(mapZoomDir);
		} catch (IOException e1) {
			throw new MapCreationException(e1);
		}
		try {
			if (!"png".equalsIgnoreCase(mapSource.getTileType()))
				// If the tile image format is not png we have to convert it
				mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider,
						TileImageFormat.PNG);
			createTiles();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		}
	}

	@Override
	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		for (int x = xMin; x <= xMax; x++) {
			File xDir = new File(mapZoomDir, Integer.toString(x));
			try {
				Utilities.mkDir(xDir);
			} catch (IOException e1) {
				throw new MapCreationException(e1);
			}
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					String tileFileName = x + "/" + y + ".png";
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null) {
						File f = new File(mapZoomDir, tileFileName);
						FileOutputStream out = new FileOutputStream(f);
						try {
							out.write(sourceTileData);
						} finally {
							Utilities.closeStream(out);
						}
					}
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
	}

}