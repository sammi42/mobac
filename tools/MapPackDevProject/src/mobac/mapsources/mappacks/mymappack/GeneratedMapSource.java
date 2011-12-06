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
package mobac.mapsources.mappacks.mymappack;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.exceptions.UnrecoverableDownloadException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;

/**
 * A simple {@link MapSource} implementation serving as fall-back if no other
 * map source is available/can be loaded.
 */
public class GeneratedMapSource implements MapSource {

	private static final String name = "Generated";

	private MapSourceLoaderInfo loaderInfo = null;

	private MapSpace mapSpace = MercatorPower2MapSpace.INSTANCE_256;

	public GeneratedMapSource() {
	}

	public Color getBackgroundColor() {
		return Color.WHITE;
	}

	public MapSpace getMapSpace() {
		return mapSpace;
	}

	public int getMaxZoom() {
		return 19;
	}

	public int getMinZoom() {
		return 0;
	}

	public String getName() {
		return name;
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream(16000);
		try {
			BufferedImage image = getTileImage(zoom, x, y, loadMethod);
			ImageIO.write(image, "png", buf);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return buf.toByteArray();
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod)
			throws IOException, UnrecoverableDownloadException, InterruptedException {

		// Corners of the tile image to be painted
		// double lon_upper_left = mapSpace.cXToLon(x * 256, zoom);
		// double lat_upper_left = mapSpace.cYToLat(y * 256, zoom);
		// double lon_lower_right = mapSpace.cXToLon((x * 256) + 255, zoom);
		// double lat_lower_right = mapSpace.cYToLat((y * 256) + 255, zoom);

		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2 = image.createGraphics();
		try {
			// Now let's paint the map tile
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, 256, 256);
			g2.setColor(Color.RED);
			// Some example text to see how it works
			g2.drawString("zoom = " + zoom, 0, 15);
			g2.drawString("x = " + x, 0, 35);
			g2.drawString("y = " + y, 0, 55);
		} finally {
			g2.dispose();
		}
		return image;
	}

	public TileImageType getTileImageType() {
		return TileImageType.PNG;
	}

	public MapSourceLoaderInfo getLoaderInfo() {
		return loaderInfo;
	}

	public void setLoaderInfo(MapSourceLoaderInfo loaderInfo) {
		this.loaderInfo = loaderInfo;

	}

	@Override
	public String toString() {
		return name;
	}

}
