package tac.program.atlascreators.impl.rmp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.exceptions.MapCreationException;
import tac.program.atlascreators.tileprovider.TileProvider;
import tac.program.interfaces.MapInterface;

/**
 * CalibratedImage that gets its data from a set of other CalibratedImage2
 * 
 */
public class MultiImage {

	private static final Logger log = Logger.getLogger(MultiImage.class);

	private final MapSource mapSource;
	private final int zoom;
	private final TileProvider tileProvider;
	private HashMap<TileKey, SoftReference<TacTile>> cache;

	public MultiImage(MapSource mapSource, TileProvider tileProvider, MapInterface map) {
		this.mapSource = mapSource;
		this.tileProvider = tileProvider;
		this.zoom = map.getZoom();
		cache = new HashMap<TileKey, SoftReference<TacTile>>(400);
	}

	public BufferedImage getSubImage(BoundingRect area, int width, int height)
			throws MapCreationException {
		if (log.isTraceEnabled())
			log.trace(String.format("getSubImage %d %d %s", width, height, area));

		MapSpace mapSpace = mapSource.getMapSpace();
		int tilesize = mapSpace.getTileSize();

		int xMax = mapSource.getMapSpace().cLonToX(area.getEast(), zoom) / tilesize;
		int xMin = mapSource.getMapSpace().cLonToX(area.getWest(), zoom) / tilesize;
		int yMax = mapSource.getMapSpace().cLatToY(-area.getSouth(), zoom) / tilesize;
		int yMin = mapSource.getMapSpace().cLatToY(-area.getNorth(), zoom) / tilesize;

		log.trace(String.format("min/max x: %d/%d  min/max y: %d/%d zoom: %d", xMin, xMax, yMin,
				yMax, zoom));

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D graph = result.createGraphics();
		try {
			graph.setColor(new Color(255, 255, 255));
			graph.fillRect(0, 0, width, height);

			for (int x = xMin; x <= xMax; x++) {
				for (int y = yMin; y <= yMax; y++) {
					TileKey key = new TileKey(x, y);
					SoftReference<TacTile> ref = cache.get(key);
					TacTile image = null;
					if (ref != null) {
						image = ref.get();
						if (image != null)
							log.trace("Cache hit: " + x + " " + y);
						else
							log.trace("Cache soft miss: " + x + " " + y);
					}
					if (image == null) {
						log.trace("Cache miss: " + x + " " + y);
						image = new TacTile(tileProvider, mapSpace, x, y, zoom);
						ref = new SoftReference<TacTile>(image);
						cache.put(key, ref);
						log.trace("Added to cache: " + x + " " + y + " elements: " + cache.size());
					}
					image.drawSubImage(area, result);
				}
			}
		} catch (Throwable t) {
			throw new MapCreationException(t);
		} finally {
			graph.dispose();
		}
		return result;
	}

	protected static class TileKey {
		int x;
		int y;

		public TileKey(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TileKey other = (TileKey) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

	}
}