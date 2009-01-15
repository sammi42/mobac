package tac.program;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

	protected Logger log;

	protected static boolean tileSizeErrorNotified = false;

	protected int xMin;
	protected int xMax;
	protected int yMin;
	protected int yMax;

	protected File oziFolder;
	protected File atlasLayerFolder;
	protected int zoom;
	protected int tileSizeWidth;
	protected int tileSizeHeight;
	protected TileSource tileSource;

	protected HashMap<String, File> tilesInFileFormat;
	protected LinkedList<String> setFiles;

	protected String layerName;

	public MapCreator(SubMapProperties smp, File oziFolder, File atlasFolder, String mapName,
			TileSource tileSource, int zoom, int mapNumber, int tileSizeWidth, int tileSizeHeight) {
		log = Logger.getLogger(this.getClass());
		xMin = smp.getXMin();
		xMax = smp.getXMax();
		yMin = smp.getYMin();
		yMax = smp.getYMax();
		this.tileSource = tileSource;
		this.oziFolder = oziFolder;
		this.zoom = zoom;
		this.tileSizeWidth = tileSizeWidth;
		this.tileSizeHeight = tileSizeHeight;
		layerName = String.format("%s-%02d-%03d", new Object[] { mapName, zoom, mapNumber });
		tilesInFileFormat = new HashMap<String, File>();
		setFiles = new LinkedList<String>();
		atlasLayerFolder = new File(atlasFolder, layerName);
		log.debug("Creating map \"" + layerName + "\" (" + smp + ") tileSize (" + tileSizeWidth
				+ "/" + tileSizeHeight + ")");
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

			mapWriter.write(Utilities.prepareMapString(layerName + "." + tileSource.getTileType(),
					longitudeMin, longitudeMax, latitudeMin, latitudeMax, width, height));
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
					File fDest = new File(setFolder, layerName + "_" + (pixelValueX * 256) + "_"
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
}