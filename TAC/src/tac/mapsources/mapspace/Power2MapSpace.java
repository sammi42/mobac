package tac.mapsources.mapspace;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

/**
 * This is the only implementation that is currently supported by TrekBuddy
 * Atlas Creator.
 * <p>
 * DO NOT TRY TO IMPLEMENT YOUR OWN. IT WILL NOT WORK!
 * </p>
 * 
 * @see MapSpace
 */
public class Power2MapSpace implements MapSpace {

	public static final MapSpace INSTANCE = new Power2MapSpace();

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

	public int moveOnLatitude(int startX, int y, int zoom, double angularDist) {

		y += OsmMercator.falseNorthing(zoom);
		double lat = -1
				* ((Math.PI / 2) - (2 * Math.atan(Math.exp(-1.0 * y / OsmMercator.radius(zoom)))));

		double lon = cXToLon(startX, zoom);
		double sinLat = Math.sin(lat);

		lon += Math.toDegrees(Math.atan2(Math.sin(angularDist) * Math.cos(lat), Math
				.cos(angularDist)
				- sinLat * sinLat));
		int newX = cLonToX(lon, zoom);
		int w = newX - startX;
		return w;
	}

	public double horizontalDistance(int zoom, int y, int xDist) {
		y = Math.max(y, 0);
		y = Math.min(y, OsmMercator.getMaxPixels(zoom));
		double lat = OsmMercator.YToLat(y, zoom);
		double lon1 = -180.0;
		double lon2 = OsmMercator.XToLon(xDist, zoom);

		double dLon = Math.toRadians(lon2 - lon1);

		double cos_lat = Math.cos(Math.toRadians(lat));
		double sin_dLon_2 = Math.sin(dLon) / 2;

		double a = cos_lat * cos_lat * sin_dLon_2 * sin_dLon_2;
		return 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}

}
