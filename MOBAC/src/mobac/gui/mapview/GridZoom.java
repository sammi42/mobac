package mobac.gui.mapview;

public class GridZoom {

	private int zoom;

	public int getZoom() {
		return zoom;
	}

	public GridZoom(int zoom) {
		this.zoom = zoom;
	}

	@Override
	public String toString() {
		return "Grid zoom " + zoom;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GridZoom))
			return false;
		return ((GridZoom) obj).zoom == zoom;
	}

	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return -1;
	}

}
