package tac.program;

public class SubMapProperties {

	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;

	public SubMapProperties(int xMin, int xMax, int yMin, int yMax) {
		super();
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
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

	public void setXMin(int min) {
		xMin = min;
	}

	public void setXMax(int max) {
		xMax = max;
	}

	public void setYMin(int min) {
		yMin = min;
	}

	public void setYMax(int max) {
		yMax = max;
	}

	@Override
	public String toString() {
		return "SubMap x(" + xMin + " to " + xMax + ") y(" + yMin + " to " + yMax + ")";
	}

}
