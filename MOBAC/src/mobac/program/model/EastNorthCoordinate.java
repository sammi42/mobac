package mobac.program.model;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.utilities.Utilities;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


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

	public EastNorthCoordinate(MapSpace mapSpace, int zoom, int pixelCoordinateX,
			int pixelCoordinateY) {
		this.lat = mapSpace.cYToLat(pixelCoordinateY, zoom);
		this.lon = mapSpace.cXToLon(pixelCoordinateX, zoom);
	}

	public EastNorthCoordinate(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public EastNorthCoordinate(Point2D.Double c) {
		this.lat = c.y;
		this.lon = c.x;
	}

	public Point toTileCoordinate(MapSpace mapSpace, int zoom) {
		int x = mapSpace.cLonToX(lon, zoom);
		int y = mapSpace.cLatToY(lat, zoom);
		return new Point(x, y);
	}

	@Override
	public String toString() {
		return Utilities.prettyPrintLatLon(lat, true) + " "
				+ Utilities.prettyPrintLatLon(lon, false);
	}

}
