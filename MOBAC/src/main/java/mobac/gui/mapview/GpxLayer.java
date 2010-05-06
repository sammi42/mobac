package mobac.gui.mapview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.io.File;

import mobac.data.gpx.gpx11.Gpx;
import mobac.data.gpx.gpx11.RteType;
import mobac.data.gpx.gpx11.TrkType;
import mobac.data.gpx.gpx11.TrksegType;
import mobac.data.gpx.gpx11.WptType;
import mobac.gui.panels.JGpxPanel;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapLayer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


/**
 * A {@link MapLayer} displaying the content of a loaded GPX file in a
 * {@link JMapViewer} instance.
 */
public class GpxLayer implements MapLayer {

	private static int POINT_RADIUS = 4;
	private static int POINT_DIAMETER = 2 * POINT_RADIUS;

	private Color wptPointColor = new Color(0, 0, 200);
	private Color trkPointColor = Color.RED;
	private Color rtePointColor = new Color(0, 200, 0);

	private Stroke outlineStroke = new BasicStroke(1);

	private Stroke lineStroke = new BasicStroke(2.0f);

	// private Logger log = Logger.getLogger(GpxLayer.class);

	/** the associated gpx file handle */
	private File file;
	/** the associated gpx object */
	private Gpx gpx;
	/** the associated panel that displays the nodes of the gpx file */
	private JGpxPanel panel;

	private boolean showWaypoints = true;
	private boolean showWaypointName = true;
	private boolean showTracks = true;
	private boolean showRoutes = true;

	private int lastTrackPointX = Integer.MIN_VALUE;
	private int lastTrackPointY = Integer.MIN_VALUE;

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
		if (showTracks) {
			for (TrkType trk : gpx.getTrk()) {
				for (TrksegType seg : trk.getTrkseg()) {
					lastTrackPointX = Integer.MIN_VALUE;
					lastTrackPointY = Integer.MIN_VALUE;
					for (WptType pt : seg.getTrkpt()) {
						paintTrack(pt, trkPointColor, g, mapSpace, zoom, minX, minY, maxX, maxY);
					}
				}
			}
		}
		if (showRoutes) {
			for (RteType rte : gpx.getRte()) {
				lastTrackPointX = Integer.MIN_VALUE;
				lastTrackPointY = Integer.MIN_VALUE;
				for (WptType pt : rte.getRtept()) {
					paintTrack(pt, rtePointColor, g, mapSpace, zoom, minX, minY, maxX, maxY);
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

	private boolean paintTrack(final WptType point, Color color, final Graphics2D g,
			MapSpace mapSpace, int zoom, int minX, int minY, int maxX, int maxY) {
		// Absolute map space coordinates
		int xAbs = mapSpace.cLonToX(point.getLon().doubleValue(), zoom);
		int yAbs = mapSpace.cLatToY(point.getLat().doubleValue(), zoom);
		// Relative coordinates regarding the top left point on map
		int x = xAbs - minX;
		int y = yAbs - minY;
		g.setColor(color);
		if (lastTrackPointX != Integer.MIN_VALUE && lastTrackPointY != Integer.MIN_VALUE) {
			g.setStroke(lineStroke);
			g.drawLine(lastTrackPointX, lastTrackPointY, x, y);
		}
		lastTrackPointX = x;
		lastTrackPointY = y;
		return true;
	}

	public Gpx getGpx() {
		return gpx;
	}

	public void setPanel(JGpxPanel panel) {
		this.panel = panel;
	}

	public JGpxPanel getPanel() {
		return panel;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

}
