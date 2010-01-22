package mobac.program.atlascreators.impl;

import java.io.IOException;

public interface MapTileWriter {

	/**
	 * 
	 * @param tilex
	 *            x tile number regarding regarding the currently processed map
	 *            (0..mapWidth / tileWidth)]
	 * @param tiley
	 *            y tile number regarding regarding the currently processed map
	 *            (0..mapheight / tileHeight)]
	 * @param tileType
	 * @param tileData
	 * @throws IOException
	 */
	public void writeTile(int tilex, int tiley, String tileType, byte[] tileData)
			throws IOException;

	public void finalizeMap();

}
