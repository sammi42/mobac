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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;

@AtlasCreatorName("Geocaching Live offline map")
@SupportedParameters(names = { Name.format })
public class GCLive extends AtlasCreator {

	private MapTileWriter mapTileWriter = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		super.testAtlas();
		// Check for max tile count <= 65535
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		if (parameters != null) {
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, parameters.getFormat());
		}
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		// This means there should not be any resizing of the tiles.
		try {
			mapTileWriter = new GCLiveWriter(new File(atlasDir, map.getName()));
			createTiles();
			mapTileWriter.finalizeMap();
			mapTileWriter = null;
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		byte[] emptyTileData = Utilities.createEmptyTileData(mapSource);

		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null)
						mapTileWriter.writeTile(x, y, null, sourceTileData);
					else
						mapTileWriter.writeTile(x, y, null, emptyTileData);
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
				}
			}
		}
	}

	/**
	 * http://palmtopia.de/trac/GCLiveMapGen/browser/Reengineering%20of%20the%20Geocaching%20Live%20Tile%20Database.txt?
	 * format=txt
	 */
	protected class GCLiveWriter implements MapTileWriter {

		private File mapDir;

		private RandomAccessFile indexFile;

		private int dataDirCounter = 0;
		private int dataFileCounter = 0;
		private int imageCounter = 0;

		private int tileEntries = 0;

		private RandomAccessFile currentDataFile;

		public GCLiveWriter(File mapDir) throws IOException {
			super();
			this.mapDir = mapDir;
			Utilities.mkDir(mapDir);
			indexFile = new RandomAccessFile(new File(mapDir, "index"), "rw");
			indexFile.seek(16); // skip header - we write it later
			prepareDataFile();
		}

		private void prepareDataFile() throws IOException {
			if (currentDataFile != null)
				Utilities.closeFile(currentDataFile);
			currentDataFile = null;
			File dataDir = new File(mapDir, Integer.toString(dataDirCounter));
			Utilities.mkDir(dataDir);
			File dataFile = new File(dataDir, "data" + Integer.toString(dataFileCounter));
			currentDataFile = new RandomAccessFile(dataFile, "rw");
			imageCounter = 0;
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {
			imageCounter++;
			if (imageCounter >= 32) {
				dataFileCounter++;
				if (dataFileCounter >= 32) {
					dataDirCounter++;
					dataFileCounter = 0;
					if (dataDirCounter >= 32)
						throw new RuntimeException("Maximum number of tiles exceeded");
				}
				prepareDataFile();
			}
			long offset = currentDataFile.getFilePointer();
			currentDataFile.write(tileData);
			int len = tileData.length;

			indexFile.writeShort((short) (17 - zoom));
			indexFile.write((tilex >> 16) & 0xFF);
			indexFile.write((tilex >> 8) & 0xFF);
			indexFile.write(tilex & 0xFF);
			indexFile.write((tiley >> 16) & 0xFF);
			indexFile.write((tiley >> 8) & 0xFF);
			indexFile.write(tiley & 0xFF);
			indexFile.writeInt((int) offset);

			int dataFileIndex = dataDirCounter * 32 + dataFileCounter;
			int tmp = (len << 4);
			tmp = tmp | ((dataFileIndex >> 8) & 0x0F);

			indexFile.write((tmp >> 16) & 0xFF);
			indexFile.write((tmp >> 8) & 0xFF);
			indexFile.write(tmp & 0xFF);
			indexFile.write(dataFileIndex & 0xFF);
			tileEntries++;
		}

		public void finalizeMap() throws IOException {
			// Write index header (first 16 bytes)
			indexFile.seek(0);
			int dataFileIndex = dataDirCounter * 32 + dataFileCounter;
			indexFile.writeInt(dataFileIndex); // Highest index used currently for the data files. Index is incremented
												// starting with 0.
			indexFile.writeInt(tileEntries); // Max. number of tiles index. Current max. number is 20000.
			indexFile.writeInt(tileEntries); // Number of tile entries (16 bytes) indexed.
			indexFile.writeInt((int) currentDataFile.getFilePointer()); // Size of the data file with the currently used
																		// highest index.
			Utilities.closeFile(currentDataFile);
			Utilities.closeFile(indexFile);
		}
	}
}
