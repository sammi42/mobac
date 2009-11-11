package tac.program.mapcreators;

import java.io.IOException;

public interface RawTileProvider {
	
	public byte[] getTileData(int x, int y) throws IOException;
	
}
