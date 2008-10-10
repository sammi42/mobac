/**
 * OzitoAtlas is a class that converts a folder with tiles in OZI format to the atlas format for tiles.
 * For instance a tile name of y5015x8755.png will be converted to test13000001_0_0.png. It also resizes
 * the tiles if necessary.
 * 
 * @author      Fredrik M�ller
 * @version	    1.0	
 */

package tac.program;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class OziToAtlas {

	private File oziFolder;
	private File atlasFolder;
	private int tileSizeWidth;
	private int tileSizeHeight;
	private String mapName;
	private int zoom;

	public OziToAtlas(File oziFolder, File atlasFolder, int tileSizeWidth, int tileSizeHeight,
			String mapName, int zoom) {

		this.oziFolder = oziFolder;
		this.atlasFolder = atlasFolder;
		this.tileSizeWidth = tileSizeWidth;
		this.tileSizeHeight = tileSizeHeight;
		this.mapName = mapName;
		this.zoom = zoom;
	}

	public void convert(int xMax, int xMin, int yMax, int yMin) {

		Settings s = Settings.getInstance();
		int mapSize = s.getMapSize();

		List<SubMapProperties> subMaps = this.calculateMapSections(mapSize, xMin, xMax, yMin, yMax);

		int mapNumber = 1;

		for (SubMapProperties smp : subMaps) {
			MapCreator mc = new MapCreator(smp, oziFolder, atlasFolder, mapName, zoom, mapNumber,
					tileSizeWidth, tileSizeHeight);
			mc.createMap();
			mapNumber++;
		}
	}

	/**
	 * 
	 * @param mapSize
	 *            maximum size in pixels of a map. If this value is 0 the map
	 *            size is unlimited.
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @return map selections
	 */
	public List<SubMapProperties> calculateMapSections(int mapSize, int xMin, int xMax, int yMin,
			int yMax) {
		List<SubMapProperties> subMaps = new LinkedList<SubMapProperties>();

		int mapWidth = (xMax - xMin + 1) * 256;
		int mapHeight = (yMax - yMin + 1) * 256;

		if ((mapSize == 0) || ((mapWidth <= mapSize) && (mapHeight <= mapSize))) {
			/**
			 * Since the desired map area is smaller or equal to the maximum
			 * allowed map size it means that there is no need to calculate any
			 * sub maps.
			 */
			subMaps.add(new SubMapProperties(xMin, xMax, yMin, yMax));
			return subMaps;
		}

		/**
		 * If the desired map area is either wider or higher than the maximum
		 * allowed map size then a calculation and splitting into sub maps has
		 * to be done
		 */

		int stepSize = mapSize / 256;
		int yIndex = yMin;
		int xIndex = xMin;

		/**
		 * If the desired map area has a Height that is smaller or equal to the
		 * allowed map size it means that the sub map sections will be compiled
		 * of one row with sub maps.
		 * 
		 *<pre>
		 * Allowed map size:    +-------+
		 *                      |       |
		 *                      |       |
		 *                      +-------+
		 * Actual desired size: +----------------------------+
		 *                      |                            |
		 *                      |                            |
		 *                      +----------------------------+
		 * Resulting sub maps:  +-------+-------+-------+----+
		 *                      |       |       |       |    |
		 *                      |       |       |       |    |
		 *                      +-------+-------+-------+----+
		 * </pre>
		 */
		if ((mapWidth > mapSize) && (mapHeight <= mapSize)) {
			while (xIndex < xMax) {
				if (xIndex + stepSize - 1 <= xMax) {
					subMaps.add(new SubMapProperties(xIndex, xIndex + stepSize - 1, yMin, yMax));
					xIndex += stepSize;
				} else
					break;
			}
			if (mapWidth % mapSize != 0)
				subMaps.add(new SubMapProperties(xIndex, xMax, yMin, yMax));
		}
		/**
		 * If the desired map area has a Width that is smaller or equal to the
		 * allowed map size it means that the sub map sections will be compiled
		 * of one column with sub maps.
		 * 
		 *<pre>
		 * Allowed map size: Actual desired size: Resulting sub maps:    
		 * +-------+         +-------+            +-------+
		 * |       |         |       |            |       |
		 * |       |         |       |            |       |
		 * +-------+         |       |            +-------+
		 *                   |       |            |       |
		 *                   |       |            |       |
		 *                   |       |            +-------+
		 *                   |       |            |       |
		 *                   |       |            |       |
		 *                   |       |            +-------+
		 *                   |       |            |       |
		 *                   +-------+            +-------+
		 * </pre>
		 */
		else if ((mapHeight > mapSize) && (mapWidth <= mapSize)) {
			while (yIndex < yMax) {
				if (yIndex + stepSize - 1 <= yMax) {
					subMaps.add(new SubMapProperties(xMin, xMax, yIndex, yIndex + stepSize - 1));
					yIndex += stepSize;
				} else
					break;
			}
			if (mapHeight % mapSize != 0)
				subMaps.add(new SubMapProperties(xMin, xMax, yIndex, yMax));
		}
		/**
		 * If the desired map area has a Width and Height that is larger than
		 * the allowed map size it means that the sub map sections will be
		 * compiled as a raster of the desired size.
		 * 
		 *<pre>
		 * Allowed map size:   Actual desired size:          Resulting sub maps:    
		 * +-------+           +------------------+          +-------+-------+--+
		 * |       |           |                  |          |       |       |  |
		 * |       |           |                  |          |       |       |  |
		 * +-------+           |                  |          +-------+-------+--+
		 *                     |                  |          |       |       |  |
		 *                     |                  |          |       |       |  |
		 *                     |                  |          +-------+-------+--+
		 *                     |                  |          |       |       |  |
		 *                     |                  |          |       |       |  |
		 *                     |                  |          +-------+-------+--+
		 *                     |                  |          |       |       |  |
		 *                     +------------------+          +-------+-------+--+
		 * </pre>
		 */
		else if ((mapHeight > mapSize) && (mapWidth > mapSize)) {
			while (xIndex < xMax) {
				if (xIndex + stepSize - 1 <= xMax) {
					while (yIndex < yMax) {
						if (yIndex + stepSize - 1 <= yMax) {
							subMaps.add(new SubMapProperties(xIndex, xIndex + stepSize - 1, yIndex,
									yIndex + stepSize - 1));
							yIndex += stepSize;
						} else
							break;
					}
					if (mapHeight % mapSize != 0) {
						subMaps.add(new SubMapProperties(xIndex, xIndex + stepSize - 1, yIndex,
								yMax));
					}
					yIndex = yMin;
					xIndex += stepSize;
				} else
					break;
			}
			// Get all
			if (mapWidth % mapSize != 0) {
				while (yIndex < yMax) {
					if (yIndex + stepSize - 1 <= yMax) {
						subMaps.add(new SubMapProperties(xMin + ((mapWidth / mapSize) * stepSize),
								xMax, yIndex, yIndex + stepSize - 1));
						yIndex += stepSize;
					} else
						break;
				}
				if (mapHeight % mapSize != 0)
					subMaps.add(new SubMapProperties(xMin + ((mapWidth / mapSize) * stepSize),
							xMax, yIndex, yMax));
			}
		}
		return subMaps;
	}
}