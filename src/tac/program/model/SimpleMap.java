package tac.program.model;

import java.awt.Dimension;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.program.interfaces.CapabilityDeletable;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;

public class SimpleMap implements MapInterface, CapabilityDeletable {

	private SimpleLayer layer;

	private String name;
	private TileSource mapSource;
	private Point maxTileNum;
	private Point minTileNum;
	private int zoom;
	private Dimension tileSize;

	public SimpleMap(SimpleLayer layer, String name, TileSource mapSource, Point maxTileNum, Point minTileNum,
			int zoom, Dimension tileSize) {
		super();
		this.layer = layer;
		this.mapSource = mapSource;
		this.maxTileNum = maxTileNum;
		this.minTileNum = minTileNum;
		this.name = name;
		this.tileSize = tileSize;
		this.zoom = zoom;
	}

	public void delete() {
		layer.deleteMap(this);
	}

	public LayerInterface getLayer() {
		return layer;
	}

	public TileSource getMapSource() {
		return mapSource;
	}

	public Point getMaxTileCoordinate() {
		return maxTileNum;
	}

	public Point getMinTileCoordinate() {
		return minTileNum;
	}

	public String getName() {
		return name;
	}

	public Dimension getTileSize() {
		return tileSize;
	}

	public int getZoom() {
		return zoom;
	}

	@Override
	public String toString() {
		return getName();
	}

}
