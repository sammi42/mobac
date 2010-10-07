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
package mobac.program.tiledatawriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import mobac.program.interfaces.TileImageDataWriter;

import org.apache.log4j.Logger;

public class TileImageJpegDataWriter implements TileImageDataWriter {

	protected Logger log;

	protected ImageWriter jpegImageWriter = null;

	protected ImageWriteParam iwp = null;

	protected float jpegCompressionLevel;

	/**
	 * 
	 * @param jpegCompressionLevel
	 *            a float between 0 and 1; 1 specifies minimum compression and maximum quality
	 */
	public TileImageJpegDataWriter(double jpegCompressionLevel) {
		this((float) jpegCompressionLevel);
	}

	public TileImageJpegDataWriter(float jpegCompressionLevel) {
		this.jpegCompressionLevel = (float) jpegCompressionLevel;
		log = Logger.getLogger(this.getClass());
	}

	public TileImageJpegDataWriter(TileImageJpegDataWriter jpegWriter) {
		this(jpegWriter.getJpegCompressionLevel());
	}

	public void initialize() {
		if (log.isTraceEnabled()) {
			String s = "Available JPEG image writers:";
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
			while (writers.hasNext()) {
				ImageWriter w = writers.next();
				s += "\n\t" + w.getClass().getName();
			}
			log.trace(s);
		}
		jpegImageWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
		jpegImageWriter.addIIOWriteWarningListener(ImageWriterWarningListener.INSTANCE);
		log.debug("Used JPEG image writer: " + jpegImageWriter.getClass().getName());
		iwp = jpegImageWriter.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(jpegCompressionLevel);
	}

	public void setJpegCompressionLevel(float jpegCompressionLevel) {
		this.jpegCompressionLevel = jpegCompressionLevel;
		iwp.setCompressionQuality(jpegCompressionLevel);
	}

	public float getJpegCompressionLevel() {
		return jpegCompressionLevel;
	}

	public void processImage(BufferedImage image, OutputStream out) throws IOException {
		jpegImageWriter.setOutput(ImageIO.createImageOutputStream(out));
		IIOImage ioImage = new IIOImage(image, null, null);
		jpegImageWriter.write(null, ioImage, iwp);
	}

	public void dispose() {
		jpegImageWriter.dispose();
		jpegImageWriter = null;
	}

	public String getFileExt() {
		return "jpg";
	}

}
