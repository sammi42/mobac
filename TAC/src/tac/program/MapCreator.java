package tac.program;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.gui.AtlasProgress;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.program.model.AtlasOutputFormat;
import tac.tar.TarArchive;
import tac.tar.TarIndex;
import tac.tar.TarTmiArchive;
import tac.utilities.Utilities;

public class MapCreator {

	public static final String TEXT_FILE_CHARSET = "ISO-8859-1";
	protected static final int COORD_KIND_LATTITUDE = 1;
	protected static final int COORD_KIND_LONGITUDE = 2;

	protected Logger log;

	protected static boolean tileSizeErrorNotified = false;

	protected MapInterface map;
	protected int xMin;
	protected int xMax;
	protected int yMin;
	protected int yMax;

	protected TarIndex tarTileIndex;
	protected File mapFolder;
	protected int zoom;
	protected AtlasOutputFormat atlasOutputFormat;
	protected MapSource mapSource;

	protected MapTileWriter mapTileWriter;

	public MapCreator(MapInterface map, TarIndex tarTileIndex, File atlasDir) {
		log = Logger.getLogger(this.getClass());
		LayerInterface layer = map.getLayer();
		this.map = map;
		xMin = map.getMinTileCoordinate().x / Tile.SIZE;
		xMax = map.getMaxTileCoordinate().x / Tile.SIZE;
		yMin = map.getMinTileCoordinate().y / Tile.SIZE;
		yMax = map.getMaxTileCoordinate().y / Tile.SIZE;
		this.mapSource = map.getMapSource();
		this.tarTileIndex = tarTileIndex;
		this.zoom = map.getZoom();
		this.atlasOutputFormat = layer.getAtlas().getOutputFormat();
		mapFolder = new File(new File(atlasDir, layer.getName()), map.getName());
	}

	public void createMap() {
		mapFolder.mkdirs();

		// write the .map file containing the calibration points
		writeMapFile();

		// This means there should not be any resizing of the tiles.
		try {
			if (atlasOutputFormat == AtlasOutputFormat.TaredAtlas)
				mapTileWriter = new TarTileWriter();
			else
				mapTileWriter = new FileTileWriter();
			createTiles();
			mapTileWriter.finalizeMap();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		}
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

		double longitudeMin = OsmMercator.XToLon(xMin * Tile.SIZE, zoom);
		double longitudeMax = OsmMercator.XToLon((xMax + 1) * Tile.SIZE, zoom);
		double latitudeMin = OsmMercator.YToLat((yMax + 1) * Tile.SIZE, zoom);
		double latitudeMax = OsmMercator.YToLat(yMin * Tile.SIZE, zoom);

		int width = (xMax - xMin + 1) * Tile.SIZE;
		int height = (yMax - yMin + 1) * Tile.SIZE;

		mapWriter.write(prepareMapString(imageFileName, longitudeMin, longitudeMax, latitudeMin,
				latitudeMax, width, height));
		mapWriter.flush();
	}

	protected void createTiles() throws InterruptedException {
		int pixelValueX = 0;
		int pixelValueY = 0;

		Thread t = Thread.currentThread();
		AtlasProgress ap = null;
		if (t instanceof AtlasThread) {
			ap = ((AtlasThread) t).getAtlasProgress();
			ap.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		}
		ImageIO.setUseCache(false);
		BufferedImage emptyImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
		try {
			ImageIO.write(emptyImage, mapSource.getTileType(), buf);
		} catch (IOException e1) {
		}
		byte[] emptyTileData = buf.toByteArray();

		for (int x = xMin; x <= xMax; x++) {
			pixelValueY = 0;
			for (int y = yMin; y <= yMax; y++) {
				if (t.isInterrupted())
					throw new InterruptedException();
				if (ap != null)
					ap.incMapCreationProgress();
				try {
					String tileFileName = "t_" + (pixelValueX * 256) + "_" + (pixelValueY * 256)
							+ "." + mapSource.getTileType();
					byte[] sourceTileData = tarTileIndex.getEntryContent("y" + y + "x" + x + "."
							+ mapSource.getTileType());
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

		public FileTileWriter() {
			super();
			setFolder = new File(mapFolder, "set");
			setFolder.mkdir();
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

	public abstract interface MapTileWriter {

		public void writeTile(String tileFileName, byte[] tileData) throws IOException;

		public void finalizeMap();
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

		sbMap.append("Point01,xy,    0,    0,in, deg,"
				+ getDegMinFormat(latitudeMax, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMin, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		sbMap.append("Point02,xy," + (width - 1) + ",0,in, deg,"
				+ getDegMinFormat(latitudeMax, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMax, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		sbMap.append("Point03,xy," + (width - 1) + "," + (height - 1) + ",in, deg,"
				+ getDegMinFormat(latitudeMin, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMax, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		sbMap.append("Point04,xy,    0," + (height - 1) + ",in, deg,"
				+ getDegMinFormat(latitudeMin, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMin, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		String emptyPointLine = "Point%02d,xy,     ,     ,"
				+ "in, deg,    ,        ,N,    ,        ,W, "
				+ "grid,   ,           ,           ,N\r\n";
		for (int i = 5; i <= 30; i++) {
			String s = String.format(emptyPointLine, new Object[] { i });
			sbMap.append(s);
		}
		sbMap.append("Projection Setup,,,,,,,,,,\r\n");
		sbMap.append("Map Feature = MF ; Map Comment = MC     These follow if they exist\r\n");
		sbMap.append("Track File = TF      These follow if they exist\r\n");
		sbMap.append("Moving Map Parameters = MM?    These follow if they exist\r\n");

		sbMap.append("MM0,Yes\r\n");
		sbMap.append("MMPNUM,4\r\n");
		sbMap.append("MMPXY,1,0,0\r\n");
		sbMap.append("MMPXY,2," + (width - 1) + ",0\r\n");
		sbMap.append("MMPXY,3," + (width - 1) + "," + (height - 1) + "\r\n");
		sbMap.append("MMPXY,4,0," + (height - 1) + "\r\n");

		DecimalFormat df6eng = Utilities.FORMAT_6_DEC_ENG;
		sbMap.append("MMPLL,1,  " + df6eng.format(longitudeMin) + "," + df6eng.format(latitudeMax)
				+ "\r\n");
		sbMap.append("MMPLL,2,  " + df6eng.format(longitudeMax) + "," + df6eng.format(latitudeMax)
				+ "\r\n");
		sbMap.append("MMPLL,3,  " + df6eng.format(longitudeMax) + "," + df6eng.format(latitudeMin)
				+ "\r\n");
		sbMap.append("MMPLL,4,  " + df6eng.format(longitudeMin) + "," + df6eng.format(latitudeMin)
				+ "\r\n");

		sbMap.append("IWH,Map Image Width/Height," + width + "," + height + "\r\n");

		return sbMap.toString();
	}

	private static String getDegMinFormat(double coord, int COORD_KIND) {

		boolean neg = coord < 0.0 ? true : false;
		int deg = (int) coord;
		double min = (coord - deg) * 60;

		StringBuffer sbOut = new StringBuffer();
		sbOut.append((int) Math.abs(deg));
		sbOut.append(",");
		sbOut.append(Utilities.FORMAT_6_DEC_ENG.format(Math.abs(min)));
		sbOut.append(",");

		if (COORD_KIND == COORD_KIND_LATTITUDE) {
			sbOut.append(neg ? "S" : "N");
		} else {
			sbOut.append(neg ? "W" : "E");
		}
		return sbOut.toString();
	}

}