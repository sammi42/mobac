package org.openstreetmap.gui.jmapviewer.interfaces;

import tac.mapsources.mapscale.Power2MapScale;

/**
 * Preparation for supporting map resolutions other than those uses by
 * Google/OpenstreetMap.
 * 
 * {@link Power2MapScale} is the only implementation that is currently supported
 * by TrekBuddy Atlas Creator.
 * <p>
 * DO NOT TRY TO IMPLEMENT YOUR OWN. IT WILL NOT WORK!
 * </p>
 */
public interface MapScale {

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

}
