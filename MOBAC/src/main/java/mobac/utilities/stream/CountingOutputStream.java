package mobac.utilities.stream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CountingOutputStream extends FilterOutputStream {

	private long bytesWritten = 0;

	public CountingOutputStream(OutputStream out) {
		super(out);
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
		bytesWritten++;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		bytesWritten += len;
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
		bytesWritten += b.length;
	}

	public long getBytesWritten() {
		return bytesWritten;
	}

}
