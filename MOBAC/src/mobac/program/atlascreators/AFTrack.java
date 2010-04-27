package mobac.program.atlascreators;

import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndex;

/**
 * AFTrack OSZ Atlas format
 */
public class AFTrack extends OSMTracker {

	private ArrayList<Integer> zoomLevel = new ArrayList<Integer>();

	private int maxZoom;
	private Point min;
	private Point max;

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);
		File oszFile = new File(atlasDir, layer.getName() + ".osz");
		mapTileWriter = new OszTileWriter(oszFile);
		zoomLevel.clear();
		min = new Point();
		max = new Point();
		maxZoom = -1;
	}

	@Override
	public void finishLayerCreation() throws IOException {
		mapTileWriter.finalizeMap();
		mapTileWriter = null;

		super.finishLayerCreation();
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		zoomLevel.add(new Integer(map.getZoom()));
		if (map.getZoom() > maxZoom) {
			maxZoom = map.getZoom();
			min.x = map.getMinTileCoordinate().x / 256;
			min.y = map.getMinTileCoordinate().y / 256;
			max.x = map.getMaxTileCoordinate().x / 256;
			max.y = map.getMaxTileCoordinate().y / 256;
		}
	}

	private class OszTileWriter extends OSMTileWriter {

		ZipOutputStream zipStream;
		FileOutputStream out;

		private CRC32 crc = new CRC32();

		public OszTileWriter(File oszFile) throws FileNotFoundException {
			super();
			out = new FileOutputStream(oszFile);
			zipStream = new ZipOutputStream(out);
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData)
				throws IOException {
			String entryName = String.format(tileFileNamePattern, zoom, tilex, tiley, tileType);
			writeZipEntry(entryName, tileData);
		}

		private void writeZipEntry(String entryName, byte[] data) throws IOException {
			ZipEntry entry = new ZipEntry(entryName);

			entry.setMethod(ZipEntry.STORED);
			entry.setCompressedSize(data.length);
			entry.setSize(data.length);
			crc.reset();
			crc.update(data);
			entry.setCrc(crc.getValue());
			zipStream.putNextEntry(entry);
			zipStream.write(data);
			zipStream.closeEntry();
		}

		public void finalizeMap() throws IOException {
			ByteArrayOutputStream bout = new ByteArrayOutputStream(100);
			OutputStreamWriter writer = new OutputStreamWriter(bout);

			Collections.sort(zoomLevel);
			for (Integer zoom : zoomLevel)
				writer.append(String.format("zoom=%d\r\n", zoom.intValue()));
			writer.append(String.format("minx=%d\r\n", min.x));
			writer.append(String.format("maxx=%d\r\n", max.x));
			writer.append(String.format("miny=%d\r\n", min.y));
			writer.append(String.format("maxy=%d\r\n", max.y));
			writer.close();
			writeZipEntry("Manifest.txt", bout.toByteArray());
			Utilities.closeStream(zipStream);
		}

	}

}
