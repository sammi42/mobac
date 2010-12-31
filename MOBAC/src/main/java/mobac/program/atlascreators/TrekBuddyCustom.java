/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.program.atlascreators;

import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.impl.MapTileBuilder;
import mobac.program.atlascreators.tileprovider.CacheTileProvider;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.program.model.AtlasOutputFormat;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;


/**
 * Extends the {@link TrekBuddy} so that custom tiles are written. Custom tiles
 * can have a size different of 255x255 pixels), a different color depth and a
 * different image type (jpg/png).
 * 
 * @author r_x
 */
@SupportedParameters(names = { Name.format })
public class TrekBuddyCustom extends TrekBuddy {

	@Override
	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace && ProjectionCategory.SPHERE
				.equals(mapSpace.getProjectionCategory()));
		// TODO supports Mercator ellipsoid?
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
				createCustomTiles();
			else
				createTiles();

			mapTileWriter.finalizeMap();
		} catch (MapCreationException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(map, e);
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
	protected void createCustomTiles() throws InterruptedException, MapCreationException {
		log.debug("Starting map creation using custom parameters: " + parameters);

		CacheTileProvider ctp = new CacheTileProvider(mapDlTileProvider);
		try {
			mapDlTileProvider = ctp;

			MapTileBuilder mapTileBuilder = new MapTileBuilder(this, mapTileWriter, true);
			atlasProgress.initMapCreation(mapTileBuilder.getCustomTileCount());
			mapTileBuilder.createTiles();
		} finally {
			ctp.cleanup();
		}
	}

}
