package mobac.program.jaxb;

import java.awt.Point;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class PolygonType {

	@XmlElement(name = "point")
	@XmlJavaTypeAdapter(PointAdapter.class)
	public Vector<Point> points;

	protected PolygonType() {
		points = new Vector<Point>(20);
	}

	public PolygonType(Vector<Point> points) {
		this.points = points;
	}

}
