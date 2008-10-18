package tac.program.model;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class Map {

	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;

	private int zoom;

	private TileSource tileSource;
	private String name;

	public Map(String name, TileSource tileSource, int xMax, int xMin, int yMax, int yMin, int zoom) {
		super();
		this.name = name;
		this.tileSource = tileSource;
		this.xMax = xMax;
		this.xMin = xMin;
		this.yMax = yMax;
		this.yMin = yMin;
		this.zoom = zoom;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public TileSource getTileSource() {
		return tileSource;
	}
	
}
