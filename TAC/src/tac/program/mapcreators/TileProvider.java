package tac.program.mapcreators;

import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class TileProvider {

	public abstract byte[] getTileData(int x, int y) throws IOException;

	public abstract byte[] getTileData(int layer, int x, int y) throws IOException;

	public abstract BufferedImage getTileImage(int x, int y) throws IOException;

	public abstract BufferedImage getTileImage(int x, int y, int layer) throws IOException;
}
