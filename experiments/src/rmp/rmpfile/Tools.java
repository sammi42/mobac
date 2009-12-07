/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package rmp.rmpfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Tools {

	/**
	 * Copies the given value into the stream as binary value, with least
	 * significant byte first.
	 * 
	 * @param stream
	 *            stream to write to
	 * @param value
	 *            value to write
	 * @param length
	 *            number of bytes to write to stream
	 * @throws IOException
	 */
	public static void writeValue(OutputStream stream, int value, int length) throws IOException {
		int i;

		for (i = 0; i < length; i++) {
			stream.write(value & 0xFF);
			value >>= 8;
		}
	}

	/**
	 * Writes the given string into the stream. The strign is written with a
	 * fixed length. If the length is longer than the string, then 00 bytes are
	 * written
	 * 
	 * @param stream
	 *            stream to write to
	 * @param str
	 *            strign to write
	 * @param length
	 *            number of bytes to write
	 * @throws IOException
	 */
	public static void writeFixedString(OutputStream stream, String str, int length)
			throws IOException {
		int i;
		int value;

		for (i = 0; i < length; i++) {
			if (i < str.length())
				value = str.charAt(i);
			else
				value = 0;

			stream.write(value);
		}
	}

	/**
	 * Write a double value into a byte array
	 * 
	 * @param os
	 *            stream to write to
	 * @param value
	 *            value to write
	 */
	public static void writeDouble(OutputStream os, double value) throws IOException {
		ByteArrayOutputStream bo;
		DataOutputStream dos;
		byte[] b;
		byte help;

		/* --- Convert the value into a byte array --- */
		bo = new ByteArrayOutputStream();
		dos = new DataOutputStream(bo);

		/* --- Convert the value into a 8 byte double --- */
		dos.writeDouble(value);
		dos.close();
		b = bo.toByteArray();

		/* --- Change byte order --- */
		for (int i = 0; i < 4; i++) {
			help = b[i];
			b[i] = b[7 - i];
			b[7 - i] = help;
		}

		/* --- Write result into output stream --- */
		os.write(b);
	}
}
