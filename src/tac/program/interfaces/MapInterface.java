package tac.program.interfaces;

import java.awt.Dimension;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public interface MapInterface {

	public String getName();

	public Point getMinTileCoordinate();

	public Point getMaxTileCoordinate();

	public int getZoom();

	public TileSource getMapSource();
	
	public Dimension getTileSize();
	
	public LayerInterface getLayer();
}
