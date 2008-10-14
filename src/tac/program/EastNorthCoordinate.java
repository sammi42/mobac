package tac.program;

import java.awt.geom.Point2D;

public class EastNorthCoordinate {
	public double lat;
	public double lon;

	public EastNorthCoordinate() {
		lat = Double.NaN;
		lon = Double.NaN;
	}

	public EastNorthCoordinate(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public EastNorthCoordinate(Point2D.Double c) {
		this.lat = c.y;
		this.lon = c.x;
	}
}
