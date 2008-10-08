package tac.program;

import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;

public class MapSelection {

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
		int x = (OsmMercator.LonToX(lon_max, zoom) + Tile.SIZE - 1) / Tile.SIZE;
		int y = (OsmMercator.LatToY(lat_min, zoom) + Tile.SIZE - 1) / Tile.SIZE;
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

	public long calculateNrOfTiles(int zoom) {
		Point min = getTopLeftTileNumber(zoom);
		Point max = getBottomRightTileNumber(zoom);
		long width = max.x - min.x;
		long height = max.y - min.y;
		return width * height;
	}
}
