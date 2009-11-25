package tac.program.mapcreators;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.exceptions.MapCreationException;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.tar.TarArchive;
import tac.tar.TarIndex;
import tac.tar.TarTmiArchive;
import tac.utilities.Utilities;

public abstract class MapCreatorTrekBuddy extends MapCreator {

	protected static final int COORD_KIND_LATTITUDE = 1;
	protected static final int COORD_KIND_LONGITUDE = 2;

	protected File mapFolder = null;

	@Override
	public void initialize(MapInterface map, TarIndex tarTileIndex, File atlasDir) {
		super.initialize(map, tarTileIndex, atlasDir);
		LayerInterface layer = map.getLayer();
		mapFolder = new File(new File(atlasDir, layer.getName()), map.getName());
	}

	protected void writeMapFile() {
		File mapFile = new File(mapFolder, map.getName() + ".map");
		FileOutputStream mapFileStream = null;
		try {
			mapFileStream = new FileOutputStream(mapFile);
			writeMapFile(mapFileStream);
		} catch (IOException e) {
			log.error("", e);
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
		double longitudeMax = mapSpace.cXToLon((xMax + 1) * tileSize, zoom);
		double latitudeMin = mapSpace.cYToLat((yMax + 1) * tileSize, zoom);
		double latitudeMax = mapSpace.cYToLat(yMin * tileSize, zoom);

		int width = (xMax - xMin + 1) * tileSize;
		int height = (yMax - yMin + 1) * tileSize;

		mapWriter.write(prepareMapString(imageFileName, longitudeMin, longitudeMax, latitudeMin,
				latitudeMax, width, height));
		mapWriter.flush();
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		int pixelValueX = 0;
		int pixelValueY = 0;

		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));

		ImageIO.setUseCache(false);
		int tileSize = map.getMapSource().getMapSpace().getTileSize();
		BufferedImage emptyImage = new BufferedImage(tileSize, tileSize,
				BufferedImage.TYPE_INT_ARGB);
		ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
		try {
			ImageIO.write(emptyImage, mapSource.getTileType(), buf);
		} catch (IOException e1) {
		}
		byte[] emptyTileData = buf.toByteArray();

		for (int x = xMin; x <= xMax; x++) {
			pixelValueY = 0;
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					String tileFileName = "t_" + (pixelValueX * tileSize) + "_"
							+ (pixelValueY * tileSize) + "." + mapSource.getTileType();
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null) {
						mapTileWriter.writeTile(tileFileName, sourceTileData);
					} else {
						log.trace("Tile \"" + tileFileName
								+ "\" not found in tile archive - creating default");
						mapTileWriter.writeTile(tileFileName, emptyTileData);
					}
				} catch (IOException e) {
					log.error("", e);
				}
				pixelValueY++;
			}
			pixelValueX++;
		}
	}

	public class TarTileWriter implements MapTileWriter {

		TarArchive ta = null;

		public TarTileWriter() {
			super();
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

		public void writeTile(String tileFileName, byte[] tileData) throws IOException {
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

		public FileTileWriter() throws IOException {
			super();
			setFolder = new File(mapFolder, "set");
			Utilities.mkDir(setFolder);
			log.debug("Writing tiles to set folder: " + setFolder);
			File setFile = new File(mapFolder, map.getName() + ".set");
			try {
				setFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
						setFile), TEXT_FILE_CHARSET));
			} catch (IOException e) {
				log.error("", e);
			}
		}

		public void writeTile(String tileFileName, byte[] tileData) throws IOException {
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

		sbMap.append("IWH,Map Image Width/Height, " + width + ", " + height + "\r\n");

		return sbMap.toString();
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