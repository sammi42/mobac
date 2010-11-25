/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.program.atlascreators;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.utilities.Utilities;
import mobac.utilities.stream.ArrayOutputStream;
import mobac.utilities.tar.TarIndex;

/**
 * Creates maps using the MGM pack file format (.mgm).
 * 
 * Each zoom level in a different directory, 64 tiles per mgm file.
 * 
 * @author paour
 */
public class MGM extends AtlasCreator {

	private static final int TILES_PER_FILE = 64;
	private static final int TILES_PER_FILE_X = 8;

	private double xResizeRatio = 1.0;
	private double yResizeRatio = 1.0;

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws AtlasTestException, IOException,
			InterruptedException {
		super.startAtlasCreation(atlas, customAtlasDir);

		File cache_conf = new File(atlasDir, "cache.conf");
		PrintWriter pw = new PrintWriter(new FileWriter(cache_conf));
		try {
			pw.println("version=3");
			pw.println("tiles_per_file=" + TILES_PER_FILE);
			pw.println("hash_size=1");
		} finally {
			pw.close();
		}
	}

	@Override
	public void initializeMap(final MapInterface map, final TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);

		xResizeRatio = 1.0;
		yResizeRatio = 1.0;

		if (parameters != null) {
			int mapTileSize = map.getMapSource().getMapSpace().getTileSize();
			if ((parameters.getWidth() != mapTileSize) || (parameters.getHeight() != mapTileSize)) {
				// handle image re-sampling + image re-sizing
				xResizeRatio = (double) parameters.getWidth() / (double) mapTileSize;
				yResizeRatio = (double) parameters.getHeight() / (double) mapTileSize;
			} else {
				// handle only image re-sampling
				mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, parameters.getFormat());
			}
		}
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			StringBuilder name = new StringBuilder(map.getLayer().getName());

			// safe name
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				if ((c < '0' || c > '9') && (c < 'A' || c > 'Z') && (c < 'a' || c > 'z')) {
					name.setCharAt(i, '_');
				}
			}

			// crate directory if necessary
			File folder = new File(atlasDir, name + "_" + map.getZoom());
			folder.mkdirs();

			atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));

			// number of tiles for this zoom level
			// final long nbTotalTiles = (256 * Math.round(Math.pow(2, map.getZoom()))) / tileSize;

			// tile resizing
			BufferedImage tileImage = null;
			Graphics2D graphics = null;
			ArrayOutputStream buffer = null;
			TileImageDataWriter writer = null;
			boolean resizeImage = false;

			if ((xResizeRatio != 1.0) || (yResizeRatio != 1.0)) {
				// resize image
				tileImage = new BufferedImage(parameters.getWidth(), parameters.getHeight(),
						BufferedImage.TYPE_3BYTE_BGR);

				// associated graphics with affine transform
				graphics = tileImage.createGraphics();
				graphics.setTransform(AffineTransform.getScaleInstance(xResizeRatio, yResizeRatio));
				graphics
						.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

				// image compression writer
				writer = parameters.getFormat().getDataWriter();

				// buffer to store compressed image
				buffer = new ArrayOutputStream(3 * parameters.getWidth() * parameters.getHeight());
				resizeImage = true;
			}

			ImageIO.setUseCache(false);

			int pxMin = xMin / TILES_PER_FILE_X;
			int pxMax = xMax / TILES_PER_FILE_X;
			int pyMin = yMin / TILES_PER_FILE_X;
			int pyMax = xMax / TILES_PER_FILE_X;

			byte[] buf = new byte[32768];

			for (int px = pxMin; px <= pxMax; px++) {
				for (int py = pyMin; py <= pyMax; py++) {
					int count = 0;
					int pos = 2 + TILES_PER_FILE * 6;
					File tmp = null;
					BufferedOutputStream bostmp = null;
					RandomAccessFile raf = null;
					BufferedInputStream bis = null;
					try {
						File pack = new File(folder, px + "_" + py + ".mgm");
						tmp = new File(folder, px + "_" + py + ".mgm.tmp");
						raf = new RandomAccessFile(pack, "rw");
						bostmp = new BufferedOutputStream(new FileOutputStream(tmp));

						// write a temporary count
						raf.writeChar(TILES_PER_FILE);

						for (int i = 0; i < TILES_PER_FILE_X; i++) {
							int x = px * TILES_PER_FILE_X + i;
							if (x < xMin || x > xMax) {
								continue;
							}

							for (int j = 0; j < TILES_PER_FILE_X; j++) {
								int y = py * TILES_PER_FILE_X + j;
								if (y < yMin || y > yMax) {
									continue;
								}

								checkUserAbort();

								atlasProgress.incMapCreationProgress();

								try {
									// retrieve the tile data (already re-sampled if needed)
									final BufferedImage tile = mapDlTileProvider.getTileImage(x, y);

									byte[] sourceTileData;
									if (resizeImage) {
										// need to resize the tile
										graphics.drawImage(tile, 0, 0, null);

										buffer.reset();

										writer.initialize();
										writer.processImage(tileImage, buffer);

										sourceTileData = buffer.toByteArray();

										if (sourceTileData == null)
											throw new MapCreationException("Image resizing failed.");
									} else {
										sourceTileData = mapDlTileProvider.getTileData(x, y);
									}
									if (sourceTileData != null) {
										bostmp.write(sourceTileData);
										count++;

										raf.writeByte(i);
										raf.writeByte(j);
										pos += sourceTileData.length;
										raf.writeInt(pos);
									}
								} catch (IOException e) {
									throw new MapCreationException("Error writing tile image: " + e.getMessage(), e);
								}
							}
						}

						bostmp.close();
						bostmp = null;

						if (count != TILES_PER_FILE) {
							raf.seek(0);
							raf.writeChar(count);
						}

						raf.seek(2 + TILES_PER_FILE * 6);

						bis = new BufferedInputStream(new FileInputStream(tmp));
						int len;
						while ((len = bis.read(buf)) != -1) {
							raf.write(buf, 0, len);
						}

					} finally {
						Utilities.closeStream(bis);
						Utilities.closeFile(raf);
						Utilities.closeStream(bostmp);
						tmp.delete();
					}
				}
				if (graphics != null)
					graphics.dispose();
			}
		} catch (Exception e) {
			throw new MapCreationException(e);
		}
	}

	@Override
	public boolean testMapSource(final MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace)
				&& (ProjectionCategory.SPHERE.equals(mapSource.getMapSpace().getProjectionCategory()) || ProjectionCategory.ELLIPSOID
						.equals(mapSource.getMapSpace().getProjectionCategory()));
	}

}
