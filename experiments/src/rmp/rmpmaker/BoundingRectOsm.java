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
	public BoundingRectOsm(final double x, final double y, final double cx, final double cy,
			final int zoom) {
		super(0, 0, 0, 0);

		setNorth(0 - tile2lat(y, zoom));
		setSouth(0 - tile2lat(y + (cy / 256), zoom));
		setWest(tile2lon(x, zoom));
		setEast(tile2lon(x + (cx / 256), zoom));
	}

	static private double tile2lon(double x, int z) {
		return (x / Math.pow(2.0, z) * 360.0) - 180;
	}

	static private double tile2lat(double y, int z) {
		double n = Math.PI - ((2.0 * Math.PI * y) / Math.pow(2.0, z));
		return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
	}

}
