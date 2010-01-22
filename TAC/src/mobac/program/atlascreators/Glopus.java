package mobac.program.atlascreators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import mobac.exceptions.MapCreationException;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


public class Glopus extends Ozi {

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		String layerName = map.getLayer().getName().replaceAll(" ", "_");
		mapName = map.getName().replaceAll(" ", "_");
		mapDir = new File(atlasDir, layerName);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(mapDir);
		} catch (IOException e1) {
			throw new MapCreationException(e1);
		}
		createTiles();
		writeKalFile();
	}

	private void writeKalFile() {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(mapDir, mapName + ".kal"));
			OutputStreamWriter mapWriter = new OutputStreamWriter(fout, TEXT_FILE_CHARSET);

			MapSpace mapSpace = mapSource.getMapSpace();

			String longitudeMin = Double.toString(mapSpace.cXToLon(xMin * tileSize, zoom));
			String longitudeMax = Double.toString(mapSpace.cXToLon((xMax + 1) * tileSize, zoom));
			String latitudeMin = Double.toString(mapSpace.cYToLat((yMax + 1) * tileSize, zoom));
			String latitudeMax = Double.toString(mapSpace.cYToLat(yMin * tileSize, zoom));

			int width = (xMax - xMin + 1) * tileSize;
			int height = (yMax - yMin + 1) * tileSize;

			mapWriter.write("[Calibration Point 1]\n");
			mapWriter.write(String.format("Longitude = %s\n", longitudeMin));
			mapWriter.write(String.format("Latitude =  %s\n", latitudeMax));
			mapWriter.write("Pixel = POINT(0,0)\n");

			mapWriter.write("[Calibration Point 2]\n");
			mapWriter.write(String.format("Longitude = %s\n", longitudeMax));
			mapWriter.write(String.format("Latitude =  %s\n", latitudeMin));
			mapWriter.write(String.format("Pixel = POINT(%d,%d)\n", width, height));

			mapWriter.write("[Calibration Point 3]\n");
			mapWriter.write(String.format("Longitude = %s\n", longitudeMax));
			mapWriter.write(String.format("Latitude =  %s\n", latitudeMax));
			mapWriter.write(String.format("Pixel = POINT(%d,%d)\n", width, 0));

			mapWriter.write("[Calibration Point 4]\n");
			mapWriter.write(String.format("Longitude = %s\n", longitudeMin));
			mapWriter.write(String.format("Latitude =  %s\n", latitudeMin));
			mapWriter.write(String.format("Pixel = POINT(%d,%d)\n", 0, height));

			mapWriter.write("[Map]\n");
			mapWriter.write(String.format("Bitmap = %s.png\n", mapName));
			mapWriter.write(String.format("Size = SIZE(%d,%d)\n", width, height));

			mapWriter.flush();
			mapWriter.close();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(fout);
		}
	}
}
