/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package mobac.program.atlascreators.impl.rmp;

/**
 * Rectangle made from latitude/longitude coordinates. Negative latitude is to
 * the north and negative longitude is to the west.
 * 
 * @author Andreas Sander
 * 
 */
public class BoundingRect {
	private double north;
	private double south;
	private double west;
	private double east;

	/**
	 * Constructor
	 */
	public BoundingRect(double north, double south, double west, double east) {
		this.north = north;
		this.south = south;
		this.west = west;
		this.east = east;
	}

	public double getNorth() {
		return north;
	}

	public void setNorth(double north) {
		this.north = north;
	}

	public double getSouth() {
		return south;
	}

	public void setSouth(double south) {
		this.south = south;
	}

	public double getWest() {
		return west;
	}

	public void setWest(double west) {
		this.west = west;
	}

	public double getEast() {
		return east;
	}

	public void setEast(double east) {
		this.east = east;
	}

	@Override
	public String toString() {
		return String.format("BoundingRect [N=%2.4f, S==%2.4f, W=%2.4f, E=%2.4f]", north, south,
				west, east);
	}

}
