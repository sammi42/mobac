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

import java.awt.image.BufferedImage;
import java.io.IOException;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.TileImageFormat;
import mobac.program.tiledatawriter.TileImageJpegDataWriter;
import mobac.utilities.stream.ArrayOutputStream;

public class GarminCustom extends GoogleEarthOverlay {

	/**
	 * Each jpeg should be less than 3MB. https://forums.garmin.com/showthread.php?t=2646
	 */
	private static final int MAX_FILE_SIZE = 3 * 1024 * 1024;

	@Override
	protected void testAtlas() throws AtlasTestException {
		for (LayerInterface layer : atlas) {
			if (layer.getMapCount() > 100)
				throw new AtlasTestException("Layer exceeeds the maximum map count of 100", layer);

			for (MapInterface map : layer) {
				if (map.getParameters() == null)
					continue;
				TileImageFormat format = map.getParameters().getFormat();
				if (!(format.getDataWriter() instanceof TileImageJpegDataWriter))
					throw new AtlasTestException("Only JPEG tile format is supported by this atlas format!", map);
			}
		}
	}

	@Override
	protected void writeTileImage(BufferedImage tileImage) throws MapCreationException {
		try {
			TileImageJpegDataWriter writer;
			if (parameters != null) {
				writer = (TileImageJpegDataWriter) parameters.getFormat().getDataWriter();
				writer = new TileImageJpegDataWriter(writer);
			} else
				writer = new TileImageJpegDataWriter(0.9);

			writer.initialize();
			// The maximum file size for the jpg image is 3 MB
			// This OutputStream will fail if the resulting image is larger than
			// 3 MB - then we retry using a higher JPEG compression level
			ArrayOutputStream buf = new ArrayOutputStream(MAX_FILE_SIZE);
			byte[] data = null;
			for (int c = 99; c > 50; c -= 5) {
				buf.reset();
				try {
					writer.processImage(tileImage, buf);
					data = buf.toByteArray();
					break;
				} catch (IOException e) {
					log.trace("Image size too large, increasing compression to " + c);
				}
				writer.setJpegCompressionLevel(c / 100f);
			}
			if (data == null)
				throw new MapCreationException("Unable to create an image with less than 3 MB!");
			String imageFileName = "files/" + cleanedMapName + "." + writer.getFileExt();
			kmzOutputStream.writeStoredEntry(imageFileName, data);
			addMapToKmz(imageFileName);
		} catch (Exception e) {
			throw new MapCreationException(e);
		}
	}

	@Override
	protected int getMaxImageSize() {
		return 1024;
	}
}
