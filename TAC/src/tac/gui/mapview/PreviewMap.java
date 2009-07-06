package tac.gui.mapview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.mapsources.MapSourcesManager;
import tac.program.MapSelection;
import tac.program.model.EastNorthCoordinate;
import tac.program.model.Settings;

public class PreviewMap extends JMapViewer implements ComponentListener {

	private static Logger log = Logger.getLogger(PreviewMap.class);
	private static final long serialVersionUID = 1L;
	public static final Color GRID_COLOR = new Color(200, 20, 20, 130);
	public static final Color SEL_COLOR = new Color(0.9f, 0.7f, 0.7f, 0.6f);
	public static final Color MAP_COLOR = new Color(1.0f, 0.84f, 0.0f, 0.4f);

	private Point iSelectionRectStart;
	private Point iSelectionRectEnd;
	private Point gridSelectionStart;
	private Point gridSelectionEnd;

	private BufferedImage gridTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

	private int gridZoom = 10;
	private int gridFactor;
	private int gridSize;

	public LinkedList<MapEventListener> mapEventListeners = new LinkedList<MapEventListener>();

	public PreviewMap() {
		super(new PreviewTileCache(), 5);
		new DefaultMapController(this);
		mapSource = MapSourcesManager.DEFAULT;
		// tileLoader = new OsmTileLoader(this);
		OsmFileCacheTileLoader cacheTileLoader = new OsmFileCacheTileLoader(this);
		cacheTileLoader.setCacheMaxFileAge(OsmFileCacheTileLoader.FILE_AGE_ONE_WEEK);
		cacheTileLoader.setTileCacheDir("./tilestore");
		setTileLoader(cacheTileLoader);
		mapMarkersVisible = false;
		setZoomContolsVisible(false);

		new PreviewMapController(this);
		addComponentListener(this);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				grabFocus();
			}
		});
	}

	public void setDisplayPositionByLatLon(EastNorthCoordinate c, int zoom) {
		setDisplayPositionByLatLon(new Point(getWidth() / 2, getHeight() / 2), c.lat, c.lon, zoom);
	}

	/**
	 * Updates the current position in {@link Settings} to the current view
	 */
	public void settingsSavePosition() {
		Settings settings = Settings.getInstance();
		settings.setMapviewZoom(getZoom());
		settings.setMapviewCenterCoordinate(getPositionCoordinate());
	}

	/**
	 * Sets the current view by the current values from {@link Settings}
	 */
	public void settingsLoadPosition() {
		Settings settings = Settings.getInstance();
		EastNorthCoordinate c = settings.getMapviewCenterCoordinate();
		setDisplayPositionByLatLon(c, settings.getMapviewZoom());
	}

	@Override
	public void setMapSource(MapSource newMapSource) {
		if (mapSource.equals(newMapSource))
			return;
		log.trace("Preview map source changed from " + mapSource + " to " + newMapSource);
		super.setMapSource(newMapSource);
		for (MapEventListener listener : mapEventListeners)
			listener.mapSourceChanged(mapSource);
	}

	protected void zoomChanged(int oldZoom) {
		log.trace("Preview map zoom changed from " + oldZoom + " to " + zoom);
		if (mapEventListeners != null)
			for (MapEventListener listener : mapEventListeners)
				listener.zoomChanged(zoom);
		updateGridValues();
	}

	public void setGridZoom(int gridZoom) {
		if (gridZoom == this.gridZoom)
			return;
		this.gridZoom = gridZoom;
		if (gridZoom < 0) {
			gridFactor = 0;
			return;
		}
		int gridZoomDiff = 20 - gridZoom;
		gridFactor = 256 << gridZoomDiff;
		updateGridValues();
		if (iSelectionRectStart != null && iSelectionRectEnd != null) {
			Point pStart = new Point(iSelectionRectStart);
			Point pEnd = new Point(iSelectionRectEnd);

			// Snap to the current grid
			pStart.x = pStart.x - (pStart.x % gridFactor);
			pStart.y = pStart.y - (pStart.y % gridFactor);
			pEnd.x += gridFactor - 1;
			pEnd.y += gridFactor - 1;
			pEnd.x = pEnd.x - (pEnd.x % gridFactor);
			pEnd.y = pEnd.y - (pEnd.y % gridFactor);

			gridSelectionStart = pStart;
			gridSelectionEnd = pEnd;
		}
		repaint();
	}

	protected void updateGridValues() {
		if (gridZoom < 0)
			return;
		int zoomToGridZoom = zoom - gridZoom;
		if (zoomToGridZoom > 0) {
			gridSize = 256 << zoomToGridZoom;
		} else {
			gridSize = 256 >> (-zoomToGridZoom);
			BufferedImage newGridTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
			if (gridSize > 2) {
				Graphics2D g = newGridTile.createGraphics();
				int alpha = 5 + (6 + zoomToGridZoom) * 16;
				alpha = Math.max(0, alpha);
				alpha = Math.min(130, alpha);
				g.setColor(new Color(200, 20, 20, alpha));
				for (int x = 0; x < 256; x += gridSize)
					g.drawLine(x, 0, x, 255);
				for (int y = 0; y < 256; y += gridSize)
					g.drawLine(0, y, 255, y);
			}
			gridTile = newGridTile;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (!isEnabled())
			return;
		super.paintComponent(g);
		Point tlc = getTopLeftCoordinate();
		if (gridZoom >= 0) {
			// Only paint grid if it is enabled (gridZoom not -1)
			int max = (256 << zoom);
			int w = Math.min(getWidth(), max - tlc.x);
			int h = Math.min(getHeight(), max - tlc.y);
			g.setColor(GRID_COLOR);
			int off_x = (tlc.x % Tile.SIZE);
			int off_y = (tlc.y % Tile.SIZE);
			if (gridSize > 1) {
				int posx;
				int posy;
				if (gridSize >= Tile.SIZE) {
					posx = -(tlc.x % gridSize);
					posy = -(tlc.y % gridSize);
					for (int x = posx; x < w; x += gridSize) {
						g.drawLine(x, 0, x, h);
					}
					for (int y = posy; y < h; y += gridSize) {
						g.drawLine(0, y, w, y);
					}
				} else {
					// posx = -Tile.WIDTH + w / 2 - off_x;
					// posy = -Tile.HEIGHT + h / 2 - off_y;
					for (int x = -off_x; x < w; x += 256) {
						for (int y = -off_y; y < h; y += 256) {
							g.drawImage(gridTile, x, y, null);
						}
					}
				}
			}
		}
		if (gridSelectionStart != null && gridSelectionEnd != null) {
			int zoomDiff = MAX_ZOOM - zoom;
			int x_min = (gridSelectionStart.x >> zoomDiff) - tlc.x;
			int y_min = (gridSelectionStart.y >> zoomDiff) - tlc.y;
			int x_max = (gridSelectionEnd.x >> zoomDiff) - tlc.x;
			int y_max = (gridSelectionEnd.y >> zoomDiff) - tlc.y;

			int w = x_max - x_min;
			int h = y_max - y_min;
			g.setColor(SEL_COLOR);
			g.fillRect(x_min, y_min, w, h);
			g.setColor(GRID_COLOR);
			g.drawRect(x_min, y_min, w, h);
		}
	}

	public EastNorthCoordinate getPositionCoordinate() {
		double lon = OsmMercator.XToLon(center.x, zoom);
		double lat = OsmMercator.YToLat(center.y, zoom);
		return new EastNorthCoordinate(lat, lon);
	}

	protected Point getTopLeftCoordinate() {
		return new Point(center.x - (getWidth() / 2), center.y - (getHeight() / 2));
	}

	public void zoomToSelection(MapSelection ms, boolean notifyListeners) {
		if (ms.getLat_max() == ms.getLat_min() || ms.getLon_max() == ms.getLon_min())
			return;
		log.trace("Setting selection to: " + ms);
		ArrayList<MapMarker> mml = new ArrayList<MapMarker>(2);
		mml.add(new MapMarkerDot(ms.getLat_max(), ms.getLon_max()));
		mml.add(new MapMarkerDot(ms.getLat_min(), ms.getLon_min()));
		setMapMarkerList(mml);
		setDisplayToFitMapMarkers();
		Point pStart = ms.getTopLeftTileCoordinate(zoom);
		Point pEnd = ms.getBottomRightTileCoordinate(zoom);
		setSelectionByTileCoordinate(pStart, pEnd, notifyListeners);
	}

	public void setSelectionByScreenPoint(Point aStart, Point aEnd, boolean notifyListeners) {
		if (aStart == null || aEnd == null)
			return;
		Point p_max = new Point(Math.max(aEnd.x, aStart.x), Math.max(aEnd.y, aStart.y));
		Point p_min = new Point(Math.min(aEnd.x, aStart.x), Math.min(aEnd.y, aStart.y));

		Point tlc = getTopLeftCoordinate();

		Point pEnd = new Point(p_max.x + tlc.x, p_max.y + tlc.y);
		Point pStart = new Point(p_min.x + tlc.x, p_min.y + tlc.y);
		setSelectionByTileCoordinate(pStart, pEnd, notifyListeners);
	}

	/**
	 * 
	 * @param pStart
	 *            x/y tile coordinate of the top left tile regarding the current
	 *            zoom level
	 * @param pEnd
	 *            x/y tile coordinate of the bottom right tile regarding the
	 *            current zoom level
	 * @param notifyListeners
	 */
	public void setSelectionByTileCoordinate(Point pStart, Point pEnd, boolean notifyListeners) {
		setSelectionByTileCoordinate(zoom, pStart, pEnd, notifyListeners);
	}

	public void setSelectionByTileCoordinate(int cZoom, Point pStart, Point pEnd,
			boolean notifyListeners) {
		if (pStart == null || pEnd == null) {
			iSelectionRectStart = null;
			iSelectionRectEnd = null;
			gridSelectionStart = null;
			gridSelectionEnd = null;
			return;
		}

		Point pNewStart = new Point();
		Point pNewEnd = new Point();
		int mapMaxCoordinate = Tile.SIZE << cZoom;
		pNewStart.x = Math.max(0, Math.min(mapMaxCoordinate, pStart.x));
		pNewStart.y = Math.max(0, Math.min(mapMaxCoordinate, pStart.y));
		pNewEnd.x = Math.max(0, Math.min(mapMaxCoordinate, pEnd.x));
		pNewEnd.y = Math.max(0, Math.min(mapMaxCoordinate, pEnd.y));

		int zoomDiff = MAX_ZOOM - cZoom;

		pNewEnd.x <<= zoomDiff;
		pNewEnd.y <<= zoomDiff;
		pNewStart.x <<= zoomDiff;
		pNewStart.y <<= zoomDiff;

		if (gridZoom >= 0) {
			int gridZoomDiff = MAX_ZOOM - gridZoom;
			int gridFactor = Tile.SIZE << gridZoomDiff;

			// Snap to the current grid
			pNewStart.x = pNewStart.x - (pNewStart.x % gridFactor);
			pNewStart.y = pNewStart.y - (pNewStart.y % gridFactor);
			pNewEnd.x += gridFactor - 1;
			pNewEnd.y += gridFactor - 1;
			pNewEnd.x = pNewEnd.x - (pNewEnd.x % gridFactor);
			pNewEnd.y = pNewEnd.y - (pNewEnd.y % gridFactor);
		}
		iSelectionRectStart = pNewStart;
		iSelectionRectEnd = pNewEnd;
		gridSelectionStart = pNewStart;
		gridSelectionEnd = pNewEnd;
		if (notifyListeners) {
			updateMapSelection();
		}
		repaint();
	}

	public void updateMapSelection() {

		if (iSelectionRectStart == null || iSelectionRectEnd == null)
			return;

		int selectionZoom;
		int x_min, y_min, x_max, y_max;
		int zoomDiff1;

		if (gridZoom >= 0) {
			selectionZoom = gridZoom;
			zoomDiff1 = PreviewMap.MAX_ZOOM - selectionZoom;
		} else {
			selectionZoom = zoom;
			zoomDiff1 = PreviewMap.MAX_ZOOM - selectionZoom;
		}
		x_min = (gridSelectionStart.x >> zoomDiff1);
		y_min = (gridSelectionStart.y >> zoomDiff1);
		x_max = (gridSelectionEnd.x >> zoomDiff1);
		y_max = (gridSelectionEnd.y >> zoomDiff1);
		EastNorthCoordinate max = new EastNorthCoordinate();
		EastNorthCoordinate min = new EastNorthCoordinate();
		max.lon = OsmMercator.XToLon(x_max, selectionZoom);
		min.lat = OsmMercator.YToLat(y_max, selectionZoom);
		min.lon = OsmMercator.XToLon(x_min, selectionZoom);
		max.lat = OsmMercator.YToLat(y_min, selectionZoom);
		for (MapEventListener listener : mapEventListeners) {
			listener.selectionChanged(max, min);
		}
	}

	public void addMapEventListener(MapEventListener l) {
		mapEventListeners.add(l);
	}

	public void componentHidden(ComponentEvent e) {

	}

	public void componentMoved(ComponentEvent e) {

	}

	public void componentResized(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void selectPreviousMap() {
		for (MapEventListener listener : mapEventListeners) {
			listener.selectPreviousMapSource();
		}
	}

	public void selectNextMap() {
		for (MapEventListener listener : mapEventListeners) {
			listener.selectNextMapSource();
		}
	}

	public void refreshMap() {
		((MemoryTileCache) tileCache).clear();
		repaint();
	}

}
