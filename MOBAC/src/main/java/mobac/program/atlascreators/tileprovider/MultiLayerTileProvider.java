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
package mobac.program.atlascreators.tileprovider;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MultiLayerMapSource;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageFormat;


public class MultiLayerTileProvider extends FilterTileProvider {

	private final MapSource mapSource;

	private TileImageDataWriter writer;

	private int layerCount;

	public MultiLayerTileProvider(MultiLayerMapSource mapSource, TileProvider tileProvider, int layerCount) {
		super(tileProvider);
		this.mapSource = mapSource;
		this.layerCount = layerCount;
		TileImageFormat tileImageFormat = TileImageFormat.PNG;
		writer = tileImageFormat.getDataWriter();
		writer.initialize();
		ImageIO.setUseCache(false);
	}

	@Override
	public byte[] getTileData(int x, int y, int layer) throws IOException {
		return getTileData(x, y);
	}

	@Override
	public byte[] getTileData(int x, int y) throws IOException {
		BufferedImage combinedImage = getTileImage(x, y);
		if (combinedImage == null)
			return null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(32000);
		writer.processImage(combinedImage, buffer);
		return buffer.toByteArray();
	}

	@Override
	public BufferedImage getTileImage(int x, int y, int layer) throws IOException {
		log.trace("Creting multi-layer tile x=" + x + " y=" + y + " layer=" + layer);
		int tileSize = mapSource.getMapSpace().getTileSize();
		BufferedImage combinedImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = combinedImage.getGraphics();
		g.setColor(mapSource.getBackgroundColor());
		g.fillRect(0, 0, tileSize, tileSize);
		boolean used = false;
		try {
			for (int l = 0; l < layerCount; l++) {
				BufferedImage image = tileProvider.getTileImage(x, y, l);
				if (image == null)
					continue;
				g.drawImage(image, 0, 0, null);
				used = true;
			}
		} finally {
			g.dispose();
		}
		if (used)
			return combinedImage;
		else
			return null;
	}
}
