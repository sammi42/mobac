package mobac.program.atlascreators;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;

import mobac.exceptions.MapCreationException;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.Utilities;
import mobac.utilities.stream.LittleEndianOutputStream;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

public class GlopusMapFile extends TrekBuddyCustom {

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			mapTileWriter = new GlopusTileWriter();

			// Select the tile creator instance based on whether tile image
			// parameters has been set or not
			if (parameters != null)
				createCustomTiles();
			else
				createTiles();

			mapTileWriter.finalizeMap();
		} catch (MapCreationException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(e);
		}
	}

	private class GlopusTileWriter implements MapTileWriter {

		LinkedList<GlopusTile> tiles;
		int tileHeight = 256;
		int tileWidth = 256;
		int zoom;
		MapSpace mapSpace;

		public GlopusTileWriter() {
			super();
			tiles = new LinkedList<GlopusTile>();
			if (parameters != null) {
				tileHeight = parameters.getHeight();
				tileWidth = parameters.getWidth();
			}
			zoom = map.getZoom();
			mapSpace = mapSource.getMapSpace();
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData)
				throws IOException {
			int xCooord = xMin + tilex * tileWidth;
			int yCooord = yMin + tiley * tileHeight;

			double calTLLon = mapSpace.cXToLon(xCooord, zoom);
			double calTLLat = mapSpace.cYToLat(yCooord, zoom);
			double calBRLon = mapSpace.cXToLon(xCooord + tileWidth - 1, zoom);
			double calBRLat = mapSpace.cYToLat(yCooord + tileHeight - 1, zoom);
			GlopusTile gt = new GlopusTile(tileData, calTLLat, calTLLon, calBRLat, calBRLon);
			tiles.add(gt);
		}

		public void finalizeMap() {

			File gmfFile = new File(layerFolder, map.getName() + ".gmf");
			FileOutputStream fout = null;
			try {
				Utilities.mkDirs(layerFolder);
				int count = tiles.size();
				int offset = 8 + count * (68 + 12);
				fout = new FileOutputStream(gmfFile);
				LittleEndianOutputStream out = new LittleEndianOutputStream(
						new BufferedOutputStream(fout, 16384));
				out.writeInt((int) 0xff000002);
				out.writeInt(count);
				int mapNumber = 0;
				Charset charset = Charset.forName("UTF-16LE");
				for (GlopusTile gt : tiles) {
					String mapName = String.format("%06d", mapNumber);
					byte[] nameBytes = mapName.getBytes(charset);
					out.writeInt(mapName.length());// Name length
					out.write(nameBytes);
					out.writeInt(offset);
					out.writeInt(tileWidth);
					out.writeInt(tileHeight);
					out.writeInt(2); // number of calibration points
					out.writeInt(0);
					out.writeInt(0);
					out.writeDouble(gt.calTLLon);
					out.writeDouble(gt.calTLLat);
					out.writeInt(tileHeight - 1);
					out.writeInt(tileWidth - 1);
					out.writeDouble(gt.calBRLon);
					out.writeDouble(gt.calBRLat);
					//log.trace(String.format("Offset \"%s\": 0x%x", mapName, offset));
					offset += gt.data.length;
				}
				out.flush();
				out = null;
				for (GlopusTile gt : tiles) {
					fout.write(gt.data);
				}
				fout.flush();
			} catch (IOException e) {
				GUIExceptionHandler.showExceptionDialog(e);
			} finally {
				Utilities.closeStream(fout);
			}
		}
	}

	private static class GlopusTile {
		byte[] data;
		double calTLLat;
		double calTLLon;
		double calBRLat;
		double calBRLon;

		public GlopusTile(byte[] data, double calTLLat, double calTLLon, double calBRLat,
				double calBRLon) {
			super();
			this.data = data;
			this.calTLLat = calTLLat;
			this.calTLLon = calTLLon;
			this.calBRLat = calBRLat;
			this.calBRLon = calBRLon;
		}

	}
}
