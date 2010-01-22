package mobac.program.atlascreators.tileprovider;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageFormat;


/**
 * Loads a tile from the underlying {@link TileProvider}, loads the tile to
 * memory, converts it to the desired {@link TileImageFormat} and returns the
 * binary representation of the image in the specified format.
 */
public class ConvertedRawTileProvider extends FilterTileProvider {

	private TileImageDataWriter writer;

	public ConvertedRawTileProvider(TileProvider tileProvider, TileImageFormat tileImageFormat) {
		super(tileProvider);
		writer = tileImageFormat.getDataWriter();
		writer.initialize();
		ImageIO.setUseCache(false);
	}

	public byte[] getTileData(int x, int y) throws IOException {
		return getTileData(x, y, 0);
	}

	public byte[] getTileData(int x, int y, int layer) throws IOException {
		RenderedImage image = getTileImage(x, y, layer);
		if (image == null)
			return null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(32000);
		writer.processImage(image, buffer);
		return buffer.toByteArray();
	}

}
