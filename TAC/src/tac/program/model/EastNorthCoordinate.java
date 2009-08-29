package tac.program.model;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.openstreetmap.gui.jmapviewer.interfaces.MapScale;

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

	public EastNorthCoordinate(MapScale mapScale, int zoom, int pixelCoordinateX,
			int pixelCoordinateY) {
		this.lat = mapScale.cYToLat(pixelCoordinateY, zoom);
		this.lon = mapScale.cXToLon(pixelCoordinateX, zoom);
	}

	public EastNorthCoordinate(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public EastNorthCoordinate(Point2D.Double c) {
		this.lat = c.y;
		this.lon = c.x;
	}

	public Point toTileCoordinate(MapScale mapScale, int zoom) {
		int x = mapScale.cLonToX(lon, zoom);
		int y = mapScale.cLatToY(lat, zoom);
		return new Point(x, y);
	}

	@Override
	public String toString() {
		return Utilities.prettyPrintLatLon(lat, true) + " "
				+ Utilities.prettyPrintLatLon(lon, false);
	}

}
