package moller.preview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.JComboBox;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class PreviewMap extends JMapViewer {
	private static final long serialVersionUID = 1L;
	private static final Color GRID_COLOR = new Color(200, 20, 20, 130);
	private static final Color SEL_COLOR = new Color(0.9f, 0.7f, 0.7f, 0.6f);

	private Point iSelectionRectStart;
	private Point iSelectionRectEnd;
	private Point gridSelectionStart;
	private Point gridSelectionEnd;

	private BufferedImage gridTile = new BufferedImage(256, 256,
			BufferedImage.TYPE_INT_ARGB);

	private int gridZoom = 10;
	private int gridFactor;
	private int gridSize;

	private JComboBox gridSizeSelector;

	private LinkedList<MapSelectionListener> mapSelectionListeners = new LinkedList<MapSelectionListener>();

	public PreviewMap() {
		super();
		gridSizeSelector = new JComboBox();
		gridSizeSelector.setEditable(false);
		gridSizeSelector.setBounds(40, 10, 80, 20);
		gridSizeSelector.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				GridZoom g = (GridZoom)gridSizeSelector.getSelectedItem();
				setGridZoom(g.getZoom());
				repaint();
			}
			
		});
		add(gridSizeSelector);
		new PreviewMapController(this);
		setTileSource(new GoogleTileSource.GoogleMaps());
	}

	@Override
	public void setTileSource(TileSource arg0) {
		super.setTileSource(arg0);
		gridSizeSelector.removeAllItems();
		gridSizeSelector.setMaximumRowCount(tileSource.getMaxZoom()+1);
		for (int i=0; i<tileSource.getMaxZoom(); i++) {
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
		int gridZoomDiff = MAX_ZOOM - gridZoom;
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
		int zoomToGridZoom = zoom - gridZoom;
		if (zoomToGridZoom > 0) {
			gridSize = 256 << zoomToGridZoom;
		} else {
			gridSize = 256 >> (-zoomToGridZoom);
			BufferedImage newGridTile = new BufferedImage(256, 256,
					BufferedImage.TYPE_INT_ARGB);
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
		super.paintComponent(g);
		Point tlc = getTopLeftCoordinate();

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
		if (gridSelectionStart != null && gridSelectionEnd != null) {
			int zoomDiff = MAX_ZOOM - zoom;
			int x_min = (gridSelectionStart.x >> zoomDiff) - tlc.x;
			int y_min = (gridSelectionStart.y >> zoomDiff) - tlc.y;
			int x_max = (gridSelectionEnd.x >> zoomDiff) - tlc.x;
			int y_max = (gridSelectionEnd.y >> zoomDiff) - tlc.y;

			w = x_max - x_min;
			h = y_max - y_min;
			g.setColor(SEL_COLOR);
			g.fillRect(x_min, y_min, w, h);
			g.setColor(GRID_COLOR);
			g.drawRect(x_min, y_min, w, h);
		}

	}

	protected Point getTopLeftCoordinate() {
		return new Point(center.x - (getWidth() / 2), center.y
				- (getHeight() / 2));
	}

	public void setSelection(Point aStart, Point aEnd,
			boolean selectionInProgress) {
		if (aStart == null || aEnd == null)
			return;
		Point p_max = new Point(Math.max(aEnd.x, aStart.x), Math.max(aEnd.y,
				aStart.y));
		Point p_min = new Point(Math.min(aEnd.x, aStart.x), Math.min(aEnd.y,
				aStart.y));

		Point tlc = getTopLeftCoordinate();

		Point pEnd = new Point(p_max.x + tlc.x, p_max.y + tlc.y);
		Point pStart = new Point(p_min.x + tlc.x, p_min.y + tlc.y);

		int zoomDiff = MAX_ZOOM - zoom;

		pEnd.x <<= zoomDiff;
		pEnd.y <<= zoomDiff;
		pStart.x <<= zoomDiff;
		pStart.y <<= zoomDiff;

		int gridZoomDiff = MAX_ZOOM - gridZoom;
		int gridFactor = Tile.SIZE << gridZoomDiff;

		// Snap to the current grid
		pStart.x = pStart.x - (pStart.x % gridFactor);
		pStart.y = pStart.y - (pStart.y % gridFactor);
		pEnd.x += gridFactor - 1;
		pEnd.y += gridFactor - 1;
		pEnd.x = pEnd.x - (pEnd.x % gridFactor);
		pEnd.y = pEnd.y - (pEnd.y % gridFactor);

		iSelectionRectStart = pStart;
		iSelectionRectEnd = pEnd;
		gridSelectionStart = pStart;
		gridSelectionEnd = pEnd;
		if (!selectionInProgress) {
			Point2D.Double max = new Point2D.Double();
			Point2D.Double min = new Point2D.Double();

			int zoomDiff1 = PreviewMap.MAX_ZOOM - gridZoom;
			if (iSelectionRectStart == null || iSelectionRectEnd == null)
				return;
			int x_min = (iSelectionRectStart.x >> zoomDiff1);
			int y_min = (iSelectionRectStart.y >> zoomDiff1);
			int x_max = (iSelectionRectEnd.x >> zoomDiff1);
			int y_max = (iSelectionRectEnd.y >> zoomDiff1);
			// x_min = x_min >> 8 << 8;
			// y_min = y_min >> 8 << 8;
			// x_max = (x_max + 255) >> 8 << 8;
			// y_max = (y_max + 255) >> 8 << 8;

			max.x = OsmMercator.XToLon(x_max, gridZoom);
			max.y = OsmMercator.YToLat(y_max, gridZoom);
			min.x = OsmMercator.XToLon(x_min, gridZoom);
			min.y = OsmMercator.YToLat(y_min, gridZoom);
			for (MapSelectionListener msp : mapSelectionListeners) {
				msp.selectionChanged(max, min);
			}
		}
		repaint();
	}

	public void addMapSelectionListener(MapSelectionListener msl) {
		mapSelectionListeners.add(msl);
	}
}
