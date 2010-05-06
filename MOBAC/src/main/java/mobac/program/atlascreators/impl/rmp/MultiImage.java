package mobac.program.atlascreators.impl.rmp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import mobac.exceptions.MapCreationException;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.collections.SoftHashMap;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


/**
 * CalibratedImage that gets its data from a set of other CalibratedImage2
 * 
 */
public class MultiImage {

	private static final Logger log = Logger.getLogger(MultiImage.class);

	private final MapSource mapSource;
	private final int zoom;
	private final TileProvider tileProvider;
	private SoftHashMap<TileKey, MobacTile> cache;

	public MultiImage(MapSource mapSource, TileProvider tileProvider, MapInterface map) {
		this.mapSource = mapSource;
		this.tileProvider = tileProvider;
		this.zoom = map.getZoom();
		cache = new SoftHashMap<TileKey, MobacTile>(400);
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

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D graph = result.createGraphics();
		try {
			graph.setColor(Color.WHITE);
			graph.fillRect(0, 0, width, height);

			for (int x = xMin; x <= xMax; x++) {
				for (int y = yMin; y <= yMax; y++) {
					TileKey key = new TileKey(x, y);
					MobacTile image = cache.get(key);
					if (image == null) {
						image = new MobacTile(tileProvider, mapSpace, x, y, zoom);
						cache.put(key, image);
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