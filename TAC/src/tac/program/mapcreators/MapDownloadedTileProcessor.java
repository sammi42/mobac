package tac.program.mapcreators;

import java.io.IOException;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.tar.TarIndex;

public class MapDownloadedTileProcessor implements RawTileProvider {

	public static final String TILE_FILENAME_PATTERN = "l%dx%dy%d.%s";

	protected final TarIndex tarIndex;
	protected final String mapTileType;

	public MapDownloadedTileProcessor(TarIndex tarIndex, MapSource mapSource) {
		this.tarIndex = tarIndex;
		this.mapTileType = mapSource.getTileType();
	}

	public byte[] getTileData(int x, int y) throws IOException {
		return getTileData(0, x, y);
	}

	public byte[] getTileData(int layer, int x, int y) throws IOException {
		return tarIndex.getEntryContent(String.format(TILE_FILENAME_PATTERN, layer, x, y,
				mapTileType));
	}

}
