package tac.program.mapcreators;

import java.io.IOException;

public interface RawTileProvider {

	public byte[] getTileData(int x, int y) throws IOException;

	public byte[] getTileData(int layer, int x, int y) throws IOException;

}
