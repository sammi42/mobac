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

/**
 * General structure of an GMF file (Little Endian)
 * 
 * <pre>
 * DWORD Version // 0xff000002
 * DWORD cnt // Number of tiles in the file
 * 
 * for each tile: 
 *   DWORD len;         // number of characters in tile name
 *   wchar_t name[len]  // map/tile name in UTF_16LE
 *   DWORD filepos      // offset where image data starts in this file
 *   DWORD width        // tile width in pixel
 *   DWORD height       // tile height in pixel
 *   DWORD cntCalPoints // calibration point count (usually 2 or 4)
 *   for each tile calibration point
 *     DWORD x      // calibration point x position in tile
 *     DWORD y      // calibration point y position in tile
 *     double dLong // longitude of calibration point
 *     double dLat  // latitude of calibration point
 * END OF FILE HEADER
 * Afterwards the tile image data follows as specified by each filepos 
 * offset. 
 * </pre>
 * 
 */
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
		int xCoordStart;
		int yCoordStart;
		int tileHeight = 256;
		int tileWidth = 256;
		int zoom;
		MapSpace mapSpace;
		String tileType;

		public GlopusTileWriter() {
			super();
			tiles = new LinkedList<GlopusTile>();
			if (parameters != null) {
				tileHeight = parameters.getHeight();
				tileWidth = parameters.getWidth();
			}
			zoom = map.getZoom();
			mapSpace = mapSource.getMapSpace();
			xCoordStart = GlopusMapFile.this.xMin * mapSpace.getTileSize();
			yCoordStart = GlopusMapFile.this.yMin * mapSpace.getTileSize();
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData)
				throws IOException {
			this.tileType = tileType;
			int xCooord = xCoordStart + tilex * tileWidth;
			int yCooord = yCoordStart + tiley * tileHeight;

			double calTLLon = mapSpace.cXToLon(xCooord, zoom);
			double calTLLat = mapSpace.cYToLat(yCooord, zoom);
			double calBRLon = mapSpace.cXToLon(xCooord + tileWidth, zoom);
			double calBRLat = mapSpace.cYToLat(yCooord + tileHeight, zoom);
			GlopusTile gt = new GlopusTile(tileData, calTLLat, calTLLon, calBRLat, calBRLon);
			tiles.add(gt);
		}

		public void finalizeMap() {

			File gmfFile = new File(layerFolder, map.getName() + ".gmf");
			FileOutputStream fout = null;
			try {
				Utilities.mkDirs(layerFolder);
				int count = tiles.size();
				int offset = 8 + count * (68 + 20);
				fout = new FileOutputStream(gmfFile);
				LittleEndianOutputStream out = new LittleEndianOutputStream(
						new BufferedOutputStream(fout, 16384));
				out.writeInt((int) 0xff000002);
				out.writeInt(count);
				int mapNumber = 0;
				Charset charset = Charset.forName("UTF-16LE");
				for (GlopusTile gt : tiles) {
					String mapName = String.format("%06d.%s", mapNumber++, tileType);
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
					out.writeInt(tileHeight);
					out.writeInt(tileWidth);
					out.writeDouble(gt.calBRLon);
					out.writeDouble(gt.calBRLat);
					if (log.isTraceEnabled())
						log.trace(String.format("Offset %f %f %f %f \"%s\": 0x%x", gt.calTLLon,
								gt.calTLLat, gt.calBRLon, gt.calBRLon, mapName, offset));
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
