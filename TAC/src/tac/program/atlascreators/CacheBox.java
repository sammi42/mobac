package tac.program.atlascreators;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.MapCreationException;
import tac.mapsources.mapspace.MercatorPower2MapSpace;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;
import tac.utilities.Utilities;

public class CacheBox extends AtlasCreator {

	private File packFile = null;
	private RandomAccessFile packRaFile = null;
	private MapOffsetInfo[] offsetInfos;

	private int nextMapOffsetIndex = 0;
	private MapOffsetInfo activeMapOffsetInfo;

	@Override
	public void startAtlasCreation(AtlasInterface atlas) throws IOException {
		super.startAtlasCreation(atlas);
	}

	@Override
	public void finishAtlasCreation() throws IOException {
	}

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		nextMapOffsetIndex = 0;
		packFile = new File(atlasDir, layer.getName() + ".pack");
		if (packFile.exists())
			Utilities.deleteFile(packFile);
		packRaFile = new RandomAccessFile(packFile, "rw");
		writeString(layer.getName(), 32); // layer name
		writeString(layer.getName(), 128); // layer friendly name
		writeString("", 256); // layer url - unused
		writeLong(0); // int64 ticks
		int mapCount = layer.getMapCount();
		writeInt(mapCount); // int32 number of bounding boxes / maps

		long offset = 32 + 128 + 256 + 8 + 4; // = 428
		offset += mapCount * 28;
		offsetInfos = new MapOffsetInfo[mapCount];

		int i = 0;
		for (MapInterface map : layer) {
			// For each map:

			int minX = map.getMinTileCoordinate().x / 256;
			int minY = map.getMinTileCoordinate().y / 256;
			int maxX = map.getMaxTileCoordinate().x / 256;
			int maxY = map.getMaxTileCoordinate().y / 256;
			int tilesInMap = (maxX - minX + 1) * (maxY - minY + 1);

			writeInt(map.getZoom()); // int32 zoom
			writeInt(minX); // int32 minX
			writeInt(maxX); // int32 maxX
			writeInt(minY); // int32 minY
			writeInt(maxY); // int32 maxY

			writeLong(offset); // int64 offset to mapIndexTable
			offsetInfos[i++] = new MapOffsetInfo(map, offset, tilesInMap);
			log.trace(String.format("Offset to index table [%d]: 0x%X", i, offset));

			offset += tilesInMap * 8;
		}
		log.trace(String.format("End of bounding boxes table: 0x%X", packRaFile.getFilePointer()));
		packRaFile.seek(offset);
		log.trace(String.format("Start of tile data: 0x%X", packRaFile.getFilePointer()));
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		activeMapOffsetInfo = offsetInfos[nextMapOffsetIndex++];
		if (!activeMapOffsetInfo.map.equals(map))
			throw new RuntimeException("Map does not match offset info!");
	}

	@Override
	public void createMap() throws MapCreationException {
		try {
			createTiles();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		}
	}

	@Override
	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));

		ImageIO.setUseCache(false);

		int offsetIndex = 0;
		long[] offsets = new long[activeMapOffsetInfo.tileCount];

		try {
			for (int y = yMin; y <= yMax; y++) {
				for (int x = xMin; x <= xMax; x++) {
					checkUserAbort();
					atlasProgress.incMapCreationProgress();
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null) {
						offsets[offsetIndex++] = packRaFile.getFilePointer();
						packRaFile.write(sourceTileData);
					} else {
						offsets[offsetIndex++] = 0; // tile does not exist
					}
				}
			}
			long pos = packRaFile.getFilePointer();
			// Write the offsets of all tiles in this map to the correspondent
			// offset index table
			packRaFile.seek(activeMapOffsetInfo.indexTableOffset);
			for (long tileoffset : offsets)
				writeLong(tileoffset);
			packRaFile.seek(pos);
		} catch (IOException e) {
			throw new MapCreationException(e);
		}
	}

	@Override
	public void finishLayerCreation() throws IOException {
		offsetInfos = null;
		packFile = null;
		packRaFile.close();
		packRaFile = null;
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		Utilities.closeFile(packRaFile);
		if (packFile != null)
			packFile.delete();
	}

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	private void writeString(String text, int length) throws IOException {
		byte[] buf = new byte[length];
		byte[] asciiBytes = text.getBytes("ASCII");
		System.arraycopy(asciiBytes, 0, buf, 0, Math.min(length, asciiBytes.length));
		for (int i = asciiBytes.length; i < length; i++)
			buf[i] = ' ';
		packRaFile.write(buf);
	}

	private void writeInt(int v) throws IOException {
		packRaFile.writeInt(swapInt(v));
	}

	private void writeLong(long v) throws IOException {
		packRaFile.writeLong(swapLong(v));
	}

	public final static int swapInt(int v) {
		return (v >>> 24) | (v << 24) | ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00);
	}

	public final static long swapLong(long v) {
		long b1 = (v >> 0) & 0xff;
		long b2 = (v >> 8) & 0xff;
		long b3 = (v >> 16) & 0xff;
		long b4 = (v >> 24) & 0xff;
		long b5 = (v >> 32) & 0xff;
		long b6 = (v >> 40) & 0xff;
		long b7 = (v >> 48) & 0xff;
		long b8 = (v >> 56) & 0xff;

		return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 | b5 << 24 | b6 << 16 | b7 << 8 | b8 << 0;
	}

	private class MapOffsetInfo {

		MapInterface map;
		long indexTableOffset = 0;
		int tileCount;

		public MapOffsetInfo(MapInterface map, long indexOffset, int tileCount) {
			super();
			this.map = map;
			this.indexTableOffset = indexOffset;
			this.tileCount = tileCount;
		}
	}
}