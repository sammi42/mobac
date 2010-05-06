package org.openstreetmap.gui.jmapviewer.interfaces;

import java.awt.Point;

import mobac.mapsources.mapspace.MercatorPower2MapSpace;

/**
 * Preparation for supporting map resolutions other than those uses by
 * Google/OpenstreetMap.
 * 
 * {@link MercatorPower2MapSpace} is the only implementation that is currently
 * supported by Mobile Atlas Creator.
 * <p>
 * DO NOT TRY TO IMPLEMENT YOUR OWN. IT WILL NOT WORK!
 * </p>
 */
public interface MapSpace {

	public enum ProjectionCategory {
		SPHERE, ELLIPSOID, GEOID
	};

	public ProjectionCategory getProjectionCategory();

	public int getMaxPixels(int zoom);

	/**
	 * @return size (height and width) of each tile in pixel
	 */
	public int getTileSize();

	/**
	 * Converts the horizontal pixel coordinate from map space to longitude.
	 * 
	 * @param lon
	 * @param zoom
	 * @return
	 */
	public int cLonToX(double lon, int zoom);

	/**
	 * Converts the vertical pixel coordinate from map space to latitude.
	 * 
	 * @param lat
	 * @param zoom
	 * @return
	 */
	public int cLatToY(double lat, int zoom);

	/**
	 * Converts longitude to the horizontal pixel coordinate from map space.
	 * 
	 * @param x
	 * @param zoom
	 * @return
	 */
	public double cXToLon(int x, int zoom);

	/**
	 * Converts latitude to the vertical pixel coordinate from map space.
	 * 
	 * @param y
	 * @param zoom
	 * @return
	 */
	public double cYToLat(int y, int zoom);

	/**
	 * "Walks" westerly a certain distance on a latitude and returns the
	 * "mileage" in map space pixels. The distance is specified as angular
	 * distance, therefore this method works with all length unit systems (e.g.
	 * metric, imperial, ...).
	 * 
	 * @param startX
	 *            x-coordinate of start point
	 * @param y
	 *            y-coordinate specifying the latitude to "walk" on
	 * @param zoom
	 * @param angularDist
	 *            angular distance: distance / earth radius (e.g. 6367.5km or
	 *            3956.6miles)
	 * @return "mileage" in number of pixels
	 */
	public int moveOnLatitude(int startX, int y, int zoom, double angularDist);

	/**
	 * Calculates the distance between two points that are laying on the same
	 * latitude / y-coordinate.
	 * 
	 * @param zoom
	 * @param y
	 *            y-coordinate specifying the latitude
	 * @param xDist
	 *            distance in pixels on the x-axis
	 * @return angular distance angular distance: distance / earth radius (e.g.
	 *         6367.5km or 3956.6miles)
	 */
	public double horizontalDistance(int zoom, int y, int xDist);

	public Point changeZoom(Point pixelCoordinate, int oldZoom, int newZoom);

	public int xChangeZoom(int x, int oldZoom, int newZoom);

	public int yChangeZoom(int y, int oldZoom, int newZoom);

}
