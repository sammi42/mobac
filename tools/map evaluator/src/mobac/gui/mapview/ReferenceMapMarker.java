package mobac.gui.mapview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import mobac.gui.mapview.interfaces.MapMarker;

public class ReferenceMapMarker implements MapMarker {

	protected static Stroke CIRCLE_STROKE = new BasicStroke(5.0f);
	protected static Stroke LINE_STROKE = new BasicStroke(1.0f);

	private final Color circleColor;
	private final double lat;
	private final double lon;
	
	public ReferenceMapMarker(Color circleColor, double lat, double lon) {
		this.circleColor = circleColor;
		this.lat = lat;
		this.lon = lon;
	}

	public void paint(Graphics2D g, Point position) {
		int size_h = 10;
		int size = size_h * 2+1;
		g.setStroke(LINE_STROKE);
		g.setColor(Color.BLACK);
		g.drawLine(position.x, position.y - size_h, position.x, position.y + size_h);
		g.drawLine(position.x - size_h, position.y, position.x + size_h, position.y);
		g.setColor(circleColor);
		g.setStroke(CIRCLE_STROKE);
		g.drawOval(position.x - size_h, position.y - size_h, size, size);
	}

	@Override
	public double getLat() {
		return lat;
	}

	@Override
	public double getLon() {
		return lon;
	}

}
