package rmp;

import java.awt.Point;
import java.io.File;
import java.io.IOException;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import rmp.rmpfile.MultiImage;
import rmp.rmpfile.RmpLayer;
import rmp.rmpfile.RmpTools;
import rmp.rmpfile.RmpWriter;
import rmp.rmpfile.entries.Bmp2bit;
import rmp.rmpfile.entries.Bmp4bit;
import rmp.rmpmaker.TacTile;
import tac.exceptions.MapCreationException;
import tac.mapsources.mapspace.MercatorPower2MapSpace;
import tac.program.atlascreators.AtlasCreator;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;

public class MagellanRmp extends AtlasCreator {

	RmpWriter rmpWriter = null;
	String imageName = null;
	int layerNum = 0;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas) throws IOException {
		super.startAtlasCreation(atlas);
		int mapCount = 0;
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				mapCount++;
				if (map.getZoom() > 15)
					throw new IOException("resolution too high - "
							+ "highest possible zoom level is 15");
				Point max = map.getMaxTileCoordinate();
				Point min = map.getMinTileCoordinate();
				if (max.x - min.x > 18000 || max.y - min.y > 18000)
					throw new IOException("Map too large. Max size 18000x18000");
			}
		}
		if (mapCount > 5)
			throw new IOException("Too many maps in atlas. Max map count = 5");
		imageName = RmpTools.buildImageName(atlas.getName());
		rmpWriter = new RmpWriter(imageName, mapCount, new File(atlasDir, imageName + ".rmp"));
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
	}

	@Override
	public void createMap() throws MapCreationException {
		try {
			createTiles();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		}
	}

	@Override
	protected void createTiles() throws InterruptedException, MapCreationException {
		int count = (xMax - xMin + 1) * (yMax - yMin + 1);
		int progressStep = count;
		atlasProgress.initMapCreation(count + progressStep + 10);
		TacTile[] images = new TacTile[count];

		int lineX = 0;
		int i = 0;
		MapSpace mapSpace = map.getMapSource().getMapSpace();
		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				images[i++] = new TacTile(mapDlTileProvider, mapSpace, x, y, zoom);
				lineX += tileSize;
			}
		}
		MultiImage layerImage = new MultiImage(images, map);
		RmpLayer layer = RmpLayer.createFromImage(layerImage, layerNum);
		atlasProgress.incMapCreationProgress(progressStep);
		checkUserAbort();
		String layerName = RmpTools.buildTileName(imageName, layerNum);
		try {
			rmpWriter.writeFileEntry(layer.getTLMFile(layerName));
			rmpWriter.writeFileEntry(layer.getA00File(layerName));
			atlasProgress.incMapCreationProgress(10);
		} catch (IOException e) {
			throw new MapCreationException(e);
		}
		layerNum++;
	}

	@Override
	public void finishAtlasCreation() throws IOException {
		try {
			rmpWriter.writeFileEntry(new Bmp2bit());
			rmpWriter.writeFileEntry(new Bmp4bit());
			rmpWriter.writeDirectory();
		} finally {
			rmpWriter.close();
			rmpWriter = null;
		}
	}

}
