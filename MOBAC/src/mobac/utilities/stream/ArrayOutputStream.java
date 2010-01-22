package mobac.utilities.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A simple fixed-size version of {@link ByteArrayOutputStream}.
 */
public class ArrayOutputStream extends OutputStream {

	protected byte[] buf;

	protected int pos = 0;

	/**
	 * @param size
	 *            Size of the buffer available for writing.
	 */
	public ArrayOutputStream(int size) {
		buf = new byte[size];
	}

	/**
	 * @param array
	 *            Byte array used for writing to
	 */
	public ArrayOutputStream(byte[] array) {
		buf = array;
	}

	/**
	 * 
	 * @param array
	 *            Byte array used for writing to
	 * @param off
	 *            offset in <code>array</code>
	 */
	public ArrayOutputStream(byte[] array, int off) {
		buf = array;
		pos = off;
	}

	public byte[] toByteArray() {
		byte[] data = new byte[pos];
		System.arraycopy(buf, 0, data, 0, pos);
		return data;
	}

	public void reset() {
		pos = 0;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int newPos = pos + len;
		if (newPos > buf.length)
			throw new IOException("End of buffer reached");
		System.arraycopy(b, off, buf, pos, len);
		pos = newPos;
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(int b) throws IOException {
		throw new RuntimeException("Unsupported");
	}

}
