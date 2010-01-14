package tac.program.atlascreators;

import java.awt.Point;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.exceptions.MapCreationException;
import tac.mapsources.mapspace.MercatorPower2MapSpace;
import tac.program.atlascreators.impl.rmp.BoundingRect;
import tac.program.atlascreators.impl.rmp.MultiImage;
import tac.program.atlascreators.impl.rmp.RmpLayer;
import tac.program.atlascreators.impl.rmp.RmpTools;
import tac.program.atlascreators.impl.rmp.RmpWriter;
import tac.program.atlascreators.impl.rmp.TacTile;
import tac.program.atlascreators.impl.rmp.Tiledata;
import tac.program.atlascreators.impl.rmp.RmpLayer.TLMEntry;
import tac.program.atlascreators.impl.rmp.interfaces.CalibratedImage;
import tac.program.atlascreators.impl.rmp.rmpfile.Bmp2bit;
import tac.program.atlascreators.impl.rmp.rmpfile.Bmp4bit;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.utilities.tar.TarIndex;

public class MagellanRmp extends AtlasCreator {

	RmpWriter rmpWriter = null;
	String imageName = null;
	int layerNum = 0;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas) throws IOException, InterruptedException {
		super.startAtlasCreation(atlas);
		int mapCount = 0;
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				mapCount++;
				// if (map.getZoom() > 15)
				// throw new IOException("resolution too high - "
				// + "highest possible zoom level is 15");
				Point max = map.getMaxTileCoordinate();
				Point min = map.getMinTileCoordinate();
				if (max.x - min.x > 18000 || max.y - min.y > 18000)
					throw new IOException("Map too large. Max size 18000x18000");
			}
		}
		// if (mapCount > 5)
		// throw new IOException("Too many maps in atlas. Max map count = 5");
		imageName = RmpTools.buildImageName(atlas.getName());
		rmpWriter = new RmpWriter(imageName, mapCount, new File(atlasDir, imageName + ".rmp"));
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		createTiles();
	}

	@Override
	protected void createTiles() throws InterruptedException, MapCreationException {
		int count = (xMax - xMin + 1) * (yMax - yMin + 1);
		atlasProgress.initMapCreation(1000);
		TacTile[] images = new TacTile[count];
		ImageIO.setUseCache(false);

		int i = 0;
		MapSpace mapSpace = map.getMapSource().getMapSpace();
		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				images[i++] = new TacTile(mapDlTileProvider, mapSpace, x, y, zoom);
			}
		}
		// Note: MultiImage relies on the fact, that the image array is sorted
		// on the left border (west coordinate / x coordinate)
		MultiImage layerImage = new MultiImage(images, map);
		try {
			RmpLayer layer = createLayer(layerImage, layerNum);
			String layerName = RmpTools.buildTileName(imageName, layerNum);
			TLMEntry tlmEntry = layer.getTLMFile(layerName);
			rmpWriter.prepareFileEntry(tlmEntry);
			rmpWriter.writeFileEntry(layer.getA00File(layerName));
			tlmEntry.updateContent();
			rmpWriter.writePreparedFileEntry(tlmEntry);
			atlasProgress.setMapCreationProgress(1000);
		} catch (IOException e) {
			throw new MapCreationException(e);
		}
		layerNum++;
	}

	@Override
	public void finishAtlasCreation() throws IOException, InterruptedException {
		if (rmpWriter == null)
			return; // Creation already aborted
		try {
			rmpWriter.writeFileEntry(new Bmp2bit());
			rmpWriter.writeFileEntry(new Bmp4bit());
			rmpWriter.writeDirectory();
		} finally {
			rmpWriter.close();
			rmpWriter = null;
		}
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		super.abortAtlasCreation();
		rmpWriter.delete();
		rmpWriter = null;
	}

	/**
	 * Create a new instance of a TLM file and fill it with the data of a
	 * calibrated image
	 * 
	 * @param si
	 *            image to get data from
	 * @param layer
	 *            Layer number - for status output only
	 * @return TLM instance
	 * @throws InterruptedException
	 * @throws MapCreationException
	 * @throws IOException
	 */
	public RmpLayer createLayer(CalibratedImage si, int layer) throws InterruptedException,
			MapCreationException, IOException {

		int count = 0;

		/* --- Create instance --- */
		RmpLayer rmpLayer = new RmpLayer(this);

		/* --- Get the coordinate space of the image --- */
		BoundingRect rect = si.getBoundingRect();

		/* --- Calculate tile dimensions --- */
		double tile_width = (rect.getEast() - rect.getWest()) * 256 / si.getImageWidth();
		double tile_height = (rect.getSouth() - rect.getNorth()) * 256 / si.getImageHeight();

		/*
		 * --- Check the theoretical maximum of horizontal and vertical position
		 * ---
		 */
		// double pos_hor = 360 / tile_width;
		// double pos_ver = 180 / tile_height;
		// if (pos_hor > 0xFFFF || pos_ver >= 0xFFFF)
		// throw new MapCreationException(
		// "Map resolution too high - please select a lower zoom level");

		/* --- Calculate the positions of the upper left tile --- */
		int x_start = (int) Math.floor((rect.getWest() + 180) / tile_width);
		int y_start = (int) Math.floor((rect.getNorth() + 90) / tile_height);

		double x_end = (rect.getEast() + 180.0) / tile_width;
		double y_end = (rect.getSouth() + 90.0) / tile_height;

		/*
		 * Create the tiles - process works column wise, starting on the top
		 * left corner of the destination area.
		 */
		for (int x = x_start; x < x_end; x++) {
			for (int y = y_start; y < y_end; y++) {
				count++;
				if (log.isTraceEnabled())
					log.trace(String.format("Create tile %d layer=%d", count, layer));

				/* --- Create tile --- */
				BoundingRect subrect = new BoundingRect(y * tile_height - 90, (y + 1) * tile_height
						- 90, x * tile_width - 180, (x + 1) * tile_width - 180);
				Tiledata td = new Tiledata();
				td.posx = x;
				td.posy = y;
				td.rect = subrect;
				td.si = si;
				rmpLayer.addPreparedImage(td);
			}
		}

		/* --- Build the TLM file --- */
		rmpLayer.buildTLMFile(tile_width, tile_height, rect.getWest(), rect.getEast(), rect
				.getNorth(), rect.getSouth());

		return rmpLayer;
	}

}
