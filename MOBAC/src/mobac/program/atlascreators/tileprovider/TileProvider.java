package mobac.program.atlascreators.tileprovider;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface TileProvider {

	public byte[] getTileData(int x, int y) throws IOException;

	public byte[] getTileData(int layer, int x, int y) throws IOException;

	public BufferedImage getTileImage(int x, int y) throws IOException;

	public BufferedImage getTileImage(int x, int y, int layer) throws IOException;
}
