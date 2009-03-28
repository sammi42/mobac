/* 
 * Copyright (c) 2007 Matthias Mann - www.matthiasmann.de
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package tac.utilities;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * 4 Bit PNG Writer
 * <p>
 * Writes a color png image with pallette containing 16 colors. Currently the
 * image data is saved without any PNG filtering.
 * </p>
 * 
 * Bases on the PNGWriter written by Matthias Mann - www.matthiasmann.de
 */
public class Png4BitWriter {

	private static final byte[] SIGNATURE = { (byte) 137, 80, 78, 71, 13, 10, 26, 10 };
	private static final int IHDR = (int) 0x49484452;
	private static final int IDAT = (int) 0x49444154;
	private static final int IEND = (int) 0x49454E44;
	private static final int PLTE = (int) 0x504C5445;

	private static final byte COLOR_PALETTE = 3;

	private static final byte COMPRESSION_DEFLATE = 0;
	private static final byte INTERLACE_NONE = 0;

	public static final byte FILTER_NONE = 0;
	public static final byte FILTER_PAETH = 4;

	public static void writeImage(File file, BufferedImage image) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		try {
			writeImage(out, image);
		} finally {
			out.close();
		}
	}

	/**
	 * 
	 * @param out
	 * @param image
	 *            Must be an image with {@link ColorModel}
	 *            {@link IndexColorModel}
	 * @throws IOException
	 */
	public static void writeImage(OutputStream out, BufferedImage image) throws IOException {
		int[] pixels = null;
		pixels = image.getData().getPixels(0, 0, image.getWidth(), image.getHeight(), pixels);
		DataOutputStream dos = new DataOutputStream(out);

		int width = image.getWidth();
		int height = image.getHeight();

		ColorModel cm = image.getColorModel();

		if (!(cm instanceof IndexColorModel))
			throw new UnsupportedOperationException("Image format not compatible");

		IndexColorModel palette = (IndexColorModel) cm;

		dos.write(SIGNATURE);
		Chunk cIHDR = new Chunk(IHDR);
		cIHDR.writeInt(width);
		cIHDR.writeInt(height);
		cIHDR.writeByte(4); // 4 bit per component
		cIHDR.writeByte(COLOR_PALETTE);
		cIHDR.writeByte(COMPRESSION_DEFLATE);
		cIHDR.writeByte(FILTER_NONE);
		cIHDR.writeByte(INTERLACE_NONE);
		cIHDR.writeTo(dos);

		Chunk cPLTE = new Chunk(PLTE);
		int paletteEntries = palette.getMapSize();
		byte[] r = new byte[paletteEntries];
		byte[] g = new byte[paletteEntries];
		byte[] b = new byte[paletteEntries];
		palette.getReds(r);
		palette.getGreens(g);
		palette.getBlues(b);
		int colorCount = Math.min(paletteEntries, 16);
		for (int i = 0; i < colorCount; i++) {
			cPLTE.writeByte(r[i]);
			cPLTE.writeByte(g[i]);
			cPLTE.writeByte(b[i]);
		}
		cPLTE.writeTo(dos);

		Chunk cIDAT = new Chunk(IDAT);
		DeflaterOutputStream dfos = new DeflaterOutputStream(cIDAT, new Deflater(
				Deflater.BEST_COMPRESSION));

		int lineLen = MyMath.divCeil(width, 2);
		byte[] lineOut = new byte[lineLen];
		int[] samples = null;

		for (int line = 0; line < height; line++) {
			dfos.write(FILTER_NONE);

			// Get the samples for the next line - each byte is one sample/pixel
			samples = image.getRaster().getPixels(0, line, width, 1, samples);
			int sx = 0;
			for (int i = 0; i < samples.length; i += 2) {
				// Now we are packing two samples of 4 bit into one byte
				int sample1 = samples[i];
				int sample2 = samples[i + 1];
				int s1 = sample1 & 0x0F;
				int s2 = sample2 & 0x0F;
				if ((s1 != sample1) || (s2 != sample2))
					throw new RuntimeException("sample has more than 4 bit!");
				lineOut[sx++] = (byte) ((s1 << 4) | s2);
			}
			dfos.write(lineOut);
		}

		dfos.finish();
		cIDAT.writeTo(dos);

		Chunk cIEND = new Chunk(IEND);
		cIEND.writeTo(dos);

		dos.flush();
	}

	protected static void writeColor(DataOutputStream dos, Color c) throws IOException {
		dos.writeByte(c.getRed());
		dos.writeByte(c.getGreen());
		dos.writeByte(c.getBlue());
	}

	static class Chunk extends DataOutputStream {
		final CRC32 crc;
		final ByteArrayOutputStream baos;

		Chunk(int chunkType) throws IOException {
			this(chunkType, new ByteArrayOutputStream(), new CRC32());
		}

		private Chunk(int chunkType, ByteArrayOutputStream baos, CRC32 crc) throws IOException {
			super(new CheckedOutputStream(baos, crc));
			this.crc = crc;
			this.baos = baos;

			writeInt(chunkType);
		}

		public void writeTo(DataOutputStream out) throws IOException {
			flush();
			out.writeInt(baos.size() - 4);
			baos.writeTo(out);
			out.writeInt((int) crc.getValue());
		}
	}

}
