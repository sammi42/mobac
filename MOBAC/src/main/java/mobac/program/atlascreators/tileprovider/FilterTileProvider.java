package mobac.program.atlascreators.tileprovider;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Base implementation of an {@link TileProvider} that changes somehow the images, e.g. combines two layers to one or
 * paints something onto a tile image.
 */
public class FilterTileProvider implements TileProvider {

	protected final Logger log;

	protected final TileProvider tileProvider;

	public FilterTileProvider(TileProvider tileProvider) {
		log = Logger.getLogger(this.getClass());
		this.tileProvider = tileProvider;
	}

	public BufferedImage getTileImage(int x, int y) throws IOException {
		return getTileImage(x, y, 0);
	}

	public BufferedImage getTileImage(int x, int y, int layer) throws IOException {
		return tileProvider.getTileImage(x, y, layer);
	}

	public byte[] getTileData(int x, int y) throws IOException {
		return getTileData(x, y, 0);
	}

	public byte[] getTileData(int x, int y, int layer) throws IOException {
		return tileProvider.getTileData(x, y, layer);
	}
}
