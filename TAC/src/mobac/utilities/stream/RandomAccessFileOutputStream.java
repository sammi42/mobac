package mobac.utilities.stream;

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

	private final RandomAccessFile file;

	public RandomAccessFileOutputStream(RandomAccessFile f) {
		this.file = f;
	}

	@Override
	public void write(int b) throws IOException {
		file.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		file.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		file.write(b);
	}

	public RandomAccessFile getFile() {
		return file;
	}

}
