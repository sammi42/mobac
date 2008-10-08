package tac.gui.preview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JComboBox;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.program.MapSelection;

public class PreviewMap extends JMapViewer {
	private static final long serialVersionUID = 1L;
	private static final Color GRID_COLOR = new Color(200, 20, 20, 130);
	private static final Color SEL_COLOR = new Color(0.9f, 0.7f, 0.7f, 0.6f);

	private Point iSelectionRectStart;
	private Point iSelectionRectEnd;
	private Point gridSelectionStart;
	private Point gridSelectionEnd;

	private BufferedImage gridTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

	private int gridZoom = 10;
	private int gridFactor;
	private int gridSize;

	private JComboBox gridSizeSelector;

	private LinkedList<MapSelectionListener> mapSelectionListeners = new LinkedList<MapSelectionListener>();

	public PreviewMap() {
		super();
		tileCache = new PreviewTileCache();
		mapMarkersVisible = false;
		tileLoader = new OsmTileLoader(this);
		gridSizeSelector = new JComboBox();
		gridSizeSelector.setEditable(false);
		gridSizeSelector.setBounds(40, 10, 100, 20);
		gridSizeSelector.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				GridZoom g = (GridZoom) gridSizeSelector.getSelectedItem();
				if (g == null)
					return;
				setGridZoom(g.getZoom());
				repaint();
				updateMapSelection();
			}

		});
		add(gridSizeSelector);
		new PreviewMapController(this);
		setTileSource(new GoogleTileSource.GoogleMaps());
	}

	@Override
	public void setTileSource(TileSource newTileSource) {
		super.setTileSource(newTileSource);
		gridSizeSelector.removeAllItems();
		gridSizeSelector.setMaximumRowCount(tileSource.getMaxZoom() + 1);
		gridSizeSelector.addItem(new GridZoom(-1) {

			@Override
			public String toString() {
				return "Grid disabled";
			}

		});
		for (int i = tileSource.getMaxZoom(); i >= 0; i--) {
			gridSizeSelector.addItem(new GridZoom(i));
		}
	}

	protected void zoomChanged(int oldZoom) {
		updateGridValues();
	}

	protected void setGridZoom(int gridZoom) {
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
			int w = getWidth();
			int h = getHeight();
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

			System.out.println(x_min + " "+y_min);
			int w = x_max - x_min;
			int h = y_max - y_min;
			g.setColor(SEL_COLOR);
			g.fillRect(x_min, y_min, w, h);
			g.setColor(GRID_COLOR);
			g.drawRect(x_min, y_min, w, h);
		}

	}

	protected Point getTopLeftCoordinate() {
		return new Point(center.x - (getWidth() / 2), center.y - (getHeight() / 2));
	}

	public void setSelection(MapSelection ms) {
		if (ms.getLat_max() == ms.getLat_min() || ms.getLon_max() == ms.getLon_min())
			return;
		Point pStart = ms.getTopLeftTileCoordinate(zoom);
		Point pEnd = ms.getBottomRightTileCoordinate(zoom);
		setSelectionByTilePoint(pStart, pEnd, true);
		ArrayList<MapMarker> mml = new ArrayList<MapMarker>(2);
		mml.add(new MapMarkerDot(ms.getLat_max(), ms.getLon_max()));
		mml.add(new MapMarkerDot(ms.getLat_min(), ms.getLon_min()));
		setMapMarkerList(mml);
		setDisplayToFitMapMarkers();
	}

	public void setSelectionByScreenPoint(Point aStart, Point aEnd, boolean notifyListeners) {
		if (aStart == null || aEnd == null)
			return;
		Point p_max = new Point(Math.max(aEnd.x, aStart.x), Math.max(aEnd.y, aStart.y));
		Point p_min = new Point(Math.min(aEnd.x, aStart.x), Math.min(aEnd.y, aStart.y));

		Point tlc = getTopLeftCoordinate();

		Point pEnd = new Point(p_max.x + tlc.x, p_max.y + tlc.y);
		Point pStart = new Point(p_min.x + tlc.x, p_min.y + tlc.y);
		setSelectionByTilePoint(pStart, pEnd, notifyListeners);
	}

	public void setSelectionByTilePoint(Point pStart, Point pEnd, boolean notifyListeners) {

		int zoomDiff = MAX_ZOOM - zoom;

		pEnd.x <<= zoomDiff;
		pEnd.y <<= zoomDiff;
		pStart.x <<= zoomDiff;
		pStart.y <<= zoomDiff;

		if (gridZoom >= 0) {
			int gridZoomDiff = MAX_ZOOM - gridZoom;
			int gridFactor = Tile.SIZE << gridZoomDiff;

			// Snap to the current grid
			pStart.x = pStart.x - (pStart.x % gridFactor);
			pStart.y = pStart.y - (pStart.y % gridFactor);
			pEnd.x += gridFactor - 1;
			pEnd.y += gridFactor - 1;
			pEnd.x = pEnd.x - (pEnd.x % gridFactor);
			pEnd.y = pEnd.y - (pEnd.y % gridFactor);
		}
		iSelectionRectStart = pStart;
		iSelectionRectEnd = pEnd;
		gridSelectionStart = pStart;
		gridSelectionEnd = pEnd;
		if (notifyListeners) {
			updateMapSelection();
		}
		repaint();
	}

	protected void updateMapSelection() {

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
		Point2D.Double max = new Point2D.Double();
		Point2D.Double min = new Point2D.Double();
		max.x = OsmMercator.XToLon(x_max, selectionZoom);
		min.y = OsmMercator.YToLat(y_max, selectionZoom);
		min.x = OsmMercator.XToLon(x_min, selectionZoom);
		max.y = OsmMercator.YToLat(y_min, selectionZoom);
		for (MapSelectionListener msp : mapSelectionListeners) {
			msp.selectionChanged(max, min);
		}
	}

	public void addMapSelectionListener(MapSelectionListener msl) {
		mapSelectionListeners.add(msl);
	}

}
