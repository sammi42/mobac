package tac.program.model;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.OsmMercator;

/**
 * Coordinate point in Mercator projection regarding a world with height and
 * width 2<sup>30</sup> pixels (2<sup>22</sup> tiles with size 256 pixels). This
 * is the maximum size a <code>int</code> can hold.
 */
public class MercatorPixelCoordinate {

	private int x;
	private int y;
	private int zoom;

	public MercatorPixelCoordinate(int x, int y, int zoom) {
		super();
		this.x = x;
		this.y = y;
		this.zoom = zoom;
	}

	public MercatorPixelCoordinate(double lat, double lon) {
		super();
		this.x = OsmMercator.LonToX(lon, JMapViewer.MAX_ZOOM);
		this.y = OsmMercator.LatToY(lat, JMapViewer.MAX_ZOOM);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZoom() {
		return zoom;
	}

	public EastNorthCoordinate getEastNorthCoordinate() {
		double lon = OsmMercator.XToLon(x, zoom);
		double lat = OsmMercator.YToLat(y, zoom);
		return new EastNorthCoordinate(lat, lon);
	}

	public MercatorPixelCoordinate adaptToZoomlevel(int aZoomlevel) {
		int zoomDiff = this.zoom - aZoomlevel;
		int new_x = x;
		int new_y = y;
		if (zoomDiff < 0) {
			zoomDiff = -zoomDiff;
			new_x <<= zoomDiff;
			new_y <<= zoomDiff;
		} else {
			new_x >>= zoomDiff;
			new_y >>= zoomDiff;
		}
		return new MercatorPixelCoordinate(new_x, new_y, aZoomlevel);
	}

	@Override
	public String toString() {
		return "x=" + x + " y=" + y + " zoom=" + zoom;
	}

}
