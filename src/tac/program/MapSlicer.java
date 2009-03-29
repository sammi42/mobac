package tac.program;

import java.util.LinkedList;
import java.util.List;

import tac.program.model.MapSlice;

/**
 * Slices a large map into smaller pieces if the large map exceeds the
 * <code>mapSize</code> limit.
 */
public class MapSlicer {

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
	public static List<MapSlice> calculateMapSlices(int mapSize, int xMin, int xMax, int yMin,
			int yMax) {
		List<MapSlice> subMaps = new LinkedList<MapSlice>();

		int mapWidth = (xMax - xMin + 1) * 256;
		int mapHeight = (yMax - yMin + 1) * 256;

		if ((mapSize == 0) || ((mapWidth <= mapSize) && (mapHeight <= mapSize))) {
			/**
			 * Since the desired map area is smaller or equal to the maximum
			 * allowed map size it means that there is no need to calculate any
			 * sub maps.
			 */
			subMaps.add(new MapSlice(xMin, xMax, yMin, yMax));
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
					subMaps.add(new MapSlice(xIndex, xIndex + stepSize - 1, yMin, yMax));
					xIndex += stepSize;
				} else
					break;
			}
			if (mapWidth % mapSize != 0)
				subMaps.add(new MapSlice(xIndex, xMax, yMin, yMax));
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
					subMaps.add(new MapSlice(xMin, xMax, yIndex, yIndex + stepSize - 1));
					yIndex += stepSize;
				} else
					break;
			}
			if (mapHeight % mapSize != 0)
				subMaps.add(new MapSlice(xMin, xMax, yIndex, yMax));
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
							subMaps.add(new MapSlice(xIndex, xIndex + stepSize - 1, yIndex, yIndex
									+ stepSize - 1));
							yIndex += stepSize;
						} else
							break;
					}
					if (mapHeight % mapSize != 0) {
						subMaps.add(new MapSlice(xIndex, xIndex + stepSize - 1, yIndex, yMax));
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
						subMaps.add(new MapSlice(xMin + ((mapWidth / mapSize) * stepSize), xMax,
								yIndex, yIndex + stepSize - 1));
						yIndex += stepSize;
					} else
						break;
				}
				if (mapHeight % mapSize != 0)
					subMaps.add(new MapSlice(xMin + ((mapWidth / mapSize) * stepSize), xMax,
							yIndex, yMax));
			}
		}
		return subMaps;
	}
}