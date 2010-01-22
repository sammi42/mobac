package mobac.program.atlascreators;

import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.atlascreators.impl.MapTileBuilder;
import mobac.program.atlascreators.tileprovider.CacheTileProvider;
import mobac.program.model.AtlasOutputFormat;
import mobac.utilities.Utilities;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


/**
 * Extends the {@link TrekBuddy} so that custom tiles are written. Custom tiles
 * can have a size different of 255x255 pixels), a different color depth and a
 * different image type (jpg/png).
 * 
 * @author r_x
 */
public class TrekBuddyCustom extends TrekBuddy {

	@Override
	public boolean testMapSource(MapSource mapSource) {
		// if (mapSource instanceof MultiLayerMapSource)
		// return false;
		return (mapSource.getMapSpace() instanceof MercatorPower2MapSpace);
	}

	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDirs(mapFolder);

			// write the .map file containing the calibration points
			writeMapFile();

			// This means there should not be any resizing of the tiles.
			if (atlasOutputFormat == AtlasOutputFormat.TaredAtlas)
				mapTileWriter = new TarTileWriter();
			else
				mapTileWriter = new FileTileWriter();

			// Select the tile creator instance based on whether tile image
			// parameters has been set or not
			if (parameters != null)
				createTiles();
			else
				super.createTiles();

			mapTileWriter.finalizeMap();
		} catch (MapCreationException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(e);
		}
	}

	/**
	 * New experimental custom tile size algorithm implementation.
	 * 
	 * It creates each custom sized tile separately. Therefore each original
	 * tile (256x256) will be loaded and painted multiple times. Therefore this
	 * implementation needs much more CPU power as each original tile is loaded
	 * at least once and each generated tile has to be saved.
	 * 
	 * @throws MapCreationException
	 */
	@Override
	protected void createTiles() throws InterruptedException, MapCreationException {
		log.debug("Starting map creation using custom parameters: " + parameters);
		
		mapDlTileProvider = new CacheTileProvider(mapDlTileProvider);

		MapTileBuilder mapTileBuilder = new MapTileBuilder(this, mapTileWriter, true);
		atlasProgress.initMapCreation(mapTileBuilder.getCustomTileCount());
		mapTileBuilder.createTiles();
	}

}