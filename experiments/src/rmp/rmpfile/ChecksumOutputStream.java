/* ------------------------------------------------------------------------

   CheckSumStream.java

   Project: Testing

  --------------------------------------------------------------------------*/

/* ---
 created: 15.08.2008 a.sander

 $History:$
 --- */

package rmp.rmpfile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream that calculates a 2Byte XOR checksum over the stream written
 * 
 */
public class ChecksumOutputStream extends OutputStream {
	private OutputStream nextStream;
	int checksum;
	boolean evenByte;

	/**
	 * Constructor
	 * 
	 * @param next_stream
	 *            stream to write data to
	 */
	public ChecksumOutputStream(OutputStream next_stream) {
		nextStream = next_stream;
		checksum = 0;
		evenByte = true;
	}

	/**
	 * Resets the checksum to 0
	 */
	public void clearChecksum() {
		checksum = 0;
		evenByte = true;
	}

	/**
	 * Returns the current checksum
	 */
	public int getChecksum() {
		return checksum;
	}

	/**
	 * Sets the current checksum
	 */
	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}

	/**
	 * Writes the current checksum (2 bytes) to the output stream
	 */
	public void writeChecksum() throws IOException {
		nextStream.write((checksum >> 8) & 0xFF);
		nextStream.write(checksum & 0xFF);
	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		int value;

		for (int i = arg1; i < arg2; i++) {
			if (evenByte)
				value = (((int) arg0[i]) & 0xFF) << 8;
			else
				value = (((int) arg0[i]) & 0xFF);

			checksum ^= value;
			evenByte = !evenByte;
		}

		/* --- Send to next stream --- */
		nextStream.write(arg0, arg1, arg2);
	}

	@Override
	public void write(byte[] arg0) throws IOException {
		int value;

		for (int i = 0; i < arg0.length; i++) {
			if (evenByte)
				value = (((int) arg0[i]) & 0xFF) << 8;
			else
				value = (((int) arg0[i]) & 0xFF);

			checksum ^= value;
			evenByte = !evenByte;
		}

		/* --- Send to next stream --- */
		nextStream.write(arg0);
	}

	@Override
	public void write(int arg0) throws IOException {
		int value;

		if (evenByte)
			value = (arg0 & 0xFF) << 8;
		else
			value = (arg0 & 0xFF);

		checksum ^= value;
		evenByte = !evenByte;

		/* --- Send to next stream --- */
		nextStream.write(arg0);
	}

	@Override
	public void close() throws IOException {
		nextStream.close();
	}

	@Override
	public void flush() throws IOException {
		nextStream.flush();
	}

}
