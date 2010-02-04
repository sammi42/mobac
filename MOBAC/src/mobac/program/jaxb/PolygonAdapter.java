package mobac.program.jaxb;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Vector;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mobac.program.Logging;

/**
 * Required {@link XmlAdapter} implementation for serializing a {@link Polygon}
 */
public class PolygonAdapter extends XmlAdapter<PolygonType, Polygon> {

	@Override
	public PolygonType marshal(Polygon polygon) throws Exception {
		Vector<Point> points = new Vector<Point>(polygon.npoints);
		for (int i = 0; i < polygon.npoints; i++) {
			Point p = new Point(polygon.xpoints[i], polygon.ypoints[i]);
			points.add(p);
			Logging.LOG.debug("Point: " + p);
		}
		return new PolygonType(points);
	}

	@Override
	public Polygon unmarshal(PolygonType value) throws Exception {
		int npoints = value.points.size();
		int[] xpoints = new int[npoints];
		int[] ypoints = new int[npoints];
		for (int i = 0; i < npoints; i++) {
			Point p = value.points.get(i);
			xpoints[i] = p.x;
			ypoints[i] = p.y;
		}

		return new Polygon(xpoints, ypoints, npoints);
	}

}
