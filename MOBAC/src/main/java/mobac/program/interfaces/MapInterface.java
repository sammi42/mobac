package mobac.program.interfaces;

import java.awt.Dimension;
import java.awt.Point;

import mobac.program.model.TileImageParameters;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


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
