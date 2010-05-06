package mobac.program.model;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

/**
 * Coordinate point in Mercator projection regarding a world with height and
 * width 2<sup>30</sup> pixels (2<sup>22</sup> tiles with size 256 pixels). This
 * is the maximum size a <code>int</code> can hold.
 */
public class MercatorPixelCoordinate {

	private final MapSpace mapSpace;
	private final int x;
	private final int y;
	private final int zoom;

	public MercatorPixelCoordinate(MapSpace mapSpace, int x, int y, int zoom) {
		super();
		this.mapSpace = mapSpace;
		this.x = x;
		this.y = y;
		this.zoom = zoom;
	}

	public MercatorPixelCoordinate(MapSpace mapSpace, double lat, double lon) {
		super();
		this.mapSpace = mapSpace;
		this.x = mapSpace.cLonToX(lon, JMapViewer.MAX_ZOOM);
		this.y = mapSpace.cLatToY(lat, JMapViewer.MAX_ZOOM);
		this.zoom = JMapViewer.MAX_ZOOM;
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

	public MapSpace getMapSpace() {
		return mapSpace;
	}

	public EastNorthCoordinate getEastNorthCoordinate() {
		double lon = mapSpace.cXToLon(x, zoom);
		double lat = mapSpace.cYToLat(y, zoom);
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
		return new MercatorPixelCoordinate(mapSpace, new_x, new_y, aZoomlevel);
	}

	@Override
	public String toString() {
		return "x=" + x + " y=" + y + " zoom=" + zoom;
	}

}
