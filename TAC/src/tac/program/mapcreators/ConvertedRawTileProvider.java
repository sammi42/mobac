package tac.program.mapcreators;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import tac.program.interfaces.TileImageDataWriter;
import tac.program.model.TileImageFormat;

/**
 * Loads a tile from the underlying {@link RawTileProvider}, loads the tile to
 * memory, converts it to the desired {@link TileImageFormat} and returns the
 * binary representation of the image in the specified format.
 */
public class ConvertedRawTileProvider implements RawTileProvider {

	private RawTileProvider tileProvider;

	private TileImageDataWriter writer;

	public ConvertedRawTileProvider(RawTileProvider tileProvider, TileImageFormat tileImageFormat) {
		this.tileProvider = tileProvider;
		writer = tileImageFormat.getDataWriter();
		writer.initialize();
		ImageIO.setUseCache(false);
	}

	public byte[] getTileData(int x, int y) throws IOException {
		return getTileData(0, x, y);
	}

	public byte[] getTileData(int layer, int x, int y) throws IOException {
		byte[] unconvertedTileData = tileProvider.getTileData(layer, x, y);
		if (unconvertedTileData == null)
			return null;
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(unconvertedTileData));
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(32000);
		writer.processImage(image, buffer);
		return buffer.toByteArray();
	}

}
