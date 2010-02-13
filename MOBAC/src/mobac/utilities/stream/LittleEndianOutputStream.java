package mobac.utilities.stream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LittleEndianOutputStream extends FilterOutputStream {

	public LittleEndianOutputStream(OutputStream out) {
		super(out);
	}

	/**
	 * Write an int, 32-bits. Like DataOutputStream.writeInt.
	 * 
	 * @param v
	 *            the int to write
	 * 
	 * @throws IOException
	 *             if write fails.
	 */
	public final void writeInt(int v) throws IOException {
		byte[] work = new byte[4];
		work[0] = (byte) (v & 0xFF);
		work[1] = (byte) ((v >> 8) & 0xFF);
		work[2] = (byte) ((v >> 16) & 0xFF);
		work[3] = (byte) ((v >> 24) & 0xFF);
		out.write(work, 0, 4);
	}

	/**
	 * Write a double.
	 * 
	 * @param v
	 *            the double to write. Like DataOutputStream.writeDouble.
	 * 
	 * @throws IOException
	 *             if write fails.
	 */
	public final void writeDouble(double v) throws IOException {
		writeLong(Double.doubleToLongBits(v));
	}

	/**
	 * Write a long, 64-bits. like DataOutputStream.writeLong.
	 * 
	 * @param v
	 *            the long to write
	 * 
	 * @throws IOException
	 *             if write fails.
	 */
	public final void writeLong(long v) throws IOException {
		byte[] work = new byte[8];
		work[0] = (byte) v;
		work[1] = (byte) (v >> 8);
		work[2] = (byte) (v >> 16);
		work[3] = (byte) (v >> 24);
		work[4] = (byte) (v >> 32);
		work[5] = (byte) (v >> 40);
		work[6] = (byte) (v >> 48);
		work[7] = (byte) (v >> 56);
		out.write(work);
	}

}
