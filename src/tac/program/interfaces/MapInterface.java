package tac.program.interfaces;

import java.awt.Dimension;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.model.TileImageParameters;

public interface MapInterface {

	public String getName();

	public Point getMinTileCoordinate();

	public Point getMaxTileCoordinate();

	public int getZoom();

	public MapSource getMapSource();

	public Dimension getTileSize();

	public LayerInterface getLayer();

	public TileImageParameters getParameters();

	public long calculateTilesToDownload();
}
