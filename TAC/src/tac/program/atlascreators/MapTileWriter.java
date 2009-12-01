package tac.program.atlascreators;

import java.io.IOException;

public interface MapTileWriter {

	public void writeTile(String tileFileName, byte[] tileData) throws IOException;

	public void finalizeMap();

}
