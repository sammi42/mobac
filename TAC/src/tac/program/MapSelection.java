package tac.program;

import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;

import tac.program.interfaces.MapInterface;
import tac.program.model.EastNorthCoordinate;

public class MapSelection {

	public static final int LAT_MAX = 85;
	public static final int LAT_MIN = -85;
	public static final int LON_MAX = 179;
	public static final int LON_MIN = -179;

	private double lat_max;
	private double lat_min;
	private double lon_max;
	private double lon_min;

	public MapSelection(double lat1, double lat2, double lon1, double lon2) {
		super();
		this.lat_max = Math.max(lat1, lat2);
		this.lat_min = Math.min(lat1, lat2);
		this.lon_max = Math.max(lon1, lon2);
		this.lon_min = Math.min(lon1, lon2);
	}

	public MapSelection(EastNorthCoordinate max, EastNorthCoordinate min) {
		super();
		this.lat_max = max.lat;
		this.lat_min = min.lat;
		this.lon_max = max.lon;
		this.lon_min = min.lon;
	}

	public MapSelection(MapInterface map) {
		super();
		Point min = map.getMinTileCoordinate();
		Point max = map.getMaxTileCoordinate();
		int zoom = map.getZoom();
		this.lat_max = OsmMercator.YToLat(max.y, zoom);
		this.lat_min = OsmMercator.YToLat(min.y, zoom);
		this.lon_max = OsmMercator.XToLon(max.x, zoom);
		this.lon_min = OsmMercator.XToLon(min.x, zoom);
	}

	public double getLat_max() {
		return lat_max;
	}

	public double getLat_min() {
		return lat_min;
	}

	public double getLon_max() {
		return lon_max;
	}

	public double getLon_min() {
		return lon_min;
	}

	public EastNorthCoordinate getMax() {
		return new EastNorthCoordinate(lat_max, lon_max);
	}

	public EastNorthCoordinate getMin() {
		return new EastNorthCoordinate(lat_min, lon_min);
	}

	public boolean coordinatesAreValid() {
		return (!(Double.isNaN(lat_max) || Double.isNaN(lat_min) || Double.isNaN(lon_max) || Double
				.isNaN(lon_min)));
	}

	/**
	 * Returns the top left tile x- and y-tile-number (minimum) of the selected
	 * area marked by the {@link MapSelection}.
	 * 
	 * @param zoom
	 * @return tile number [0..2<sup>zoom</sup>]
	 */
	public Point getTopLeftTileNumber(int zoom) {
		int x = OsmMercator.LonToX(lon_min, zoom) / Tile.SIZE;
		int y = OsmMercator.LatToY(lat_max, zoom) / Tile.SIZE;
		return new Point(x, y);
	}

	/**
	 * Returns the top left tile x- and y-tile-coordinate (minimum) of the
	 * selected area marked by the {@link MapSelection}.
	 * 
	 * @param zoom
	 * @return tile coordinate [0..(256 * 2<sup>zoom</sup>)]
	 */
	public Point getTopLeftTileCoordinate(int zoom) {
		int x = OsmMercator.LonToX(lon_min, zoom);
		int y = OsmMercator.LatToY(lat_max, zoom);
		return new Point(x, y);
	}

	/**
	 * Returns the bottom right tile x- and y-tile-number (minimum) of the
	 * selected area marked by the {@link MapSelection}.
	 * 
	 * @param zoom
	 * @return tile number [0..2<sup>zoom</sup>]
	 */
	public Point getBottomRightTileNumber(int zoom) {
		int x = OsmMercator.LonToX(lon_max, zoom) / Tile.SIZE;
		int y = OsmMercator.LatToY(lat_min, zoom) / Tile.SIZE;
		return new Point(x, y);
	}

	/**
	 * Returns the bottom right tile x- and y-tile-coordinate (minimum) of the
	 * selected area marked by the {@link MapSelection}.
	 * 
	 * @param zoom
	 * @return tile coordinate [0..(256 * 2<sup>zoom</sup>)]
	 */
	public Point getBottomRightTileCoordinate(int zoom) {
		int x = OsmMercator.LonToX(lon_max, zoom);
		int y = OsmMercator.LatToY(lat_min, zoom);
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
		return String.format("lat/lon: max(%6f/%6f) min(%6f/%6f)", new Object[] { lat_max, lon_max,
				lat_min, lon_min });
	}

}
