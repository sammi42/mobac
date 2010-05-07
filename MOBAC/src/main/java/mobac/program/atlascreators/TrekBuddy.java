package mobac.program.atlascreators;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarArchive;
import mobac.utilities.tar.TarIndex;
import mobac.utilities.tar.TarTmiArchive;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

public abstract class TrekBuddy extends AtlasCreator {

	public static final String FILENAME_PATTERN = "t_%d_%d.%s";

	protected static final int COORD_KIND_LATTITUDE = 1;
	protected static final int COORD_KIND_LONGITUDE = 2;

	protected File layerFolder = null;
	protected File mapFolder = null;
	protected MapTileWriter mapTileWriter;

	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws IOException, InterruptedException,
			AtlasTestException {
		super.startAtlasCreation(atlas, customAtlasDir);
	}

	public void finishAtlasCreation() {
		switch (atlas.getOutputFormat()) {
		case TaredAtlas:
			createAtlasTarArchive("cr");
			break;
		case UntaredAtlas:
			createAtlasTbaFile("cr");
			break;
		}
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		LayerInterface layer = map.getLayer();
		layerFolder = new File(atlasDir, layer.getName());
		mapFolder = new File(layerFolder, map.getName());
	}

	protected void writeMapFile() throws IOException {
		File mapFile = new File(mapFolder, map.getName() + ".map");
		FileOutputStream mapFileStream = null;
		try {
			mapFileStream = new FileOutputStream(mapFile);
			writeMapFile(mapFileStream);
		} finally {
			Utilities.closeStream(mapFileStream);
		}
	}

	protected void writeMapFile(OutputStream stream) throws IOException {
		writeMapFile("t_." + mapSource.getTileType(), stream);
	}

	protected void writeMapFile(String imageFileName, OutputStream stream) throws IOException {
		log.trace("Writing map file");
		OutputStreamWriter mapWriter = new OutputStreamWriter(stream, TEXT_FILE_CHARSET);

		MapSpace mapSpace = mapSource.getMapSpace();

		double longitudeMin = mapSpace.cXToLon(xMin * tileSize, zoom);
		double longitudeMax = mapSpace.cXToLon((xMax + 1) * tileSize - 1, zoom);
		double latitudeMin = mapSpace.cYToLat((yMax + 1) * tileSize - 1, zoom);
		double latitudeMax = mapSpace.cYToLat(yMin * tileSize, zoom);

		int width = (xMax - xMin + 1) * tileSize;
		int height = (yMax - yMin + 1) * tileSize;

		mapWriter.write(prepareMapString(imageFileName, longitudeMin, longitudeMax, latitudeMin,
				latitudeMax, width, height));
		mapWriter.flush();
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		int tilex = 0;
		int tiley = 0;

		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));

		ImageIO.setUseCache(false);
		byte[] emptyTileData = Utilities.createEmptyTileData(mapSource);
		String tileType = mapSource.getTileType();
		for (int x = xMin; x <= xMax; x++) {
			tiley = 0;
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null) {
						mapTileWriter.writeTile(tilex, tiley, tileType, sourceTileData);
					} else {
						log.trace(String.format(
								"Tile x=%d y=%d not found in tile archive - creating default",
								tilex, tiley));
						mapTileWriter.writeTile(tilex, tiley, tileType, emptyTileData);
					}
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), e);
				}
				tiley++;
			}
			tilex++;
		}
	}

	public class TarTileWriter implements MapTileWriter {

		TarArchive ta = null;
		int tileHeight = 256;
		int tileWidth = 256;

		public TarTileWriter() {
			super();
			if (parameters != null) {
				tileHeight = parameters.getHeight();
				tileWidth = parameters.getWidth();
			}
			File mapTarFile = new File(mapFolder, map.getName() + ".tar");
			log.debug("Writing tiles to tared map: " + mapTarFile);
			try {
				ta = new TarTmiArchive(mapTarFile, null);
				ByteArrayOutputStream buf = new ByteArrayOutputStream(8192);
				writeMapFile(buf);
				ta.writeFileFromData(map.getName() + ".map", buf.toByteArray());
			} catch (IOException e) {
				log.error("", e);
			}
		}

		public void writeTile(int tilex, int tiley, String imageFormat, byte[] tileData)
				throws IOException {
			String tileFileName = String.format(FILENAME_PATTERN, (tilex * tileWidth),
					(tiley * tileHeight), imageFormat);

			ta.writeFileFromData("set/" + tileFileName, tileData);
		}

		public void finalizeMap() {
			try {
				ta.writeEndofArchive();
			} catch (IOException e) {
				log.error("", e);
			}
			ta.close();
		}

	}

	public class FileTileWriter implements MapTileWriter {

		File setFolder;
		Writer setFileWriter;

		int tileHeight = 256;
		int tileWidth = 256;

		public FileTileWriter() throws IOException {
			super();
			setFolder = new File(mapFolder, "set");
			Utilities.mkDir(setFolder);
			log.debug("Writing tiles to set folder: " + setFolder);
			File setFile = new File(mapFolder, map.getName() + ".set");
			if (parameters != null) {
				tileHeight = parameters.getHeight();
				tileWidth = parameters.getWidth();
			}
			try {
				setFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
						setFile), TEXT_FILE_CHARSET));
			} catch (IOException e) {
				log.error("", e);
			}
		}

		public void writeTile(int tilex, int tiley, String imageFormat, byte[] tileData)
				throws IOException {
			String tileFileName = String.format(FILENAME_PATTERN, (tilex * tileWidth),
					(tiley * tileHeight), imageFormat);

			File f = new File(setFolder, tileFileName);
			FileOutputStream out = new FileOutputStream(f);
			setFileWriter.write(tileFileName + "\r\n");
			try {
				out.write(tileData);
			} finally {
				Utilities.closeStream(out);
			}
		}

		public void finalizeMap() {
			try {
				setFileWriter.flush();
			} catch (IOException e) {
				log.error("", e);
			}
			Utilities.closeWriter(setFileWriter);
		}
	}

	protected String prepareMapString(String fileName, double longitudeMin, double longitudeMax,
			double latitudeMin, double latitudeMax, int width, int height) {

		StringBuffer sbMap = new StringBuffer();

		sbMap.append("OziExplorer Map Data File Version 2.2\r\n");
		sbMap.append(fileName + "\r\n");
		sbMap.append(fileName + "\r\n");
		sbMap.append("1 ,Map Code,\r\n");
		sbMap.append("WGS 84,WGS 84,   0.0000,   0.0000,WGS 84\r\n");
		sbMap.append("Reserved 1\r\n");
		sbMap.append("Reserved 2\r\n");
		sbMap.append("Magnetic Variation,,,E\r\n");
		sbMap.append("Map Projection,Mercator,PolyCal,No," + "AutoCalOnly,No,BSBUseWPX,No\r\n");

		String latMax = getDegMinFormat(latitudeMax, COORD_KIND_LATTITUDE);
		String latMin = getDegMinFormat(latitudeMin, COORD_KIND_LATTITUDE);
		String lonMax = getDegMinFormat(longitudeMax, COORD_KIND_LONGITUDE);
		String lonMin = getDegMinFormat(longitudeMin, COORD_KIND_LONGITUDE);

		String pointLine = "Point%02d,xy, %4s, %4s,in, deg, %1s, %1s, grid," + " , , ,N\r\n";

		sbMap.append(String.format(pointLine, 1, 0, 0, latMax, lonMin));
		sbMap.append(String.format(pointLine, 2, width - 1, 0, latMax, lonMax));
		sbMap.append(String.format(pointLine, 3, width - 1, height - 1, latMin, lonMax));
		sbMap.append(String.format(pointLine, 4, 0, height - 1, latMin, lonMin));

		for (int i = 5; i <= 30; i++) {
			String s = String.format(pointLine, i, "", "", "", "");
			sbMap.append(s);
		}
		sbMap.append("Projection Setup,,,,,,,,,,\r\n");
		sbMap.append("Map Feature = MF ; Map Comment = MC     These follow if they exist\r\n");
		sbMap.append("Track File = TF      These follow if they exist\r\n");
		sbMap.append("Moving Map Parameters = MM?    These follow if they exist\r\n");

		sbMap.append("MM0,Yes\r\n");
		sbMap.append("MMPNUM,4\r\n");

		String mmpxLine = "MMPXY, %d, %5d, %5d\r\n";

		sbMap.append(String.format(mmpxLine, 1, 0, 0));
		sbMap.append(String.format(mmpxLine, 2, width - 1, 0));
		sbMap.append(String.format(mmpxLine, 3, width - 1, height - 1));
		sbMap.append(String.format(mmpxLine, 4, 0, height - 1));

		String mpllLine = "MMPLL, %d, %2.6f, %2.6f\r\n";

		sbMap.append(String.format(Locale.ENGLISH, mpllLine, 1, longitudeMin, latitudeMax));
		sbMap.append(String.format(Locale.ENGLISH, mpllLine, 2, longitudeMax, latitudeMax));
		sbMap.append(String.format(Locale.ENGLISH, mpllLine, 3, longitudeMax, latitudeMin));
		sbMap.append(String.format(Locale.ENGLISH, mpllLine, 4, longitudeMin, latitudeMin));

		sbMap.append("MOP,Map Open Position,0,0\r\n");

		// The simple variant for calculating mm1b
		// http://www.trekbuddy.net/forum/viewtopic.php?t=3755&postdays=0&postorder=asc&start=286
		double mm1b = (longitudeMax - longitudeMin) * 111319
				* Math.cos((latitudeMax + latitudeMin) / 2) / width;
		sbMap.append(String.format(Locale.ENGLISH, "MM1B, %2.6f\r\n", mm1b));

		sbMap.append("IWH,Map Image Width/Height, " + width + ", " + height + "\r\n");

		return sbMap.toString();
	}

	/**
	 * 
	 * @param name
	 */
	public void createAtlasTarArchive(String name) {
		log.trace("Creating cr.tar for atlas in dir \"" + atlasDir.getPath() + "\"");

		File[] atlasLayerDirs = Utilities.listSubDirectories(atlasDir);
		List<File> atlasMapDirs = new LinkedList<File>();
		for (File dir : atlasLayerDirs)
			Utilities.addSubDirectories(atlasMapDirs, dir, 0);

		TarArchive ta = null;
		File crFile = new File(atlasDir, name + ".tar");
		try {
			ta = new TarArchive(crFile, atlasDir);

			ta.writeFileFromData(name + ".tba", "Atlas 1.0\r\n".getBytes());

			for (File mapDir : atlasMapDirs) {
				ta.writeFile(mapDir);
				File mapFile = new File(mapDir, mapDir.getName() + ".map");
				ta.writeFile(mapFile);
				try {
					mapFile.delete();
				} catch (Exception e) {
				}
			}
			ta.writeEndofArchive();
		} catch (IOException e) {
			log.error("Failed writing tar file \"" + crFile.getPath() + "\"", e);
		} finally {
			if (ta != null)
				ta.close();
		}
	}

	public void createAtlasTbaFile(String name) {
		File crtba = new File(atlasDir.getAbsolutePath(), name + ".tba");
		try {
			FileWriter fw = new FileWriter(crtba);
			fw.write("Atlas 1.0\r\n");
			fw.close();
		} catch (IOException e) {
			log.error("", e);
		}
	}

	private static String getDegMinFormat(double coord, int COORD_KIND) {

		boolean neg = (coord < 0.0);
		coord = Math.abs(coord);
		int deg = (int) coord;
		double min = (coord - deg) * 60;

		String degMinFormat = "%d, %3.6f, %c";

		char dirC;
		if (COORD_KIND == COORD_KIND_LATTITUDE)
			dirC = (neg ? 'S' : 'N');
		else
			dirC = (neg ? 'W' : 'E');

		return String.format(Locale.ENGLISH, degMinFormat, deg, min, dirC);
	}

}