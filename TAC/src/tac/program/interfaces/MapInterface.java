package tac.program.interfaces;

import java.awt.Dimension;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.model.TileImageParameters;

public interface MapInterface extends AtlasObject, CapabilityDeletable {

	public Point getMinTileCoordinate();

	public Point getMaxTileCoordinate();

	public int getZoom();

	public MapSource getMapSource();

	public Dimension getTileSize();

	public LayerInterface getLayer();

	public void setLayer(LayerInterface layer);

	public TileImageParameters getParameters();

	public void setParameters(TileImageParameters p);

	public int calculateTilesToDownload();

	public MapInterface deepClone(LayerInterface newLayer);
}
