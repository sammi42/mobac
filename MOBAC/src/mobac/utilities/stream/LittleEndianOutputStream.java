package mobac.utilities.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LittleEndianOutputStream extends FilterOutputStream {

	public LittleEndianOutputStream(OutputStream out) {
		super(out);
	}

	public void writeInt(int value) throws IOException {
		for (int i = 0; i < 4; i++) {
			out.write(value & 0xFF);
			value >>= 8;
		}
	}
	
	public void writeDouble(double value) throws IOException {
		

		/* --- Convert the value into a byte array --- */
		ByteArrayOutputStream bo = new ByteArrayOutputStream(8);
		DataOutputStream dos = new DataOutputStream(bo);

		/* --- Convert the value into a 8 byte double --- */
		dos.writeDouble(value);
		dos.close();
		byte[] b = bo.toByteArray();

		/* --- Change byte order --- */
		for (int i = 0; i < 4; i++) {
			byte help = b[i];
			b[i] = b[7 - i];
			b[7 - i] = help;
		}

		/* --- Write result into output stream --- */
		out.write(b);
	}
}
