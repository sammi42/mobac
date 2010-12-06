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
package mobac.mapsources;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;

import mobac.exceptions.TileException;
import mobac.gui.mapview.PreviewMap;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;

import org.apache.log4j.Logger;

public abstract class AbstractMultiLayerMapSource implements MapSource, Iterable<MapSource> {

	protected Logger log;

	private final String name;
	private int maxZoom;
	private int minZoom;
	protected MapSource[] mapSources;
	private MapSpace mapSpace;
	private TileImageType tileImageType;

	public AbstractMultiLayerMapSource(String name, TileImageType tileImageType) {
		log = Logger.getLogger(this.getClass());
		this.name = name;
		this.tileImageType = tileImageType;
	}

	protected void initializeValues() {
		MapSource refMapSource = mapSources[0];
		mapSpace = refMapSource.getMapSpace();
		maxZoom = PreviewMap.MAX_ZOOM;
		minZoom = 0;
		for (MapSource ms : mapSources) {
			maxZoom = Math.min(maxZoom, ms.getMaxZoom());
			minZoom = Math.max(minZoom, ms.getMinZoom());
			if (ms.getMapSpace() != mapSpace)
				throw new RuntimeException("Different map spaces used");
		}
	}

	public MapSource[] getLayerMapSources() {
		return mapSources;
	}

	public Color getBackgroundColor() {
		return Color.BLACK;
	}

	public MapSpace getMapSpace() {
		return mapSpace;
	}

	public int getMaxZoom() {
		return maxZoom;
	}

	public int getMinZoom() {
		return minZoom;
	}

	public String getName() {
		return name;
	}

	public String getStoreName() {
		return null;
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, InterruptedException,
			TileException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream(16000);
		BufferedImage image = getTileImage(zoom, x, y, loadMethod);
		if (image == null)
			return null;
		ImageIO.write(image, tileImageType.getFileExt(), buf);
		return buf.toByteArray();
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			InterruptedException, TileException {
		int tileSize = mapSpace.getTileSize();
		BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2 = image.createGraphics();
		try {
			g2.setColor(getBackgroundColor());
			g2.fillRect(0, 0, tileSize, tileSize);
			boolean used = false;
			for (MapSource layerMapSource : mapSources) {
				BufferedImage layerImage = layerMapSource.getTileImage(zoom, x, y, loadMethod);
				if (layerImage != null) {
					// log.trace("Multi layer loading: " + layerMapSource + " " + x + " " + y + " " + zoom);
					g2.drawImage(layerImage, 0, 0, null);
					used = true;
				}
			}
			if (used)
				return image;
			else
				return null;
		} finally {
			g2.dispose();
		}
	}

	public TileImageType getTileImageType() {
		return tileImageType;
	}

	@Override
	public String toString() {
		return getName();
	}

	public Iterator<MapSource> iterator() {
		return Arrays.asList(mapSources).iterator();
	}

}
