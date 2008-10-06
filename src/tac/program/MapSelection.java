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
	 * @return
	 */
	public Point getTopLeftTile(int zoom) {
		int x = OsmMercator.LonToX(lon_min, zoom) / Tile.SIZE;
		int y = OsmMercator.LatToY(lat_max, zoom) / Tile.SIZE;
		return new Point(x, y);
	}

	/**
	 * Returns the bottom right tile x- and y-tile-number (minimum) of the
	 * selected area marked by the {@link MapSelection}.
	 * 
	 * @param zoom
	 * @return
	 */
	public Point getBottomRightTile(int zoom) {
		int x = (OsmMercator.LonToX(lon_max, zoom) + Tile.SIZE + 1) / Tile.SIZE;
		int y = (OsmMercator.LatToY(lat_min, zoom) + Tile.SIZE + 1) / Tile.SIZE;
		return new Point(x, y);
	}

	public long calculateNrOfTiles(int zoom) {
		Point min = getTopLeftTile(zoom);
		Point max = getBottomRightTile(zoom);
		long width = max.x - min.x;
		long height = max.y - min.y;
		return width * height;
	}
}
