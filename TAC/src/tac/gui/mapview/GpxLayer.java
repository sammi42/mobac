package tac.gui.mapview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapLayer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.data.gpx.gpx11.Gpx;
import tac.data.gpx.gpx11.RteType;
import tac.data.gpx.gpx11.TrkType;
import tac.data.gpx.gpx11.TrksegType;
import tac.data.gpx.gpx11.WptType;

/**
 * A {@link MapLayer} displaying the content of a loaded GPX file in a
 * {@link JMapViewer} instance.
 */
public class GpxLayer implements MapLayer {

	private static int POINT_RADIUS = 4;
	private static int POINT_DIAMETER = 2 * POINT_RADIUS;

	private Color wptPointColor = new Color(0,0,200);
	private Color trkPointColor = Color.RED;
	private Color rtePointColor = new Color(0,200,0);

	private Stroke outlineStroke = new BasicStroke(1); 
	
	// private Logger log = Logger.getLogger(GpxLayer.class);

	private Gpx gpx;

	private boolean showWaypoints = true;
	private boolean showWaypointName = true;
	private boolean showTrackpoints = true;
	private boolean showTrackpointName = true;
	private boolean showRoutepoints = true;
	private boolean showRoutepointName = true;

	public GpxLayer(Gpx gpx) {
		this.gpx = gpx;
	}

	public void paint(JMapViewer map, Graphics2D g, int zoom, int minX, int minY, int maxX, int maxY) {
		g.setColor(wptPointColor);
		final MapSpace mapSpace = map.getMapSource().getMapSpace();
		if (showWaypoints) {
			for (WptType pt : gpx.getWpt()) {
				paintPoint(pt, wptPointColor, g, showWaypointName, mapSpace, zoom, minX, minY,
						maxX, maxY);
			}
		}
		if (showTrackpoints) {
			for (TrkType trk : gpx.getTrk()) {
				for (TrksegType seg : trk.getTrkseg()) {
					for (WptType pt : seg.getTrkpt()) {
						paintPoint(pt, trkPointColor, g, showTrackpointName, mapSpace, zoom, minX,
								minY, maxX, maxY);
					}
				}
			}
		}
		if (showRoutepoints) {
			for (RteType rte : gpx.getRte()) {
				for (WptType pt : rte.getRtept()) {
					paintPoint(pt, rtePointColor, g, showRoutepointName, mapSpace, zoom, minX,
							minY, maxX, maxY);
				}
			}
		}
	}

	private boolean paintPoint(final WptType point, Color color, final Graphics2D g,
			boolean paintPointName, MapSpace mapSpace, int zoom, int minX, int minY, int maxX,
			int maxY) {
		int x = mapSpace.cLonToX(point.getLon().doubleValue(), zoom);
		if (x < minX || x > maxX)
			return false; // Point outside of visible region
		int y = mapSpace.cLatToY(point.getLat().doubleValue(), zoom);
		if (y < minY || y > maxY)
			return false; // Point outside of visible region
		x -= minX;
		y -= minY;
		g.setColor(color);
		g.fillOval(x - POINT_RADIUS, y - POINT_RADIUS, POINT_DIAMETER, POINT_DIAMETER);
		g.setColor(Color.BLACK);
		g.setStroke(outlineStroke);
		g.drawOval(x - POINT_RADIUS, y - POINT_RADIUS, POINT_DIAMETER, POINT_DIAMETER);
		if (paintPointName && point.getName() != null)
			g.drawString(point.getName(), x + POINT_RADIUS + 5, y - POINT_RADIUS);

		return true;
	}

	public Gpx getGpx() {
		return gpx;
	}

}
