package tac.program;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;

import tac.utilities.Utilities;

/**
 * @author Fredrik
 * 
 */
public class MapCreator {
	private Logger log = Logger.getLogger(MapCreator.class);
	private static boolean tileSizeErrorNotified = false;

	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;

	private File oziFolder;
	private File atlasLayerFolder;
	private int zoom;
	private int tileSizeWidth;
	private int tileSizeHeight;

	private HashMap<String, File> tilesInFileFormat;
	private LinkedList<String> setFiles;

	private String layerName;

	public MapCreator(SubMapProperties smp, File oziFolder, File atlasFolder, String mapName,
			int zoom, int mapNumber, int tileSizeWidth, int tileSizeHeight) {
		xMin = smp.getXMin();
		xMax = smp.getXMax();
		yMin = smp.getYMin();
		yMax = smp.getYMax();
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
			if (tileSizeWidth == 256 && tileSizeHeight == 256)
				createDefaultSizedTiles(setFolder);
			else
				createCustomSizedTiles(setFolder);
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		}
		writeSetFile();
	}

	private void writeMapFile() {
		log.trace("Writing map file");
		File mapFile = new File(atlasLayerFolder, layerName + ".map");

		FileWriter fw = null;
		try {
			fw = new FileWriter(mapFile);

			double longitudeMin = OsmMercator.XToLon(xMin * Tile.SIZE, zoom);
			double longitudeMax = OsmMercator.XToLon((xMax + 1) * Tile.SIZE, zoom);
			double latitudeMin = OsmMercator.YToLat((yMax + 1) * Tile.SIZE, zoom);
			double latitudeMax = OsmMercator.YToLat(yMin * Tile.SIZE, zoom);

			int width = (xMax - xMin + 1) * Tile.SIZE;
			int height = (yMax - yMin + 1) * Tile.SIZE;

			fw.write(Utilities.prepareMapString(layerName + ".png", longitudeMin, longitudeMax,
					latitudeMin, latitudeMax, width, height));
			fw.close();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeWriter(fw);
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
	private void createDefaultSizedTiles(File setFolder) throws InterruptedException {
		int pixelValueX = 0;
		int pixelValueY = 0;

		Thread t = Thread.currentThread();
		for (int x = xMin; x <= xMax; x++) {
			pixelValueY = 0;
			for (int y = yMin; y <= yMax; y++) {
				if (t.isInterrupted())
					throw new InterruptedException();
				try {
					File fDest = new File(setFolder, layerName + "_" + pixelValueX * 256 + "_"
							+ pixelValueY * 256 + ".png");
					File fSource = (File) tilesInFileFormat.get("y" + y + "x" + x + ".png");
					if (fSource != null) {
						Utilities.fileCopy(fSource, fDest);
					} else {
						FileOutputStream fos = new FileOutputStream(fDest);
						BufferedImage emptyImage = new BufferedImage(256, 256,
								BufferedImage.TYPE_INT_ARGB);
						ImageIO.write(emptyImage, "png", fos);
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

	/**
	 * New experimental custom tile size algorithm implementation.
	 * 
	 * It creates each custom sized tile separately. Therefore each original
	 * tile (256x256) will be loaded and painted multiple times. Therefore this
	 * implementation needs much more CPU power as each original tile is loaded
	 * up to 4-6 times.
	 * 
	 * @param setFolder
	 * @throws InterruptedException
	 */
	private void createCustomSizedTiles(File setFolder) throws InterruptedException {
		Thread t = Thread.currentThread();

		// left upper point on the map in pixels
		// regarding the current zoom level
		int xStart = xMin * Tile.SIZE;
		int yStart = yMin * Tile.SIZE;

		// lower right point on the map in pixels
		// regarding the current zoom level
		int xEnd = xMax * Tile.SIZE + (Tile.SIZE - 1);
		int yEnd = yMax * Tile.SIZE + (Tile.SIZE - 1);

		int mergedWidth = xEnd - xStart;
		int mergedHeight = yEnd - yStart;

		if (tileSizeWidth > mergedWidth || tileSizeHeight > mergedHeight) {
			if (!tileSizeErrorNotified) {
				JOptionPane.showMessageDialog(null,
						"Tile size settings is too large: default of 256 will be used instead, ",
						"Information", JOptionPane.INFORMATION_MESSAGE);
				tileSizeErrorNotified = true;
			}
			createDefaultSizedTiles(setFolder);
			return;
		}

		// Absolute positions
		int xAbsPos = xStart;
		int yAbsPos = yStart;

		log.trace("tile size: " + tileSizeWidth + " * " + tileSizeHeight);
		log.trace("X: from " + xStart + " to " + xEnd);
		log.trace("Y: from " + yStart + " to " + yEnd);

		int yRelPos = 0;
		while (yAbsPos < yEnd) {
			int xRelPos = 0;
			xAbsPos = xStart;
			while (xAbsPos < xEnd) {
				if (t.isInterrupted())
					throw new InterruptedException();
				BufferedImage tileImage = new BufferedImage(tileSizeWidth, tileSizeHeight,
						BufferedImage.TYPE_3BYTE_BGR);
				Graphics2D graphics = tileImage.createGraphics();
				File fDest = new File(setFolder, layerName + "_" + xRelPos + "_" + yRelPos + ".png");
				log.trace("Creating tile " + fDest.getName());
				int xTile = xAbsPos / Tile.SIZE;
				int xTileOffset = -(xAbsPos % Tile.SIZE);

				for (int x = xTileOffset; x < tileSizeWidth; x += Tile.SIZE) {
					int yTile = yAbsPos / Tile.SIZE;
					int yTileOffset = -(yAbsPos % Tile.SIZE);
					for (int y = yTileOffset; y < tileSizeHeight; y += Tile.SIZE) {
						String tileFileName = "y" + yTile + "x" + xTile + ".png";
						log.trace("\t" + tileFileName + " x:" + xTileOffset + " y:" + yTileOffset);
						File fSource = (File) tilesInFileFormat.get(tileFileName);
						if (fSource != null) {
							try {
								BufferedImage orgTileImage = ImageIO.read(fSource);
								graphics.drawImage(orgTileImage, xTileOffset, yTileOffset,
										orgTileImage.getWidth(), orgTileImage.getHeight(), null);
							} catch (IOException e) {
								log.error("", e);
							}
						}
						yTile++;
						yTileOffset += Tile.SIZE;
					}
					xTile++;
					xTileOffset += Tile.SIZE;
				}
				try {
					graphics.dispose();
					ImageIO.write(tileImage, "png", fDest);
					setFiles.add(fDest.getName());
				} catch (IOException e) {
					log.error("Error writing tile image: ", e);
				}

				xRelPos += tileSizeWidth;
				xAbsPos += tileSizeWidth;
			}
			yRelPos += tileSizeHeight;
			yAbsPos += tileSizeHeight;
		}
	}

	/**
	 * Old algorithm - currently unused
	 * 
	 * @param setFolder
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unused")
	private void createCustomSizedTilesOld(File setFolder) throws InterruptedException {

		int mergedWidth = (xMax - xMin + 1) * 256;
		int mergedHeight = (yMax - yMin + 1) * 256;

		if (tileSizeWidth > mergedWidth || tileSizeHeight > mergedHeight) {
			if (!tileSizeErrorNotified) {
				JOptionPane.showMessageDialog(null,
						"Tile size settings is too large: default of 256 will be used instead, ",
						"Information", JOptionPane.INFORMATION_MESSAGE);
				tileSizeErrorNotified = true;
			}
			createDefaultSizedTiles(setFolder);
			return;
		}

		BufferedImage mergedImage = new BufferedImage(mergedWidth, mergedHeight,
				BufferedImage.TYPE_3BYTE_BGR);

		Graphics2D graphics = mergedImage.createGraphics();

		int offsetX = 0;
		int offsetY = 0;

		Thread t = Thread.currentThread();
		for (int y = yMin; y <= yMax; y++) {
			for (int x = xMin; x <= xMax; x++) {

				if (t.isInterrupted())
					throw new InterruptedException();

				File tileToMerge = (File) tilesInFileFormat.get("y" + y + "x" + x + ".png");

				if (tileToMerge != null) {
					try {
						graphics.drawImage(ImageIO.read(tileToMerge), null, offsetX * 256,
								offsetY * 256);
					} catch (IOException e) {
						log.error("Image loading failed!", e);
					}
				}
				offsetX++;
			}
			offsetX = 0;
			offsetY++;
		}
		graphics.dispose();

		List<SubImageProperties> cuttingPoints = new ArrayList<SubImageProperties>();

		int nrOfHorizontalTiles;
		int nrOfVerticalTiles;

		if (mergedWidth % tileSizeWidth == 0 && mergedHeight % tileSizeHeight == 0) {
			nrOfHorizontalTiles = mergedWidth / tileSizeWidth;
			nrOfVerticalTiles = mergedHeight / tileSizeHeight;

			for (int i = 0; i < nrOfVerticalTiles; i++) {
				for (int j = 0; j < nrOfHorizontalTiles; j++) {
					cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, i * tileSizeHeight,
							tileSizeWidth, tileSizeHeight));
				}
			}
		} else if (mergedWidth % tileSizeWidth == 0) {
			nrOfHorizontalTiles = mergedWidth / tileSizeWidth;
			nrOfVerticalTiles = mergedHeight / tileSizeHeight;

			for (int i = 0; i < nrOfVerticalTiles; i++) {
				for (int j = 0; j < nrOfHorizontalTiles; j++) {
					cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, i * tileSizeHeight,
							tileSizeWidth, tileSizeHeight));
				}
			}
			// Get the last rest row, since the mergeHeight is not, as an
			// integer dividable with tileSizeHeight
			int tileSizeRestHeight = mergedHeight - nrOfVerticalTiles * tileSizeHeight;

			for (int i = 0; i < 1; i++) {
				for (int j = 0; j < nrOfHorizontalTiles; j++) {
					cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, nrOfVerticalTiles
							* tileSizeHeight, tileSizeWidth, tileSizeRestHeight));
				}
			}
		} else if (mergedHeight % tileSizeHeight == 0) {
			nrOfHorizontalTiles = mergedWidth / tileSizeWidth;
			nrOfVerticalTiles = mergedHeight / tileSizeHeight;

			for (int i = 0; i < nrOfVerticalTiles; i++) {
				for (int j = 0; j < nrOfHorizontalTiles; j++) {
					cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, i * tileSizeHeight,
							tileSizeWidth, tileSizeHeight));
				}
			}
			// Get the last rest column, since the mergeWidth is not, as an
			// integer dividable with tileSizeWidth
			int tileSizeRestWidth = mergedWidth - nrOfHorizontalTiles * tileSizeWidth;

			for (int i = 0; i < 1; i++) {
				for (int j = 0; j < nrOfVerticalTiles; j++) {
					cuttingPoints.add(new SubImageProperties(nrOfHorizontalTiles * tileSizeWidth, j
							* tileSizeHeight, tileSizeRestWidth, tileSizeHeight));
				}
			}
		} else {
			nrOfHorizontalTiles = mergedWidth / tileSizeWidth;
			nrOfVerticalTiles = mergedHeight / tileSizeHeight;

			int tileSizeRestHeight = mergedHeight - nrOfVerticalTiles * tileSizeHeight;
			int tileSizeRestWidth = mergedWidth - nrOfHorizontalTiles * tileSizeWidth;

			for (int i = 0; i < nrOfVerticalTiles; i++) {
				for (int j = 0; j < nrOfHorizontalTiles; j++) {
					cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, i * tileSizeHeight,
							tileSizeWidth, tileSizeHeight));
				}
			}
			// Get the last rest row, since the mergeHeight is not, as an
			// integer dividable with tileSizeHeight
			for (int i = 0; i < 1; i++) {
				for (int j = 0; j < nrOfHorizontalTiles; j++) {
					cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, nrOfVerticalTiles
							* tileSizeHeight, tileSizeWidth, tileSizeRestHeight));
				}
				cuttingPoints.add(new SubImageProperties(nrOfHorizontalTiles * tileSizeWidth,
						nrOfVerticalTiles * tileSizeHeight, tileSizeRestWidth, tileSizeRestHeight));
			}
			// Get the last rest column, since the mergeWidth is not, as an
			// integer dividable with tileSizeWidth
			for (int i = 0; i < 1; i++) {
				for (int j = 0; j < nrOfVerticalTiles; j++) {
					cuttingPoints.add(new SubImageProperties(nrOfHorizontalTiles * tileSizeWidth, j
							* tileSizeHeight, tileSizeRestWidth, tileSizeHeight));
				}
			}
		}

		// Iterate through all cutting points and get the sub images from
		// the merged image and write
		// them to disk as new images.
		for (int i = 0; i < cuttingPoints.size(); i++) {
			SubImageProperties sip = cuttingPoints.get(i);

			int x = sip.getX();
			int y = sip.getY();
			int w = sip.getW();
			int h = sip.getH();

			BufferedImage buf = mergedImage.getSubimage(x, y, w, h);

			try {
				File fo = new File(setFolder, layerName + "_" + x + "_" + y + ".png");
				setFiles.add(fo.getName());
				FileOutputStream fos = new FileOutputStream(fo);
				ImageIO.write(buf, "png", fos);
				fos.close();
			} catch (IOException e) {
				log.error("Error saveing custom tile:", e);
			}
			buf = null;
		}
		mergedImage = null;
		cuttingPoints.clear();
	}

	private void writeSetFile() {
		// Create the set file for this map
		File setFile = new File(atlasLayerFolder, layerName + ".set");
		log.trace("Writing map .set file: " + setFile.getAbsolutePath());
		FileWriter fw = null;
		try {
			fw = new FileWriter(setFile);
			for (String file : setFiles) {
				fw.write(file + "\r\n");
			}
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeWriter(fw);
		}
	}
}