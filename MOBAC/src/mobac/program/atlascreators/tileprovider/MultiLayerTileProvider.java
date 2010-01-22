package mobac.program.atlascreators.tileprovider;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageFormat;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


public class MultiLayerTileProvider extends FilterTileProvider {

	private final MapSource mapSource;

	private TileImageDataWriter writer;

	private int layerCount;

	public MultiLayerTileProvider(MapSource mapSource, TileProvider tileProvider, int layerCount) {
		super(tileProvider);
		this.mapSource = mapSource;
		this.layerCount = layerCount;
		TileImageFormat tileImageFormat = TileImageFormat.PNG;
		writer = tileImageFormat.getDataWriter();
		writer.initialize();
		ImageIO.setUseCache(false);
	}

	public byte[] getTileData(int layer, int x, int y) throws IOException {
		return getTileData(x, y);
	}

	public byte[] getTileData(int x, int y) throws IOException {
		RenderedImage combinedImage = getTileImage(x, y);
		if (combinedImage == null)
			return null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(32000);
		writer.processImage(combinedImage, buffer);
		return buffer.toByteArray();
	}

	@Override
	public BufferedImage getTileImage(int x, int y, int layer) throws IOException {
		int tileSize = mapSource.getMapSpace().getTileSize();
		BufferedImage combinedImage = new BufferedImage(tileSize, tileSize,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = combinedImage.getGraphics();
		boolean used = false;
		try {
			for (int l = 0; l < layerCount; l++) {
				BufferedImage image = tileProvider.getTileImage(x, y, l);
				if (image == null)
					continue;
				g.drawImage(image, 0, 0, null);
				used = true;
			}
		} finally {
			g.dispose();
		}
		if (used)
			return combinedImage;
		else
			return null;
	}
}
