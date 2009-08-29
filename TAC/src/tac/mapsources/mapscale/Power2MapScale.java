package tac.mapsources.mapscale;

import org.openstreetmap.gui.jmapviewer.interfaces.MapScale;

/**
 * This is the only implementation that is currently supported by TrekBuddy
 * Atlas Creator.
 * <p>
 * DO NOT TRY TO IMPLEMENT YOUR OWN. IT WILL NOT WORK!
 * </p>
 * 
 * @see MapScale
 */
public class Power2MapScale implements MapScale {

	public static final MapScale INSTANCE = new Power2MapScale();

	public int cLatToY(double lat, int zoom) {
		return OsmMercator.LatToY(lat, zoom);
	}

	public int cLonToX(double lon, int zoom) {
		return OsmMercator.LonToX(lon, zoom);
	}

	public double cXToLon(int x, int zoom) {
		return OsmMercator.XToLon(x, zoom);
	}

	public double cYToLat(int y, int zoom) {
		return OsmMercator.YToLat(y, zoom);
	}

	public int getTileSize() {
		return OsmMercator.TILE_SIZE;
	}

}
