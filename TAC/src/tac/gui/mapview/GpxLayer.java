package tac.gui.mapview;

import java.awt.Color;
import java.awt.Graphics2D;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapLayer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.data.gpx.gpx11.Gpx;
import tac.data.gpx.gpx11.WptType;

/**
 * A {@link MapLayer} displaying the content of a loaded GPX file in a
 * {@link JMapViewer} instance.
 */
public class GpxLayer implements MapLayer {

	private static int POINT_RADIUS = 3;
	private static int POINT_DIAMETER = 2 * POINT_RADIUS;

	private Color pointColor = Color.RED;

	// private Logger log = Logger.getLogger(GpxLayer.class);

	private Gpx gpx;

	private boolean showWaypoints;
	private boolean showWaypointName;
	private boolean showTrackpoints;

	public GpxLayer(Gpx gpx) {
		this.gpx = gpx;
		showWaypoints = true;
		showWaypointName = true;
	}

	public void paint(JMapViewer map, Graphics2D g, int zoom, int minX, int minY, int maxX, int maxY) {
		g.setColor(pointColor);
		final MapSpace mapSpace = map.getMapSource().getMapSpace();
		if (showWaypoints) {
			for (WptType wpt : gpx.getWpt()) {
				int x = mapSpace.cLonToX(wpt.getLon().doubleValue(), zoom);
				if (x < minX || x > maxX)
					continue; // Point outside of visible region
				int y = mapSpace.cLatToY(wpt.getLat().doubleValue(), zoom);
				if (y < minY || y > maxY)
					continue; // Point outside of visible region
				x -= minX;
				y -= minY;
				g.setColor(pointColor);
				g.fillOval(x - POINT_RADIUS, y - POINT_RADIUS, POINT_DIAMETER, POINT_DIAMETER);
				g.setColor(Color.BLACK);
				g.drawOval(x - POINT_RADIUS, y - POINT_RADIUS, POINT_DIAMETER, POINT_DIAMETER);
				if (showWaypointName && wpt.getName() != null)
					g.drawString(wpt.getName(), x + POINT_RADIUS + 5, y - POINT_RADIUS);
			}
		}
		if (showTrackpoints) {
			// TODO
			throw new RuntimeException("not implemented");
		}
	}

	public Gpx getGpx() {
		return gpx;
	}

}
