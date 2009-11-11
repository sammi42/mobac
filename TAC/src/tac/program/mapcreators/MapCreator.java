package tac.program.mapcreators;

import java.io.File;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.MapCreationException;
import tac.gui.AtlasProgress;
import tac.program.AtlasThread;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.program.model.AtlasOutputFormat;
import tac.program.model.TileImageParameters;
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
	protected final TileImageParameters parameters;
	
	protected RawTileProvider mapDlTileProvider;
	protected MapTileWriter mapTileWriter;

	protected AtlasProgress atlasProgress = null;

	public MapCreator(MapInterface map, TarIndex tarTileIndex, File atlasDir) {
		log = Logger.getLogger(this.getClass());
		LayerInterface layer = map.getLayer();
		this.map = map;
		this.mapSource = map.getMapSource();
		this.tileSize = mapSource.getMapSpace().getTileSize();
		this.parameters = map.getParameters();
		xMin = map.getMinTileCoordinate().x / tileSize;
		xMax = map.getMaxTileCoordinate().x / tileSize;
		yMin = map.getMinTileCoordinate().y / tileSize;
		yMax = map.getMaxTileCoordinate().y / tileSize;
		this.tarTileIndex = tarTileIndex;
		this.zoom = map.getZoom();
		this.atlasOutputFormat = layer.getAtlas().getOutputFormat();
		mapDlTileProvider = new MapDownloadedTileProcessor(tarTileIndex, mapSource);
		Thread t = Thread.currentThread();
		if (!(t instanceof AtlasThread))
			throw new RuntimeException("Calling thread must be AtlasThread!");
		atlasProgress = ((AtlasThread) t).getAtlasProgress();
	}

	/**
	 * Checks if the user has aborted atlas creation and if <code>true</code> an
	 * {@link InterruptedException} is thrown.
	 * 
	 * @throws InterruptedException
	 */
	protected void checkUserAbort() throws InterruptedException {
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException();
	}

	public abstract void createMap() throws MapCreationException;

	protected abstract void createTiles() throws InterruptedException, MapCreationException;

}