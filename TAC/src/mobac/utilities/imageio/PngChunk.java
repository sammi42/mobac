package mobac.utilities.imageio;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

class PngChunk extends DataOutputStream {

	final CRC32 crc;
	final ByteArrayOutputStream baos;

	PngChunk(int chunkType) throws IOException {
		this(chunkType, new ByteArrayOutputStream(), new CRC32());
	}

	private PngChunk(int chunkType, ByteArrayOutputStream baos, CRC32 crc) throws IOException {
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