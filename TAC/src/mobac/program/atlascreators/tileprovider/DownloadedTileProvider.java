package mobac.program.atlascreators.tileprovider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


public class DownloadedTileProvider implements TileProvider {

	public static final String TILE_FILENAME_PATTERN = "l%dx%dy%d";

	protected final TarIndex tarIndex;
	protected final String mapTileType;

	public DownloadedTileProvider(TarIndex tarIndex, MapSource mapSource) {
		this.tarIndex = tarIndex;
		this.mapTileType = mapSource.getTileType();
	}

	public byte[] getTileData(int x, int y) throws IOException {
		return getTileData(x, y, 0);
	}

	public byte[] getTileData(int x, int y, int layer) throws IOException {
		return tarIndex.getEntryContent(String.format(TILE_FILENAME_PATTERN, layer, x, y));
	}

	public BufferedImage getTileImage(int x, int y) throws IOException {
		return getTileImage(x, y, 0);
	}

	public BufferedImage getTileImage(int x, int y, int layer) throws IOException {
		byte[] unconvertedTileData = getTileData(x, y, layer);
		if (unconvertedTileData == null)
			return null;
		return ImageIO.read(new ByteArrayInputStream(unconvertedTileData));
	}
}
