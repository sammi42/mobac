package mobac.program.jaxb;

import java.awt.Point;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Required {@link XmlAdapter} implementation for serializing a {@link Point} as
 * the default one creates a {@link StackOverflowError}
 * 
 */
public class PointAdapter extends XmlAdapter<String, Point> {

	@Override
	public String marshal(Point point) throws Exception {
		return point.x + "/" + point.y;
	}

	@Override
	public Point unmarshal(String value) throws Exception {
		int i = value.indexOf('/');
		if (i < 0)
			throw new UnmarshalException("Invalid format");
		int x = Integer.parseInt(value.substring(0, i));
		int y = Integer.parseInt(value.substring(i + 1));
		return new Point(x, y);
	}
}
