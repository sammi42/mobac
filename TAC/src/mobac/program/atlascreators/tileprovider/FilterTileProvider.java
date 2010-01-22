package mobac.program.atlascreators.tileprovider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;


public abstract class FilterTileProvider implements TileProvider {

	protected final TileProvider tileProvider;

	public FilterTileProvider(TileProvider tileProvider) {
		this.tileProvider = tileProvider;
	}

	public BufferedImage getTileImage(int x, int y) throws IOException {
		return getTileImage(x, y, 0);
	}

	public BufferedImage getTileImage(int x, int y, int layer) throws IOException {
		byte[] unconvertedTileData = tileProvider.getTileData(x, y, layer);
		if (unconvertedTileData == null)
			return null;
		return ImageIO.read(new ByteArrayInputStream(unconvertedTileData));
	}
}
