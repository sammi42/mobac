/* ------------------------------------------------------------------------

   BoundingBox.java

   Project: JpgToRmp

  --------------------------------------------------------------------------*/

/* ---
 created: 17.07.2009 a.sander

 $History:$
 --- */

package rmp.rmpmaker;

/**
 * Coordinates of a bounding box around osm tiles
 * 
 */
public class BoundingRectOsm extends BoundingRect {
	public BoundingRectOsm(final int x, final int y, final int cx, final int cy, final int zoom) {
		super(0, 0, 0, 0);

		setNorth(-tile2lat(y, zoom));
		setSouth(-tile2lat(y + (cy / 256.0), zoom));
		setWest(tile2lon(x, zoom));
		setEast(tile2lon(x + (cx / 256.0), zoom));
	}

	static private double tile2lon(double x, int z) {
		return (x / Math.pow(2.0, z) * 360.0) - 180;
	}

	static private double tile2lat(double y, int z) {
		// This computation is really strange - OSM uses Mercator projection but
		// this definitely not a transformation MercatorXY to Lat/Lon!?
		double n = Math.PI - ((2.0 * Math.PI * y) / Math.pow(2.0, z));
		return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
	}

}
