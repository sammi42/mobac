package utilities;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
	
	public static byte[] getFileBytes(File file) throws IOException {
		int size = (int) file.length();
		byte[] buffer = new byte[size];
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		try {
			in.readFully(buffer);
			return buffer;
		} finally {
			closeStream(in);
		}
	}

	public static void closeStream(InputStream in) {
		if (in == null)
			return;
		try {
			in.close();
		} catch (IOException e) {
		}
	}
}
