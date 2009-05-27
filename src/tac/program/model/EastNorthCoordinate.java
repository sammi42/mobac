package tac.program.model;

import java.awt.geom.Point2D;

import org.openstreetmap.gui.jmapviewer.OsmMercator;

import tac.utilities.Utilities;

public class EastNorthCoordinate {
	public double lat;
	public double lon;

	public EastNorthCoordinate() {
		lat = Double.NaN;
		lon = Double.NaN;
	}

	public EastNorthCoordinate(int zoom, int tileNumX, int tileNumY) {
		this.lat = OsmMercator.YToLat(tileNumY, zoom);
		this.lon = OsmMercator.XToLon(tileNumY, zoom);
	}

	public EastNorthCoordinate(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public EastNorthCoordinate(Point2D.Double c) {
		this.lat = c.y;
		this.lon = c.x;
	}

	@Override
	public String toString() {
		return Utilities.prettyPrintLatLon(lat, true) + " "
				+ Utilities.prettyPrintLatLon(lon, false);
	}

}
