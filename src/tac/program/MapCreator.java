package tac.program;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.gui.AtlasProgress;
import tac.program.model.SubMapProperties;
import tac.utilities.Utilities;

/**
 * @author Fredrik
 */
public class MapCreator {

	private static final String TEXT_FILE_CHARSET = "ISO-8859-1";
	protected static final int COORD_KIND_LATTITUDE = 1;
	protected static final int COORD_KIND_LONGITUDE = 2;

	protected Logger log;

	protected static boolean tileSizeErrorNotified = false;

	protected int xMin;
	protected int xMax;
	protected int yMin;
	protected int yMax;

	protected File oziFolder;
	protected File atlasLayerFolder;
	protected int zoom;
	protected TileSource tileSource;

	protected HashMap<String, File> tilesInFileFormat;
	protected LinkedList<String> setFiles;

	protected String layerName;

	public MapCreator(SubMapProperties smp, File oziFolder, File atlasFolder, String mapName,
			TileSource tileSource, int zoom, int mapNumber) {
		log = Logger.getLogger(this.getClass());
		xMin = smp.getXMin();
		xMax = smp.getXMax();
		yMin = smp.getYMin();
		yMax = smp.getYMax();
		this.tileSource = tileSource;
		this.oziFolder = oziFolder;
		this.zoom = zoom;
		layerName = String.format("%s-%02d-%03d", new Object[] { mapName, zoom, mapNumber });
		tilesInFileFormat = new HashMap<String, File>();
		setFiles = new LinkedList<String>();
		atlasLayerFolder = new File(atlasFolder, layerName);
	}

	public void createMap() {
		atlasLayerFolder.mkdir();

		// write the .map file containing the calibration points
		writeMapFile();

		// Create the set folder where all the tiles shall be stored.
		File setFolder = new File(atlasLayerFolder, "set");
		setFolder.mkdir();

		// List all tiles in the ozi folder.
		File[] tiles = oziFolder.listFiles();

		// Put all tiles in a Hash Map so the will be easy to access later on.
		for (int i = 0; i < tiles.length; i++) {
			tilesInFileFormat.put(tiles[i].getName(), tiles[i]);
		}

		// This means there should not be any resizing of the tiles.
		try {
			createTiles(setFolder);
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		}
		writeSetFile();
	}

	private void writeMapFile() {
		log.trace("Writing map file");
		File mapFile = new File(atlasLayerFolder, layerName + ".map");

		OutputStreamWriter mapWriter = null;
		try {
			mapWriter = new OutputStreamWriter(new FileOutputStream(mapFile), TEXT_FILE_CHARSET);

			double longitudeMin = OsmMercator.XToLon(xMin * Tile.SIZE, zoom);
			double longitudeMax = OsmMercator.XToLon((xMax + 1) * Tile.SIZE, zoom);
			double latitudeMin = OsmMercator.YToLat((yMax + 1) * Tile.SIZE, zoom);
			double latitudeMax = OsmMercator.YToLat(yMin * Tile.SIZE, zoom);

			int width = (xMax - xMin + 1) * Tile.SIZE;
			int height = (yMax - yMin + 1) * Tile.SIZE;

			mapWriter.write(prepareMapString("t_." + tileSource.getTileType(), longitudeMin,
					longitudeMax, latitudeMin, latitudeMax, width, height));
			mapWriter.close();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeWriter(mapWriter);
		}
	}

	/**
	 * Copies the tile files (without any modification) from the ozi directory
	 * into the set directory of the map. While copying the filename is adapted
	 * to the tile naming schema.
	 * 
	 * @param setFolder
	 * @throws InterruptedException
	 */
	protected void createTiles(File setFolder) throws InterruptedException {
		int pixelValueX = 0;
		int pixelValueY = 0;

		Thread t = Thread.currentThread();
		AtlasProgress ap = null;
		if (t instanceof AtlasThread) {
			ap = ((AtlasThread) t).getAtlasProgress();
			int tileCount = (xMax - xMin + 1) * (yMax - yMin + 1);
			ap.initMapProgressBar(tileCount);
		}
		for (int x = xMin; x <= xMax; x++) {
			pixelValueY = 0;
			for (int y = yMin; y <= yMax; y++) {
				if (t.isInterrupted())
					throw new InterruptedException();
				if (ap != null)
					ap.incMapProgressBar();
				try {
					File fDest = new File(setFolder, "t_" + (pixelValueX * 256) + "_"
							+ (pixelValueY * 256) + "." + tileSource.getTileType());
					File fSource = (File) tilesInFileFormat.get("y" + y + "x" + x + "."
							+ tileSource.getTileType());
					if (fSource != null) {
						Utilities.fileCopy(fSource, fDest);
					} else {
						FileOutputStream fos = new FileOutputStream(fDest);
						BufferedImage emptyImage = new BufferedImage(256, 256,
								BufferedImage.TYPE_INT_ARGB);
						ImageIO.write(emptyImage, tileSource.getTileType(), fos);
						fos.close();
					}
					setFiles.add(fDest.getName());
				} catch (IOException e) {
					log.error("", e);
				}
				pixelValueY++;
			}
			pixelValueX++;
		}
	}

	private void writeSetFile() {
		// Create the set file for this map
		File setFile = new File(atlasLayerFolder, layerName + ".set");
		log.trace("Writing map .set file: " + setFile.getAbsolutePath());
		OutputStreamWriter setWriter = null;
		try {
			setWriter = new OutputStreamWriter(new FileOutputStream(setFile), TEXT_FILE_CHARSET);
			for (String file : setFiles) {
				setWriter.write(file + "\r\n");
			}
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeWriter(setWriter);
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

		sbMap.append("Point01,xy,    0,    0,in, deg,"
				+ getDegMinFormat(latitudeMax, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMin, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		sbMap.append("Point02,xy," + (width - 1) + ",0,in, deg,"
				+ getDegMinFormat(latitudeMax, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMax, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		sbMap.append("Point03,xy,    0," + (height - 1) + ",in, deg,"
				+ getDegMinFormat(latitudeMin, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMin, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		sbMap.append("Point04,xy," + (width - 1) + "," + (height - 1) + ",in, deg,"
				+ getDegMinFormat(latitudeMin, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMax, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		String emptyPointLine = "Point%02d,xy,     ,     ,"
				+ "in, deg,    ,        ,N,    ,        ,W, "
				+ "grid,   ,           ,           ,N\r\n";
		for (int i = 5; i <= 30; i++) {
			String s = String.format(emptyPointLine, new Object[] { i });
			sbMap.append(s);
		}
		sbMap.append("Projection Setup,,,,,,,,,,\r\n");
		sbMap.append("Map Feature = MF ; Map Comment = MC     " + "These follow if they exist\r\n");
		sbMap.append("Track File = TF      These follow if they exist\r\n");
		sbMap.append("Moving Map Parameters = MM?    " + "These follow if they exist\r\n");

		sbMap.append("MM0,Yes\r\n");
		sbMap.append("MMPNUM,4\r\n");
		sbMap.append("MMPXY,1,0,0\r\n");
		sbMap.append("MMPXY,2," + (width - 1) + ",0\r\n");
		sbMap.append("MMPXY,3,0," + (height - 1) + "\r\n");
		sbMap.append("MMPXY,4," + (width - 1) + "," + (height - 1) + "\r\n");

		DecimalFormat df6eng = Utilities.FORMAT_6_DEC_ENG;
		sbMap.append("MMPLL,1,  " + df6eng.format(longitudeMin) + "," + df6eng.format(latitudeMax)
				+ "\r\n");
		sbMap.append("MMPLL,2,  " + df6eng.format(longitudeMax) + "," + df6eng.format(latitudeMax)
				+ "\r\n");
		sbMap.append("MMPLL,3,  " + df6eng.format(longitudeMin) + "," + df6eng.format(latitudeMin)
				+ "\r\n");
		sbMap.append("MMPLL,4,  " + df6eng.format(longitudeMax) + "," + df6eng.format(latitudeMin)
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