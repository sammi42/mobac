package mobac.gui.mapview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

public class ReferenceMapMarker extends MapMarkerDot {

	protected static Stroke CIRCLE_STROKE = new BasicStroke(5.0f);
	protected static Stroke LINE_STROKE = new BasicStroke(1.0f);

	public ReferenceMapMarker(Color circleColor, double lat, double lon) {
		super(circleColor, lat, lon);
	}

	@Override
	public void paint(Graphics2D g, Point position) {
		int size_h = 10;
		int size = size_h * 2+1;
		g.setStroke(LINE_STROKE);
		g.setColor(Color.BLACK);
		g.drawLine(position.x, position.y - size_h, position.x, position.y + size_h);
		g.drawLine(position.x - size_h, position.y, position.x + size_h, position.y);
		g.setColor(color);
		g.setStroke(CIRCLE_STROKE);
		g.drawOval(position.x - size_h, position.y - size_h, size, size);
	}

}
