package mobac.gui.mapview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import mobac.mapsources.MapSourcesManager;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.MapSelection;
import mobac.program.model.MercatorPixelCoordinate;
import mobac.program.model.Settings;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

public class PreviewMap extends JMapViewer implements ComponentListener {

	private static final long serialVersionUID = 1L;

	public static final Color GRID_COLOR = new Color(200, 20, 20, 130);
	public static final Color SEL_COLOR = new Color(0.9f, 0.7f, 0.7f, 0.6f);
	public static final Color MAP_COLOR = new Color(1.0f, 0.84f, 0.0f, 0.4f);

	public static final int MAP_CONTROLLER_RECTANGLE_SELECT = 0;
	public static final int MAP_CONTROLLER_GPX = 1;

	private static Logger log = Logger.getLogger(PreviewMap.class);

	/**
	 * Interactive map selection max/min pixel coordinates regarding zoom level
	 * <code>MAX_ZOOM</code>
	 */
	private Point iSelectionMin;
	private Point iSelectionMax;

	/**
	 * Map selection max/min pixel coordinates regarding zoom level
	 * <code>MAX_ZOOM</code> with respect to the grid zoom.
	 */
	private Point gridSelectionStart;
	private Point gridSelectionEnd;

	/**
	 * Pre-painted tile with grid lines on it. This makes painting the grid a
	 * lot faster in difference to painting each line or rectangle if the grid
	 * zoom is much higher that the current zoom level.
	 */
	private BufferedImage gridTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

	private int gridZoom = -1;
	private int gridSize;

	protected LinkedList<MapEventListener> mapEventListeners = new LinkedList<MapEventListener>();

	protected JMapController mapKeyboardController;
	protected JMapController mapSelectionController;

	public PreviewMap() {
		super(MapSourcesManager.DEFAULT, new PreviewTileCache(), 5);
		new DefaultMapController(this);
		mapSource = MapSourcesManager.DEFAULT;
		// tileLoader = new OsmTileLoader(this);
		OsmFileCacheTileLoader cacheTileLoader = new OsmFileCacheTileLoader(this);
		setTileLoader(cacheTileLoader);
		mapMarkersVisible = false;
		setZoomContolsVisible(false);

		mapKeyboardController = new MapKeyboardController(this, true);
		mapSelectionController = new PreviewMapController(this, true);

		addComponentListener(this);
	}

	public void setDisplayPositionByLatLon(EastNorthCoordinate c, int zoom) {
		setDisplayPositionByLatLon(new Point(getWidth() / 2, getHeight() / 2), c.lat, c.lon, zoom);
	}

	/**
	 * Updates the current position in {@link Settings} to the current view
	 */
	public void settingsSave() {
		Settings settings = Settings.getInstance();
		settings.mapviewZoom = getZoom();
		settings.mapviewCenterCoordinate = getPositionCoordinate();
		settings.mapviewGridZoom = gridZoom;
		settings.mapviewMapSource = mapSource.getName();
		settings.mapviewSelectionMin = iSelectionMin;
		settings.mapviewSelectionMax = iSelectionMax;
	}

	/**
	 * Sets the current view by the current values from {@link Settings}
	 */
	public void settingsLoad() {
		Settings settings = Settings.getInstance();
		MapSource mapSource = MapSourcesManager.getSourceByName(settings.mapviewMapSource);
		if (mapSource != null)
			setMapSource(mapSource);
		EastNorthCoordinate c = settings.mapviewCenterCoordinate;
		gridZoom = settings.mapviewGridZoom;
		setDisplayPositionByLatLon(c, settings.mapviewZoom);
		setSelectionByTileCoordinate(MAX_ZOOM, settings.mapviewSelectionMin,
				settings.mapviewSelectionMax, true);
	}

	@Override
	public void setMapSource(MapSource newMapSource) {
		if (newMapSource.equals(mapSource))
			return;
		log.trace("Preview map source changed from " + mapSource + " to " + newMapSource);
		super.setMapSource(newMapSource);
		if (mapEventListeners == null)
			return;
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
		updateGridValues();
		applyGridOnSelection();
		updateMapSelection();
		repaint();
	}

	public int getGridZoom() {
		return gridZoom;
	}

	protected void updateGridValues() {
		if (gridZoom < 0)
			return;
		int zoomToGridZoom = zoom - gridZoom;
		int tilesize = mapSource.getMapSpace().getTileSize();
		if (zoomToGridZoom > 0) {
			gridSize = tilesize << zoomToGridZoom;
		} else {
			gridSize = tilesize >> (-zoomToGridZoom);
			BufferedImage newGridTile = new BufferedImage(tilesize, tilesize,
					BufferedImage.TYPE_INT_ARGB);
			if (gridSize > 2) {
				Graphics2D g = newGridTile.createGraphics();
				int alpha = 5 + (6 + zoomToGridZoom) * 16;
				alpha = Math.max(0, alpha);
				alpha = Math.min(130, alpha);
				g.setColor(new Color(200, 20, 20, alpha));
				for (int x = 0; x < tilesize; x += gridSize)
					g.drawLine(x, 0, x, 255);
				for (int y = 0; y < tilesize; y += gridSize)
					g.drawLine(0, y, 255, y);
			}
			gridTile = newGridTile;
		}
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		if (!isEnabled())
			return;
		Graphics2D g = (Graphics2D) graphics;
		super.paintComponent(g);
		Point tlc = getTopLeftCoordinate();
		if (gridZoom >= 0) {
			// Only paint grid if it is enabled (gridZoom not -1)
			int max = (256 << zoom);
			int w = Math.min(getWidth(), max - tlc.x);
			int h = Math.min(getHeight(), max - tlc.y);
			g.setColor(GRID_COLOR);
			if (gridSize > 1) {
				int posx;
				int posy;
				if (gridSize >= mapSource.getMapSpace().getTileSize()) {
					int tilesize = mapSource.getMapSpace().getTileSize();
					posx = -tlc.x % tilesize;
					posy = -tlc.y % tilesize;
					for (int x = posx; x < w; x += gridSize) {
						g.drawLine(x, posy, x, h);
					}
					for (int y = posy; y < h; y += gridSize) {
						g.drawLine(posx, y, w, y);
					}
				} else {
					int off_x = tlc.x % 256;
					int off_y = tlc.y % 256;
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

			int w = x_max - x_min + 1;
			int h = y_max - y_min + 1;
			g.setColor(SEL_COLOR);
			g.fillRect(x_min, y_min, w, h);
		}
		if (iSelectionMin != null && iSelectionMax != null) {
			int zoomDiff = MAX_ZOOM - zoom;
			int x_min = (iSelectionMin.x >> zoomDiff) - tlc.x;
			int y_min = (iSelectionMin.y >> zoomDiff) - tlc.y;
			int x_max = (iSelectionMax.x >> zoomDiff) - tlc.x;
			int y_max = (iSelectionMax.y >> zoomDiff) - tlc.y;

			int w = x_max - x_min + 1;
			int h = y_max - y_min + 1;
			g.setColor(GRID_COLOR);
			g.drawRect(x_min, y_min, w, h);
		}
		ScaleBar.paintScaleBar(this, g, mapSource.getMapSpace(), tlc, zoom);
	}

	public EastNorthCoordinate getPositionCoordinate() {
		MapSpace mapSpace = mapSource.getMapSpace();
		double lon = mapSpace.cXToLon(center.x, zoom);
		double lat = mapSpace.cYToLat(center.y, zoom);
		return new EastNorthCoordinate(lat, lon);
	}

	protected Point getTopLeftCoordinate() {
		return new Point(center.x - (getWidth() / 2), center.y - (getHeight() / 2));
	}

	public void zoomTo(MapSelection ms) {
		if (!ms.isAreaSelected())
			return;
		log.trace("Setting selection to: " + ms);
		Point max = ms.getBottomRightPixelCoordinate(MAX_ZOOM);
		Point min = ms.getTopLeftPixelCoordinate(MAX_ZOOM);
		setDisplayToFitPixelCoordinates(max.x, max.y, min.x, min.y);
	}

	/**
	 * Zooms to the specified {@link MapSelection} and sets the selection to it;
	 * 
	 * @param ms
	 * @param notifyListeners
	 */
	public void setSelectionAndZoomTo(MapSelection ms, boolean notifyListeners) {
		if (!ms.isAreaSelected())
			return;
		log.trace("Setting selection to: " + ms);
		Point max = ms.getBottomRightPixelCoordinate(MAX_ZOOM);
		Point min = ms.getTopLeftPixelCoordinate(MAX_ZOOM);
		setDisplayToFitPixelCoordinates(max.x, max.y, min.x, min.y);
		Point pStart = ms.getTopLeftPixelCoordinate(zoom);
		Point pEnd = ms.getBottomRightPixelCoordinate(zoom);
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
			iSelectionMin = null;
			iSelectionMax = null;
			gridSelectionStart = null;
			gridSelectionEnd = null;
			return;
		}

		Point pNewStart = new Point();
		Point pNewEnd = new Point();
		int mapMaxCoordinate = mapSource.getMapSpace().getMaxPixels(cZoom);
		pNewStart.x = Math.max(0, Math.min(mapMaxCoordinate, pStart.x));
		pNewStart.y = Math.max(0, Math.min(mapMaxCoordinate, pStart.y));
		pNewEnd.x = Math.max(0, Math.min(mapMaxCoordinate, pEnd.x));
		pNewEnd.y = Math.max(0, Math.min(mapMaxCoordinate, pEnd.y));

		int zoomDiff = MAX_ZOOM - cZoom;

		pNewEnd.x <<= zoomDiff;
		pNewEnd.y <<= zoomDiff;
		pNewStart.x <<= zoomDiff;
		pNewStart.y <<= zoomDiff;

		iSelectionMin = pNewStart;
		iSelectionMax = pNewEnd;
		gridSelectionStart = null;
		gridSelectionEnd = null;

		applyGridOnSelection();

		if (notifyListeners)
			updateMapSelection();
		repaint();
	}

	protected void applyGridOnSelection() {
		if (gridZoom < 0) {
			gridSelectionStart = iSelectionMin;
			gridSelectionEnd = iSelectionMax;
			return;
		}

		if (iSelectionMin == null || iSelectionMax == null)
			return;

		int gridZoomDiff = MAX_ZOOM - gridZoom;
		int gridFactor = mapSource.getMapSpace().getTileSize() << gridZoomDiff;

		Point pNewStart = new Point(iSelectionMin);
		Point pNewEnd = new Point(iSelectionMax);

		// Snap to the current grid
		pNewStart.x = pNewStart.x - (pNewStart.x % gridFactor);
		pNewStart.y = pNewStart.y - (pNewStart.y % gridFactor);
		pNewEnd.x += gridFactor;
		pNewEnd.y += gridFactor;
		pNewEnd.x = pNewEnd.x - (pNewEnd.x % gridFactor) - 1;
		pNewEnd.y = pNewEnd.y - (pNewEnd.y % gridFactor) - 1;

		gridSelectionStart = pNewStart;
		gridSelectionEnd = pNewEnd;
	}

	public void updateMapSelection() {
		int x_min, y_min, x_max, y_max;

		if (gridZoom >= 0) {
			if (gridSelectionStart == null || gridSelectionEnd == null)
				return;
			x_min = gridSelectionStart.x;
			y_min = gridSelectionStart.y;
			x_max = gridSelectionEnd.x;
			y_max = gridSelectionEnd.y;
		} else {
			if (iSelectionMin == null || iSelectionMax == null)
				return;
			x_min = iSelectionMin.x;
			y_min = iSelectionMin.y;
			x_max = iSelectionMax.x;
			y_max = iSelectionMax.y;
		}
		MercatorPixelCoordinate min = new MercatorPixelCoordinate(mapSource.getMapSpace(), x_min,
				y_min, MAX_ZOOM);
		MercatorPixelCoordinate max = new MercatorPixelCoordinate(mapSource.getMapSpace(), x_max,
				y_max, MAX_ZOOM);
		// log.debug("sel min: [" + min + "]");
		// log.debug("sel max: [" + max + "]");
		for (MapEventListener listener : mapEventListeners)
			listener.selectionChanged(max, min);
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

	/**
	 * Clears the in-memory tile cache and performs a repaint which causes a
	 * reload of all displayed tiles (from disk or if not present from the map
	 * source via network).
	 */
	public void refreshMap() {
		((MemoryTileCache) tileCache).clear();
		repaint();
	}

	public JMapController getMapKeyboardController() {
		return mapKeyboardController;
	}

	public JMapController getMapSelectionController() {
		return mapSelectionController;
	}

}
