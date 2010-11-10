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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageType;
import mobac.utilities.tar.TarIndex;



public class DownloadedTileProvider implements TileProvider {

	public static final String TILE_FILENAME_PATTERN = "l%dx%dy%d";

	protected final TarIndex tarIndex;
	protected final TileImageType mapTileType;

	public DownloadedTileProvider(TarIndex tarIndex, MapSource mapSource) {
		this.tarIndex = tarIndex;
		this.mapTileType = mapSource.getTileImageType();
	}

	public byte[] getTileData(int x, int y) throws IOException {
		return getTileData(x, y, 0);
	}

	public byte[] getTileData(int x, int y, int layer) throws IOException {
		return tarIndex.getEntryContent(String.format(TILE_FILENAME_PATTERN, layer, x, y));
	}

	public BufferedImage getTileImage(int x, int y) throws IOException {
		return getTileImage(x, y, 0);
	}

	public BufferedImage getTileImage(int x, int y, int layer) throws IOException {
		byte[] unconvertedTileData = getTileData(x, y, layer);
		if (unconvertedTileData == null)
			return null;
		return ImageIO.read(new ByteArrayInputStream(unconvertedTileData));
	}
}
