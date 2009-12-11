package tac.utilities.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Allows to write to an {@link RandomAccessFile} through an
 * {@link OutputStream}.
 * 
 * <p>
 * Notes:
 * <ul>
 * <li>Closing this stream does not have any effect on the underlying
 * {@link RandomAccessFile}.</li>
 * <li>Seeking or changing the {@link RandomAccessFile} file position directly
 * affects {@link RandomAccessFileOutputStream}.</li>
 * </ul>
 * 
 */
public class RandomAccessFileOutputStream extends OutputStream {

	private final RandomAccessFile f;

	public RandomAccessFileOutputStream(RandomAccessFile f) {
		this.f = f;
	}

	@Override
	public void write(int b) throws IOException {
		f.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		f.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		f.write(b);
	}

}
