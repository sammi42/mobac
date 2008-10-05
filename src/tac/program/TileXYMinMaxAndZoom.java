package tac.program;

import java.awt.Point;

public class TileXYMinMaxAndZoom {

	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;
	private int zoom;


	public TileXYMinMaxAndZoom(int theXMin, int theXMax, int theYMin, int theYMax, int theZoom) {

		xMin = theXMin;
		xMax = theXMax;
		yMin = theYMin;
		yMax = theYMax;
		zoom = theZoom;
	}

	public TileXYMinMaxAndZoom(Point theTopLeft, Point theBottomRight, int theZoom) {

		xMax = (int)theTopLeft.getX();
		xMin = (int)theBottomRight.getX();
		yMax = (int)theBottomRight.getY();
		yMin = (int)theTopLeft.getY();
		zoom = theZoom;
	}

	public void setXMin(int theXMin) {
		xMin = theXMin;
	}

	public void setXMax(int theXMax) {
		xMax = theXMax;
	}

	public void setYMin(int theYMin) {
		yMin = theYMin;
	}

	public void setYMax(int theYMax) {
		yMax = theYMax;
	}

	public void setZoom(int theZoom) {
			zoom = theZoom;
	}


	public int getXMin() {
		return xMin;
	}

	public int getXMax() {
		return xMax;
	}

	public int getYMin() {
		return yMin;
	}

	public int getYMax() {
		return yMax;
	}

	public int getZoom() {
		return zoom;
	}
}