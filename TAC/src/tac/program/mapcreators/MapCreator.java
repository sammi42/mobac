package tac.program.mapcreators;

import java.io.File;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.MapCreationException;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.program.model.AtlasOutputFormat;
import tac.tar.TarIndex;

/**
 * Abstract base class for all MapCreator implementations.
 */
public abstract class MapCreator {

	public static final String TEXT_FILE_CHARSET = "ISO-8859-1";

	protected final Logger log;

	protected final MapInterface map;
	protected final int xMin;
	protected final int xMax;
	protected final int yMin;
	protected final int yMax;

	protected final TarIndex tarTileIndex;
	protected final int zoom;
	protected final AtlasOutputFormat atlasOutputFormat;
	protected final MapSource mapSource;
	protected final int tileSize;

	protected MapTileWriter mapTileWriter;

	public MapCreator(MapInterface map, TarIndex tarTileIndex, File atlasDir) {
		log = Logger.getLogger(this.getClass());
		LayerInterface layer = map.getLayer();
		this.map = map;
		this.mapSource = map.getMapSource();
		this.tileSize = mapSource.getMapSpace().getTileSize();
		xMin = map.getMinTileCoordinate().x / tileSize;
		xMax = map.getMaxTileCoordinate().x / tileSize;
		yMin = map.getMinTileCoordinate().y / tileSize;
		yMax = map.getMaxTileCoordinate().y / tileSize;
		this.tarTileIndex = tarTileIndex;
		this.zoom = map.getZoom();
		this.atlasOutputFormat = layer.getAtlas().getOutputFormat();
	}

	public abstract void createMap() throws MapCreationException;

	protected abstract void createTiles() throws InterruptedException, MapCreationException;

}