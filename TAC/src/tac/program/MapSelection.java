package tac.program;

import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;

import tac.program.interfaces.MapInterface;
import tac.program.model.EastNorthCoordinate;
import tac.program.model.MercatorPixelCoordinate;

public class MapSelection {

	public static final int LAT_MAX = 85;
	public static final int LAT_MIN = -85;
	public static final int LON_MAX = 179;
	public static final int LON_MIN = -179;

	private int zoom;
	private int minTileCoordinate_x;
	private int minTileCoordinate_y;
	private int maxTileCoordinate_x;
	private int maxTileCoordinate_y;

	public MapSelection(EastNorthCoordinate max, EastNorthCoordinate min) {
		super();
		zoom = JMapViewer.MAX_ZOOM;
		minTileCoordinate_x = OsmMercator.LonToX(min.lon, zoom);
		maxTileCoordinate_x = OsmMercator.LonToX(max.lon, zoom);
		minTileCoordinate_y = OsmMercator.LatToY(min.lat, zoom);
		maxTileCoordinate_y = OsmMercator.LatToY(max.lat, zoom);
	}

	public MapSelection(MapInterface map) {
		this(map.getMaxTileCoordinate(), map.getMinTileCoordinate(), map.getZoom());
	}

	/**
	 * 
	 * @param max
	 *            tile coordinate
	 * @param min
	 *            tile coordinate
	 * @param zoom
	 */
	public MapSelection(Point max, Point min, int zoom) {
		super();
		minTileCoordinate_x = min.x;
		minTileCoordinate_y = min.y;
		maxTileCoordinate_x = max.x;
		maxTileCoordinate_y = max.y;
		this.zoom = zoom;
	}

	public MapSelection(MercatorPixelCoordinate mapSelectionMax,
			MercatorPixelCoordinate mapSelectionMin) {
		if (mapSelectionMax.getZoom() != mapSelectionMin.getZoom())
			throw new RuntimeException("Different zoom levels - unsuported!");
		this.zoom = mapSelectionMax.getZoom();
		maxTileCoordinate_x = mapSelectionMax.getX();
		maxTileCoordinate_y = mapSelectionMax.getY();
		minTileCoordinate_x = mapSelectionMin.getX();
		minTileCoordinate_y = mapSelectionMin.getY();
	}

	/**
	 * Is an area selected or only one point?
	 * 
	 * @return
	 */
	public boolean isAreaSelected() {
		boolean result = maxTileCoordinate_x != minTileCoordinate_x
				&& maxTileCoordinate_y != minTileCoordinate_y;
		return result;
	}

	public EastNorthCoordinate getMax() {
		return new EastNorthCoordinate(zoom, maxTileCoordinate_x, maxTileCoordinate_y);
	}

	public EastNorthCoordinate getMin() {
		return new EastNorthCoordinate(zoom, minTileCoordinate_x, minTileCoordinate_y);
	}

	/**
	 * Returns the top left tile x- and y-tile-number (minimum) of the selected
	 * area marked by the {@link MapSelection}.
	 * 
	 * @param aZoomLevel
	 * @return tile number [0..2<sup>zoom</sup>]
	 */
	public Point getTopLeftTileNumber(int aZoomlevel) {
		Point tlc = getTopLeftPixelCoordinate(aZoomlevel);
		tlc.x /= Tile.SIZE;
		tlc.y /= Tile.SIZE;
		return tlc;
	}

	public MercatorPixelCoordinate getTopLeftPixelCoordinate() {
		return new MercatorPixelCoordinate(minTileCoordinate_x, maxTileCoordinate_y, zoom);
	}

	/**
	 * Returns the top left tile x- and y-tile-coordinate (minimum) of the
	 * selected area marked by the {@link MapSelection}.
	 * 
	 * @param aZoomlevel
	 * @return tile coordinate [0..(256 * 2<sup>zoom</sup>)]
	 */
	public Point getTopLeftPixelCoordinate(int aZoomlevel) {
		int zoomDiff = this.zoom - aZoomlevel;
		int x = minTileCoordinate_x;
		int y = maxTileCoordinate_y;
		if (zoomDiff < 0) {
			zoomDiff = -zoomDiff;
			x <<= zoomDiff;
			y <<= zoomDiff;
		} else {
			x >>= zoomDiff;
			y >>= zoomDiff;
		}
		return new Point(x, y);
	}

	/**
	 * Returns the bottom right tile x- and y-tile-number (minimum) of the
	 * selected area marked by the {@link MapSelection}.
	 * 
	 * @param aZoomlevel
	 * @return tile number [0..2<sup>zoom</sup>]
	 */
	public Point getBottomRightTileNumber(int aZoomlevel) {
		Point brc = getBottomRightPixelCoordinate(aZoomlevel);
		brc.x /= Tile.SIZE;
		brc.y /= Tile.SIZE;
		return brc;
	}

	public MercatorPixelCoordinate getBottomRightPixelCoordinate() {
		return new MercatorPixelCoordinate(maxTileCoordinate_x, minTileCoordinate_y, zoom);
	}

	/**
	 * Returns the bottom right tile x- and y-tile-coordinate (minimum) of the
	 * selected area marked by the {@link MapSelection}.
	 * 
	 * @param aZoomlevel
	 * @return tile coordinate [0..(256 * 2<sup>zoom</sup>)]
	 */
	public Point getBottomRightPixelCoordinate(int aZoomlevel) {
		int zoomDiff = this.zoom - aZoomlevel;
		int x = maxTileCoordinate_x;
		int y = minTileCoordinate_y;
		if (zoomDiff < 0) {
			zoomDiff = -zoomDiff;
			x <<= zoomDiff;
			y <<= zoomDiff;
		} else {
			x >>= zoomDiff;
			y >>= zoomDiff;
		}
		return new Point(x, y);
	}

	/**
	 * Return the amount of tiles for the current selection in the specified
	 * zoom level.
	 * 
	 * @param zoom
	 *            is the zoom level to calculate the amount of tiles for
	 * @return the amount of tiles in the current selection in the supplied zoom
	 *         level
	 */
	public long calculateNrOfTiles(int zoom) {
		Point max = getBottomRightTileNumber(zoom);
		Point min = getTopLeftTileNumber(zoom);
		long width = max.x - min.x + 1;
		long height = max.y - min.y + 1;
		return width * height;
	}

	public long[] calculateNrOfTilesEx(int zoom) {
		Point max = getBottomRightTileNumber(zoom);
		Point min = getTopLeftTileNumber(zoom);
		long width = max.x - min.x + 1;
		long height = max.y - min.y + 1;
		return new long[] { width * height, width, height };
	}

	@Override
	public String toString() {
		EastNorthCoordinate max = getMax();
		EastNorthCoordinate min = getMin();
		return String.format("lat/lon: max(%6f/%6f) min(%6f/%6f)", new Object[] { max.lat, max.lon,
				min.lat, min.lon });
	}

}
