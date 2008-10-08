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

import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;

import tac.utilities.Utilities;

/**
 * @author Fredrik
 * 
 */
public class MapCreator {

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
		File mapFile = new File(atlasLayerFolder, layerName + ".map");

		FileWriter fw = null;
		try {
			fw = new FileWriter(mapFile);

			double longitudeMin = OsmMercator.XToLon(xMin * Tile.SIZE, zoom);
			double longitudeMax = OsmMercator.XToLon(xMax * Tile.SIZE, zoom);
			double latitudeMin = OsmMercator.YToLat(yMax * Tile.SIZE, zoom);
			double latitudeMax = OsmMercator.YToLat(yMin * Tile.SIZE, zoom);

			int width = (xMax - xMin) * Tile.SIZE;
			int height = (yMax - yMin) * Tile.SIZE;

			fw.write(Utilities.prepareMapString(layerName + ".png", longitudeMin, longitudeMax,
					latitudeMin, latitudeMax, width, height));
			fw.close();
		} catch (IOException iox) {
			iox.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (Exception e) {
			}
		}
	}

	private void createDefaultSizedTiles(File setFolder) throws InterruptedException {
		int pixelValueX = 0;
		int pixelValueY = 0;

		Thread t = Thread.currentThread(); 
		for (int x = xMin; x < xMax; x++) {
			pixelValueY = 0;
			for (int y = yMin; y < yMax; y++) {
				if (t.isInterrupted())
					throw new InterruptedException();
				try {
					File fDest = new File(setFolder, layerName + "_" + pixelValueX * 256 + "_"
							+ pixelValueY * 256 + ".png");
					File fSource = (File) tilesInFileFormat.get("y" + y + "x" + x + ".png");
					setFiles.add(fDest.getName());
					Utilities.fileCopy(fSource, fDest);
				} catch (IOException iox) {
					iox.printStackTrace();
				}
				pixelValueY++;
			}
			pixelValueX++;
		}
	}

	private void createCustomSizedTiles(File setFolder) throws InterruptedException {

		// TODO XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx
		if (1 == 1)
			throw new RuntimeException("Not working!");

		int mergedWidth = (xMax - xMin) * 256;
		int mergedHeight = (yMax - yMin) * 256;

		if (tileSizeWidth > mergedWidth || tileSizeHeight > mergedHeight) {
			if (!ProcessValues.getTileSizeErrorNotified()) {
				JOptionPane.showMessageDialog(null,
						"Tile size settings is too large: default of 256 will be used instead, ",
						"Information", JOptionPane.INFORMATION_MESSAGE);
				ProcessValues.setTileSizeErrorNotified(true);
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
						e.printStackTrace();
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
				e.printStackTrace();
			}
			buf = null;
		}
		mergedImage = null;
		cuttingPoints.clear();
	}

	private void writeSetFile() {
		// Create the set file for this map
		File setFile = new File(atlasLayerFolder, layerName + ".set");
		FileWriter fw = null;
		try {
			fw = new FileWriter(setFile);
			for (String file : setFiles) {
				fw.write(file + "\r\n");
			}
		} catch (IOException iox) {
			iox.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (Exception e) {
			}
		}
	}
}