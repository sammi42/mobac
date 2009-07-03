package tac.program.model;

import org.openstreetmap.gui.jmapviewer.OsmMercator;

/**
 * Coordinate point in Mercator projection regarding a world with height and
 * width 2<sup>30</sup> pixels (2<sup>22</sup> tiles with size 256 pixels). This
 * is the maximum size a <code>int</code> can hold.
 */
public class Mercator22Coordinate {

	private int x;
	private int y;

	public Mercator22Coordinate(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public Mercator22Coordinate(double lat, double lon) {
		super();
		this.x = OsmMercator.LonToX(lon, 22);
		this.y = OsmMercator.LatToY(lat, 22);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public EastNorthCoordinate getEastNorthCoordinate() {
		double lat = OsmMercator.XToLon(x, 22);
		double lon = OsmMercator.YToLat(y, 22);
		return new EastNorthCoordinate(lat, lon);
	}
}
