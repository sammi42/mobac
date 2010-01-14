/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package tac.program.atlascreators.impl.rmp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import tac.exceptions.MapCreationException;
import tac.program.atlascreators.impl.rmp.interfaces.CalibratedImage;

/**
 * Content of a single tile
 * 
 */
public class Tiledata {

	public int posx;
	public int posy;
	public int totalOffset;
	public CalibratedImage si;

	public BoundingRect rect;

	private int dataSize = 0;

	public int getTileDataSize() {
		return dataSize;
	}

	public void writeTileData(OutputStream out) throws IOException {
		try {
			BufferedImage image = si.getSubImage(rect, 256, 256);
			ByteArrayOutputStream bout = new ByteArrayOutputStream(16384);
			ImageIO.write(image, "jpg", bout);
			byte[] data = bout.toByteArray();
			dataSize = data.length;
			RmpTools.writeValue(out, dataSize, 4);
			out.write(data);
		} catch (MapCreationException e) {
			throw new IOException(e.getCause());
		}
	}


}
