package tac.program.atlascreators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.exceptions.MapCreationException;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;
import tac.utilities.Utilities;

public class CacheWolf extends Ozi {

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
	}

	@Override
	public void createMap() throws MapCreationException {
		try {
			Utilities.mkDir(mapDir);
		} catch (IOException e1) {
			throw new MapCreationException(e1);
		}
		try {
			createTiles();
			writeWflFile();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		}
	}

	private void writeWflFile() {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(mapDir, mapName + ".wfl"));
			OutputStreamWriter mapWriter = new OutputStreamWriter(fout, TEXT_FILE_CHARSET);

			MapSpace mapSpace = mapSource.getMapSpace();

			double topLeftLon = mapSpace.cXToLon(xMin * tileSize, zoom);
			double topLeftLat = mapSpace.cYToLat(yMin * tileSize, zoom);

			double bottomRightLon = mapSpace.cXToLon((xMax + 1) * tileSize, zoom);
			double bottomRightLat = mapSpace.cYToLat((yMax + 1) * tileSize, zoom);

			int width = (xMax - xMin + 1) * tileSize;
			int height = (yMax - yMin + 1) * tileSize;

			double[] affine = { 0, 0, 0, 0 };

			// TrekBuddy Atlas Creator does only output maps with north at top
			// (no rotation). Therefore we should be able to simplify the affine
			// calculation process:
			affine[1] = (bottomRightLon - topLeftLon) / width;
			affine[2] = (bottomRightLat - topLeftLat) / height;

			for (double d : affine)
				mapWriter.write(Double.toString(d) + "\n");

			mapWriter.write(Double.toString(topLeftLat) + "\n");
			mapWriter.write(Double.toString(topLeftLon) + "\n");
			mapWriter.write(Double.toString(bottomRightLat) + "\n");
			mapWriter.write(Double.toString(bottomRightLon) + "\n");

			mapWriter.flush();
			mapWriter.close();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(fout);
		}
	}
}
