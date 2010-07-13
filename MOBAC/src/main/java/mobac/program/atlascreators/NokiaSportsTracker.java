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
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.TileImageFormat;
import mobac.program.model.TileImageParameters;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class NokiaSportsTracker extends AtlasCreator {

	protected File mapDir = null;

	protected MapTileWriter mapTileWriter = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				TileImageParameters param = map.getParameters();
				if (param != null)
					throw new AtlasTestException("Custom tile settings are not supported by this atlas format");
			}
		}
	}

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);
		mapDir = new File(atlasDir, layer.getName());
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		if (!"png".equalsIgnoreCase(mapSource.getTileType()))
			// If the tile image format is not png we have to convert it
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, TileImageFormat.PNG);
	}

	public void createMap() throws MapCreationException, InterruptedException {
		// This means there should not be any resizing of the tiles.
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
						writeTile(x, y, sourceTileData);
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), e);
				}
			}
		}
	}

	public void writeTile(int tilex, int tiley, byte[] tileData) throws IOException {
		String tileName = getTileName(zoom, tilex, tiley);
		int count = tileName.length();
		int dirCount = count / 3;
		if ((count % 3 == 0) & (dirCount > 0))
			dirCount--;
		File tileDir = mapDir;
		for (int i = 0; i < dirCount; i++) {
			int start = i * 3;
			String dirName = tileName.substring(start, start + 3);
			tileDir = new File(tileDir, dirName);
		}
		String fileName = tileName + ".jpg";
		File file = new File(tileDir, fileName);
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

	protected static final char[] NUM_CHAR = { 'q', 'r', 't', 's' };

	public static String getTileName(int zoom, int tilex, int tiley) {
		char[] tileNum = new char[zoom + 1];
		tileNum[0] = 't';
		for (int i = zoom; i > 0; i--) {
			int num = (tilex % 2) | ((tiley % 2) << 1);
			tileNum[i] = NUM_CHAR[num];
			tilex >>= 1;
			tiley >>= 1;
		}
		return new String(tileNum);
	}

}
