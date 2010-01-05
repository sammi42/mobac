package tac.program.atlascreators;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.MapCreationException;
import tac.gui.AtlasProgress;
import tac.mapsources.MultiLayerMapSource;
import tac.program.AtlasThread;
import tac.program.PauseResumeHandler;
import tac.program.atlascreators.tileprovider.DownloadedTileProvider;
import tac.program.atlascreators.tileprovider.MultiLayerTileProvider;
import tac.program.atlascreators.tileprovider.TileProvider;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.program.model.AtlasOutputFormat;
import tac.program.model.Settings;
import tac.program.model.TileImageParameters;
import tac.utilities.Utilities;
import tac.utilities.tar.TarIndex;

/**
 * Abstract base class for all AtlasCreator implementations.
 * 
 * The general call schema is as follows:
 * <ol>
 * <li>AtlasCreator instantiation via
 * {@link AtlasOutputFormat#createAtlasCreatorInstance()}</li>
 * <li>AtlasCreator atlas initialization via
 * {@link #startAtlasCreation(AtlasInterface)}</li>
 * <li>1 to n times {@link #initializeMap(MapInterface, TarIndex)} followed by
 * {@link #createMap()}</li>
 * <li>AtlasCreator atlas finalization via {@link #finishAtlasCreation()}</li>
 * </ol>
 */
public abstract class AtlasCreator {

	public static final String TEXT_FILE_CHARSET = "ISO-8859-1";

	protected final Logger log;

	/************************************************************/
	/** atlas specific fields **/
	/************************************************************/

	protected AtlasInterface atlas;

	protected File atlasDir;

	protected AtlasProgress atlasProgress = null;

	protected PauseResumeHandler pauseResumeHandler = null;

	/************************************************************/
	/** map specific fields **/
	/************************************************************/

	protected MapInterface map;
	protected int xMin;
	protected int xMax;
	protected int yMin;
	protected int yMax;

	protected TarIndex tarTileIndex;
	protected int zoom;
	protected AtlasOutputFormat atlasOutputFormat;
	protected MapSource mapSource;
	protected int tileSize;
	protected TileImageParameters parameters;

	protected TileProvider mapDlTileProvider;

	private boolean aborted = false;

	/**
	 * Default constructor - initializes the logging environment
	 */
	protected AtlasCreator() {
		log = Logger.getLogger(this.getClass());
	};

	/**
	 * @see AtlasCreator
	 */
	public void startAtlasCreation(AtlasInterface atlas) throws IOException {
		this.atlas = atlas;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String atlasDirName = atlas.getName() + "_" + sdf.format(new Date());
		File atlasOutputDir = Settings.getInstance().getAtlasOutputDirectory();

		atlasDir = new File(atlasOutputDir, atlasDirName);
		Utilities.mkDirs(atlasDir);
	}

	public void initLayerCreation(LayerInterface layer) throws IOException {

	}

	public void finishLayerCreation() throws IOException {

	}

	/**
	 * @see AtlasCreator
	 */
	public void finishAtlasCreation() throws IOException {
	}

	public void abortAtlasCreation() throws IOException {
		this.aborted = true;
	}

	public boolean isAborted() {
		return aborted;
	}

	/**
	 * Test if the {@link AtlasCreator} instance supports the selected
	 * {@link MapSource}
	 * 
	 * @param mapSource
	 * @return <code>true</code> if supported otherwise <code>false</code>
	 * @see AtlasCreator
	 */
	public abstract boolean testMapSource(MapSource mapSource);

	/**
	 * @see AtlasCreator
	 */
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
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
		mapDlTileProvider = new DownloadedTileProvider(tarTileIndex, mapSource);
		if (map.getMapSource() instanceof MultiLayerMapSource)
			mapDlTileProvider = new MultiLayerTileProvider(map.getMapSource(), mapDlTileProvider, 2);
		Thread t = Thread.currentThread();
		if (!(t instanceof AtlasThread))
			throw new RuntimeException("Calling thread must be AtlasThread!");
		AtlasThread at = (AtlasThread) t;
		atlasProgress = at.getAtlasProgress();
		pauseResumeHandler = at.getPauseResumeHandler();
	}

	/**
	 * @see AtlasCreator
	 */
	public abstract void createMap() throws MapCreationException;

	/**
	 * Usually called within {@link #createMap()}.
	 * 
	 * @throws InterruptedException
	 * @throws MapCreationException
	 */
	protected abstract void createTiles() throws InterruptedException, MapCreationException;

	/**
	 * Checks if the user has aborted atlas creation and if <code>true</code> an
	 * {@link InterruptedException} is thrown.
	 * 
	 * @throws InterruptedException
	 */
	protected void checkUserAbort() throws InterruptedException {
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException();
		pauseResumeHandler.pauseWait();
	}

}