/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.program.atlascreators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;

import mobac.exceptions.MapCreationException;
import mobac.program.interfaces.MapSpace;
import mobac.utilities.Utilities;

/**
 * http://sourceforge.net/tracker/?func=detail&atid=1105497&aid=3147526&group_id=238075
 */
public class PNGWorldfile extends Glopus {

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(mapDir);
		} catch (IOException e) {
			throw new MapCreationException(e);
		}
		createTiles();
		writeWorldFile();
	}

	/**
	 * http://en.wikipedia.org/wiki/World_file
	 * 
	 * <pre>
	 * Format of Worldfile: 
	 * 			   0.000085830078125  (size of pixel in x direction)                              =(east-west)/image width
	 * 			   0.000000000000     (rotation term for row)
	 * 			   0.000000000000     (rotation term for column)
	 * 			   -0.00006612890625  (size of pixel in y direction)                              =-(north-south)/image height
	 * 			   -106.54541         (x coordinate of centre of upper left pixel in map units)   =west
	 * 			   39.622615          (y coordinate of centre of upper left pixel in map units)   =north
	 * </pre>
	 */
	private void writeWorldFile() throws MapCreationException {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(mapDir, mapName + ".pgw"));
			OutputStreamWriter mapWriter = new OutputStreamWriter(fout, TEXT_FILE_CHARSET);

			MapSpace mapSpace = mapSource.getMapSpace();

			int width = (xMax - xMin + 1) * tileSize;
			int height = (yMax - yMin + 1) * tileSize;

			double lonMin = mapSpace.cXToLon(xMin * tileSize, zoom);
			double lonMax = mapSpace.cXToLon((xMax + 1) * tileSize, zoom);
			double latMin = mapSpace.cYToLat((yMax + 1) * tileSize, zoom);
			double latMax = mapSpace.cYToLat(yMin * tileSize, zoom);

			mapWriter.write(String.format(Locale.ENGLISH, "%.15f\n", (lonMax - lonMin) / width));
			mapWriter.write("0.0\n");
			mapWriter.write("0.0\n");
			mapWriter.write(String.format(Locale.ENGLISH, "%.15f\n", -(latMax - latMin) / height));
			mapWriter.write(String.format(Locale.ENGLISH, "%.7f\n", lonMin));
			mapWriter.write(String.format(Locale.ENGLISH, "%.7f\n", latMax));

			mapWriter.flush();
			mapWriter.close();
		} catch (IOException e) {
			throw new MapCreationException(e);
		} finally {
			Utilities.closeStream(fout);
		}
	}
}
