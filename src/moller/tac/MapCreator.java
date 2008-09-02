package moller.tac;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * @author Fredrik
 *
 */
public class MapCreator {

	public static void create(SubMapProperties smp, File oziFolder, File atlasFolder, String mapName, int zoom, String mapNumber, int tileSizeWidth, int tileSizeHeight) {
		
		String fileSeparator = System.getProperty("file.separator");
		
		int xMin = smp.getXMin();
		int xMax = smp.getXMax();
		int yMin = smp.getYMin();
		int yMax = smp.getYMax();
		
		File atlasContentFolder = new File(atlasFolder.getAbsolutePath() + fileSeparator + mapName + zoom + mapNumber);
		atlasContentFolder.mkdir();

		File mapFile = new File(atlasContentFolder.getAbsolutePath() + fileSeparator + mapName + zoom + mapNumber +".map");

		try {
			FileWriter fw = new FileWriter(mapFile);

			Rectangle2D.Double rect = null;
			
			rect = GoogleTileUtils.getTileRect(xMin, yMin, zoom);
			double longitudeMin =  rect.getMinX();
			
			rect = GoogleTileUtils.getTileRect(xMax, yMin, zoom);
			double longitudeMax = rect.getMaxX();
			
			rect = GoogleTileUtils.getTileRect(xMax, yMax, zoom);
			double latitudeMin = rect.getMinY();
			
			rect = GoogleTileUtils.getTileRect(xMax, yMin, zoom);
			double latitudeMax = rect.getMaxY();

			fw.write(Utilities.prepareMapString(mapName + zoom + mapNumber + ".png", longitudeMin, longitudeMax, latitudeMin, latitudeMax, (xMax - xMin + 1) * 256, (yMax - yMin + 1) * 256));
			fw.close();
		}
		catch (IOException iox) {
			System.out.println(iox);
		}

		// Create the set folder where all the tiles shall be stored.
		File setFolder = new File(atlasContentFolder.getAbsolutePath() + fileSeparator + "set");
		setFolder.mkdir();
	
		// List all tiles in the ozi folder.
		File [] tiles = oziFolder.listFiles();
		
		// Sort the files so they end up in alphabetical order.
		Arrays.sort(tiles);
		
		// Put all tiles in a Hash Map so the will be easy to access later on.
		HashMap<String, File> tilesInFileFormat = new HashMap<String, File>();
		
		for (int i = 0; i < tiles.length; i++) {
			tilesInFileFormat.put(tiles[i].getName(), tiles[i]);
		}
				
		// This means there should not be any resizing of the tiles.
		if (tileSizeWidth == 256 && tileSizeHeight == 256) {
			
			int pixelValueX = 0;
			int pixelValueY = 0;

			convertLoop:
				
			for (int y = yMin; y < yMax; y++) {
				for (int x = xMin; x < xMax; x++) {

					if (ProcessValues.getAbortAtlasDownload()) {
						break convertLoop;
					}else {
						try {
							Utilities.fileCopy((File)tilesInFileFormat.get("y" + y + "x" + x + ".png"), new File(setFolder.getAbsolutePath() + fileSeparator + mapName + zoom + mapNumber + "_" + pixelValueX * 256 + "_" + pixelValueY * 256 + ".png" ));

							if (pixelValueX != (xMax - xMin)) 
								pixelValueX++;
							else {
								pixelValueX = 0;
								pixelValueY++;
							}
						} catch (IOException iox) {
							System.out.println(iox);
						}	
					}
				}
			}
		} else {
						
			int mergedWidth  = (xMax - xMin + 1) * 256;
			int mergedHeight = (yMax - yMin + 1) * 256;
			
			if (tileSizeWidth > mergedWidth || tileSizeHeight > mergedHeight) {
				if (!ProcessValues.getTileSizeErrorNotified()) {
					JOptionPane.showMessageDialog(null, "Tile size settings is too large: default of 256 will be used instead, ", "Information", JOptionPane.INFORMATION_MESSAGE);
					ProcessValues.setTileSizeErrorNotified(true);
				}
				tileSizeWidth = 256;
				tileSizeHeight = 256;
			}
						
			BufferedImage mergedImage = new BufferedImage(mergedWidth, mergedHeight, BufferedImage.TYPE_3BYTE_BGR);
			
			Graphics2D graphics = mergedImage.createGraphics(); 
			
			int offsetX = 0;
			int offsetY = 0;
				
			for (int y = yMin; y <= yMax; y++) {
				for (int x = xMin; x <= xMax; x++) {
										
					File tileToMerge = (File)tilesInFileFormat.get("y" + y + "x" + x + ".png");
					
					if(tileToMerge != null) {
						try {
							graphics.drawImage(ImageIO.read(tileToMerge), null, offsetX * 256, offsetY * 256);
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
						cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, i * tileSizeHeight, tileSizeWidth, tileSizeHeight));			
					}
				}
			} else if(mergedWidth % tileSizeWidth == 0) {
				nrOfHorizontalTiles = mergedWidth / tileSizeWidth;
				nrOfVerticalTiles = mergedHeight / tileSizeHeight;
				
				for (int i = 0; i < nrOfVerticalTiles; i++) {
					for (int j = 0; j < nrOfHorizontalTiles; j++) {
						cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, i * tileSizeHeight, tileSizeWidth, tileSizeHeight));			
					}
				}
				// Get the last rest row, since the mergeHeight is not, as an integer dividable with tileSizeHeight
				int tileSizeRestHeight = mergedHeight - nrOfVerticalTiles * tileSizeHeight;
				
				for (int i = 0; i < 1; i++) {
					for (int j = 0; j < nrOfHorizontalTiles; j++) {
						cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, nrOfVerticalTiles * tileSizeHeight, tileSizeWidth, tileSizeRestHeight));			
					}
				}
			} else if(mergedHeight % tileSizeHeight == 0) {
				nrOfHorizontalTiles = mergedWidth / tileSizeWidth;
				nrOfVerticalTiles = mergedHeight / tileSizeHeight;
				
				for (int i = 0; i < nrOfVerticalTiles; i++) {
					for (int j = 0; j < nrOfHorizontalTiles; j++) {
						cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, i * tileSizeHeight, tileSizeWidth, tileSizeHeight));			
					}
				}
				// Get the last rest column, since the mergeWidth is not, as an integer dividable with tileSizeWidth   
				int tileSizeRestWidth = mergedWidth - nrOfHorizontalTiles * tileSizeWidth;
				
				for (int i = 0; i < 1; i++) {
					for (int j = 0; j < nrOfVerticalTiles; j++) {
						cuttingPoints.add(new SubImageProperties(nrOfHorizontalTiles * tileSizeWidth, j * tileSizeHeight, tileSizeRestWidth, tileSizeHeight));			
					}
				}
			} else {
				nrOfHorizontalTiles = mergedWidth / tileSizeWidth;
				nrOfVerticalTiles = mergedHeight / tileSizeHeight;
				
				int tileSizeRestHeight = mergedHeight - nrOfVerticalTiles * tileSizeHeight;
				int tileSizeRestWidth = mergedWidth - nrOfHorizontalTiles * tileSizeWidth;
				
				for (int i = 0; i < nrOfVerticalTiles; i++) {
					for (int j = 0; j < nrOfHorizontalTiles; j++) {
						cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, i * tileSizeHeight, tileSizeWidth, tileSizeHeight));			
					}
				}
				// Get the last rest row, since the mergeHeight is not, as an integer dividable with tileSizeHeight
				for (int i = 0; i < 1; i++) {
					for (int j = 0; j < nrOfHorizontalTiles; j++) {
						cuttingPoints.add(new SubImageProperties(j * tileSizeWidth, nrOfVerticalTiles * tileSizeHeight, tileSizeWidth, tileSizeRestHeight));			
					}
					cuttingPoints.add(new SubImageProperties(nrOfHorizontalTiles * tileSizeWidth, nrOfVerticalTiles * tileSizeHeight, tileSizeRestWidth, tileSizeRestHeight));
				}
				// Get the last rest column, since the mergeWidth is not, as an integer dividable with tileSizeWidth   
				for (int i = 0; i < 1; i++) {
					for (int j = 0; j < nrOfVerticalTiles; j++) {
						cuttingPoints.add(new SubImageProperties(nrOfHorizontalTiles * tileSizeWidth, j * tileSizeHeight, tileSizeRestWidth, tileSizeHeight));			
					}
				}
			}
						
			// Iterate through all cutting points and get the sub images from the merged image and write 
			// them to disk as new images.
			for (int i = 0; i < cuttingPoints.size(); i ++){
				SubImageProperties sip = cuttingPoints.get(i);
				
				int x = sip.getX();
				int y = sip.getY();
				int w = sip.getW();
				int h = sip.getH();
								
				BufferedImage buf = mergedImage.getSubimage(x, y, w, h);
			
				try {
					FileOutputStream fos = new FileOutputStream(setFolder + fileSeparator + mapName + zoom + mapNumber + "_" + x + "_" + y + ".png");
					ImageIO.write(buf, "png", fos);
					fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buf = null;
			}	
			mergedImage = null;
			cuttingPoints.clear();
		}
	
		// Create the set file for this map
		File setFile = new File(atlasContentFolder.getAbsolutePath() + fileSeparator + mapName + zoom + mapNumber + ".set");
		createSetFile(setFile, setFolder);
	}
	
	/**
	 * This method writes a set file for a map. This file contains a list over 
	 * all tiles that exists in this map.
	 * 
	 * @param setFile is a Java File object that points to the place where the file
	 *                shall be created.
	 * @param  setFolder is the folder where all the tiles that shall be listed 
	 *         in the set file is stored               
	 */
	private static void createSetFile(File setFile, File setFolder) {
		
		File [] setTiles = setFolder.listFiles();

		try {
			FileWriter fw = new FileWriter(setFile);

			for (int i = 0; i < setTiles.length; i++) {
				fw.write(setTiles[i].getName() + "\r\n");
			}
			fw.close();
		} catch (IOException iox) {
			System.out.println(iox);
		}		
	}
}