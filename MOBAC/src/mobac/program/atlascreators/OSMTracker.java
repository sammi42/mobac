package mobac.program.atlascreators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.TileImageParameters;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

/**
 * Creates maps identical to the atlas format used by OSMTracker.
 * 
 * Please note that this atlas format ignores the defined atlas structure. It
 * uses a separate directory for each used map source and inside one directory
 * for each zoom level.
 */
public class OSMTracker extends AtlasCreator {

	protected String tileFileNamePattern = "%d/%d/%d.%s";

	protected File mapDir = null;

	protected String tileType = null;

	protected MapTileWriter mapTileWriter = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas) throws AtlasTestException, IOException,
			InterruptedException {
		super.startAtlasCreation(atlas);
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				TileImageParameters param = map.getParameters();
				if (param == null)
					continue;
				if (param.getHeight() != 256 || param.getWidth() != 256)
					throw new AtlasTestException("Custom tile size is not supported by this atlas");
			}
		}
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		mapDir = new File(atlasDir, map.getMapSource().getName());
		tileType = mapSource.getTileType();
		if (parameters != null) {
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, parameters
					.getFormat());
			tileType = parameters.getFormat().getDataWriter().getFileExt();
		}
	}

	public void createMap() throws MapCreationException, InterruptedException {
		// This means there should not be any resizing of the tiles.
		if (mapTileWriter == null)
			mapTileWriter = new OSMTileWriter();
		createTiles();
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null)
						mapTileWriter.writeTile(x, y, tileType, sourceTileData);
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), e);
				}
			}
		}
	}

	protected class OSMTileWriter implements MapTileWriter {

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData)
				throws IOException {
			File file = new File(mapDir, String.format(tileFileNamePattern, zoom, tilex, tiley,
					tileType));
			writeTile(file, tileData);
		}

		protected void writeTile(File file, byte[] tileData) throws IOException {
			Utilities.mkDirs(file.getParentFile());
			FileOutputStream out = new FileOutputStream(file);
			try {
				out.write(tileData);
			} finally {
				Utilities.closeStream(out);
			}
		}

		public void finalizeMap() throws IOException {
			// Nothing to do
		}

	}
}
