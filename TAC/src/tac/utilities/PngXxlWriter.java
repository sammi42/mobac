package tac.utilities;

/*
 * PNGWriter.java
 *
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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.BufferedOutputStream;
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

import javax.activation.UnsupportedDataTypeException;
import javax.imageio.ImageIO;

/**
 * A small PNG writer to save RGB data.
 * 
 * @author Matthias Mann
 */
public class PngXxlWriter {

	private static final int BUFFER_SIZE = 128 * 1024;

	private static final byte[] SIGNATURE = { (byte) 137, 80, 78, 71, 13, 10, 26, 10 };
	private static final int IHDR = (int) 0x49484452;
	private static final int IDAT = (int) 0x49444154;
	private static final int IEND = (int) 0x49454E44;
	private static final byte COLOR_TRUECOLOR = 2;
	private static final byte COMPRESSION_DEFLATE = 0;
	private static final byte FILTER_SET_1 = 0;
	private static final byte FILTER_NONE = 0;
	private static final byte INTERLACE_NONE = 0;

	// private static final byte FILTER_TYPE_PAETH = 4;

	/**
	 * Writes an image in OpenGL GL_RGB format to an OutputStream.
	 * 
	 * @param os
	 *            The output stream where the PNG should be written to
	 * @param image
	 */
	public static void write(OutputStream os, BufferedImage image) throws IOException {
		int height = image.getHeight();
		int width = image.getWidth();
		DataOutputStream dos = new DataOutputStream(os);

		ColorModel cm = image.getColorModel();

		System.out.println(cm.getClass());
		System.out.println(image.getSampleModel());

		dos.write(SIGNATURE);

		Chunk cIHDR = new Chunk(IHDR);
		cIHDR.writeInt(width);
		cIHDR.writeInt(height);
		cIHDR.writeByte(8); // 8 bit per component
		cIHDR.writeByte(COLOR_TRUECOLOR);
		cIHDR.writeByte(COMPRESSION_DEFLATE);
		cIHDR.writeByte(FILTER_SET_1);
		cIHDR.writeByte(INTERLACE_NONE);
		cIHDR.writeTo(dos);

		int lineLen = width * 3;
		byte[] lineOut = new byte[lineLen];
		byte[] curLine = new byte[lineLen];
		byte[] prevLine = new byte[lineLen];
		// System.out.println("lineLength: " + lineLen);

		Rectangle rect = new Rectangle(0, 0, width, 1);
		ImageDataChunkWriter idcw = new ImageDataChunkWriter(dos);

		DataOutputStream imageDataStream = idcw.getStream();

		for (int line = 0; line < height; line++) {

			rect.y = line;
			DataBuffer db = image.getData(rect).getDataBuffer();
			if (db.getNumBanks() > 1)
				throw new UnsupportedDataTypeException("Image data has more than one data bank");
			if (db instanceof DataBufferByte)
				curLine = ((DataBufferByte) db).getData();
			else
				throw new UnsupportedDataTypeException(db.getClass().getName());

			lineOut = curLine;
			/*
			 * lineOut[0] = (byte) (curLine[0] - prevLine[0]); lineOut[1] =
			 * (byte) (curLine[1] - prevLine[1]); lineOut[2] = (byte)
			 * (curLine[2] - prevLine[2]);
			 * 
			 * for (int x = 3; x < lineLen; x++) { int a = curLine[x - 3] & 255;
			 * int b = prevLine[x] & 255; int c = prevLine[x - 3] & 255; int p =
			 * a + b - c; int pa = p - a; if (pa < 0) pa = -pa; int pb = p - b;
			 * if (pb < 0) pb = -pb; int pc = p - c; if (pc < 0) pc = -pc; if
			 * (pa <= pb && pa <= pc) c = a; else if (pb <= pc) c = b;
			 * lineOut[x] = (byte) (curLine[x] - c); }
			 */

			imageDataStream.write(FILTER_NONE);
			imageDataStream.write(lineOut);

			// swap the line buffers
			byte[] temp = curLine;
			curLine = prevLine;
			prevLine = temp;
		}

		idcw.finish();

		Chunk cIEND = new Chunk(IEND);
		cIEND.writeTo(dos);

		dos.flush();
	}

	static class ImageDataChunkWriter extends OutputStream {

		DeflaterOutputStream dfos;
		DataOutputStream stream;
		DataOutputStream out;
		CRC32 crc = new CRC32();

		public ImageDataChunkWriter(DataOutputStream out) throws IOException {
			this.out = out;
			dfos = new DeflaterOutputStream(new BufferedOutputStream(this, BUFFER_SIZE),
					new Deflater(Deflater.BEST_COMPRESSION));
			stream = new DataOutputStream(dfos);
		}

		public DataOutputStream getStream() {
			return stream;
		}

		public void finish() throws IOException {
			stream.flush();
			stream.close();
			dfos.finish();
			dfos = null;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			crc.reset();
			out.writeInt(len);
			out.writeInt(IDAT);
			out.write(b, off, len);
			crc.update("IDAT".getBytes());
			crc.update(b, off, len);
			out.writeInt((int) crc.getValue());
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(int b) throws IOException {
			throw new IOException("Simgle byte writing not supported");
		}
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

	public static void main(String[] args) {
		try {
			BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
			image = ImageIO.read(new File("D:/109385_6023.jpg"));
			PngXxlWriter.write(new FileOutputStream("D:/Test.png"), image);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}