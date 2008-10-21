package tac.program;

import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;

public class MapSelection {

	public static final int LAT_MAX = 85;
	public static final int LAT_MIN = -85;
	public static final int LON_MAX = 179;
	public static final int LON_MIN = -179;

	private double lat_max;
	private double lat_min;
	private double lon_max;
	private double lon_min;

	public MapSelection(double lat_max, double lat_min, double lon_max, double lon_min) {
		super();
		this.lat_max = lat_max;
		this.lat_min = lat_min;
		this.lon_max = lon_max;
		this.lon_min = lon_min;
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

	@Override
	public String toString() {
		return String.format("lat/lon: max(%6f/%6f) min(%6f/%6f)", new Object[] { lat_max, lon_max,
				lat_min, lon_min });
	}

}
