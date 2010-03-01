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

/**
 * Correspondent feature tracker entry: <a href="https://sourceforge.net/tracker/?func=detail&aid=2931899&group_id=238075&atid=1105497"
 * >IMP calibration files</a>
 * 
 * State: Waiting for further information.
 */
public class CompeGPSImp extends Ozi {

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(mapDir);
		} catch (IOException e1) {
			throw new MapCreationException(e1);
		}
		createTiles();
		writeImpFile();
	}

	private void writeImpFile() {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(mapDir, mapName + ".imp"));
			OutputStreamWriter mapWriter = new OutputStreamWriter(fout, TEXT_FILE_CHARSET);

			MapSpace mapSpace = mapSource.getMapSpace();

			String longitudeMin = Double.toString(mapSpace.cXToLon(xMin * tileSize, zoom));
			String longitudeMax = Double.toString(mapSpace.cXToLon((xMax + 1) * tileSize, zoom));
			String latitudeMin = Double.toString(mapSpace.cYToLat((yMax + 1) * tileSize, zoom));
			String latitudeMax = Double.toString(mapSpace.cYToLat(yMin * tileSize, zoom));

			int width = (xMax - xMin + 1) * tileSize;
			int height = (yMax - yMin + 1) * tileSize;

			mapWriter.write("CompeGPS MAP File");
			mapWriter.write("<Header>");
			mapWriter.write("Version=2");
			mapWriter.write("VerCompeGPS=6.8.5");
			mapWriter.write("Projection=0,Mercator,");
			mapWriter.write("Coordinates=1");
			mapWriter.write("Datum=WGS 84");
			mapWriter.write("</Header>");

			mapWriter.write("<Map>");
			mapWriter.write("Bitmap=nothing here");
			mapWriter.write("BitsPerPixel=0");
			mapWriter.write("BitmapWidth=20480");
			mapWriter.write("BitmapHeight=24576");
			mapWriter.write("Type=10");
			mapWriter.write("<BitmapData>");

			mapWriter.flush();
			mapWriter.close();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(fout);
		}
	}
}
