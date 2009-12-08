package rmp;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import rmp.rmpfile.MultiImage;
import rmp.rmpfile.RmpLayer;
import rmp.rmpfile.RmpTools;
import rmp.rmpfile.RmpWriter;
import rmp.rmpfile.entries.Bmp2bit;
import rmp.rmpfile.entries.Bmp4bit;
import rmp.rmpfile.entries.RmpIni;
import rmp.rmpmaker.TacTile;
import tac.exceptions.MapCreationException;
import tac.program.atlascreators.AtlasCreator;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;

public class MagellanRmp extends AtlasCreator {

	List<RmpLayer> layers = new LinkedList<RmpLayer>();

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return true;
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas) throws IOException {
		super.startAtlasCreation(atlas);
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
		atlasProgress.initMapCreation(count);
		TacTile[] images = new TacTile[count];

		int lineX = 0;
		int i = 0;
		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				images[i++] = new TacTile(mapDlTileProvider, x, y, zoom);
				lineX += tileSize;
			}
		}
		MultiImage layerImage = new MultiImage(images);
		layerImage.setActiveImageMax(64);
		layers.add(RmpLayer.createFromImage(layerImage, layers.size()));
	}

	@Override
	public void finishAtlasCreation() throws IOException {
		String image_name = RmpTools.buildImageName(new File(atlas.getName()));

		/* --- Create RMP.ini --- */
		RmpIni rmp_ini = new RmpIni(image_name, layers.size());

		/* --- Create packer and fill it with content --- */
		RmpWriter packer = new RmpWriter();
		packer.addFile(rmp_ini);

		int layerNum = 0;
		while (layers.size() > 0) {
			String layerName = RmpTools.buildTileName(image_name, layerNum++);
			RmpLayer layer = layers.get(0);
			log.trace("Adding layer: " + layerName);
			packer.addFile(layer.getTLMFile(layerName));
			packer.addFile(layer.getA00File(layerName));

			/* --- Free resources --- */
			layers.remove(0);
		}

		packer.addFile(new Bmp2bit());
		packer.addFile(new Bmp4bit());

		/* --- Write to disk --- */
		packer.writeToDisk(new File(atlasDir, image_name + ".rmp"));
	}

}
