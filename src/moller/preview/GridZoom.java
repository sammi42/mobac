package moller.preview;

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
		return "Zoom " + zoom;
	}

}
