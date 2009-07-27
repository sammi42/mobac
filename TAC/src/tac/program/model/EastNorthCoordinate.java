package tac.program.model;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.openstreetmap.gui.jmapviewer.OsmMercator;

import tac.utilities.Utilities;

@XmlRootElement
public class EastNorthCoordinate {

	@XmlAttribute
	public double lat;
	@XmlAttribute
	public double lon;

	public EastNorthCoordinate() {
		lat = Double.NaN;
		lon = Double.NaN;
	}

	public EastNorthCoordinate(int zoom, int tileNumX, int tileNumY) {
		this.lat = OsmMercator.YToLat(tileNumY, zoom);
		this.lon = OsmMercator.XToLon(tileNumX, zoom);
	}

	public EastNorthCoordinate(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public EastNorthCoordinate(Point2D.Double c) {
		this.lat = c.y;
		this.lon = c.x;
	}

	public Point toTileCoordinate(int zoom) {
		int x = OsmMercator.LonToX(lon, zoom);
		int y = OsmMercator.LatToY(lat, zoom);
		return new Point(x, y);
	}

	@Override
	public String toString() {
		return Utilities.prettyPrintLatLon(lat, true) + " "
				+ Utilities.prettyPrintLatLon(lon, false);
	}

}
