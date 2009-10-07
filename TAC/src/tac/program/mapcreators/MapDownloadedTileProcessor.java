package tac.program.mapcreators;

import java.io.IOException;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.tar.TarIndex;

public class MapDownloadedTileProcessor {

	TarIndex tarIndex;
	String mapTileType;

	public MapDownloadedTileProcessor(TarIndex tarIndex, MapSource mapSource) {
		this.tarIndex = tarIndex;
		this.mapTileType = mapSource.getTileType();
	}

	public byte[] getTileData(int x, int y) throws IOException {
		return tarIndex.getEntryContent("y" + y + "x" + x + "." + mapTileType);
	}
	
}
