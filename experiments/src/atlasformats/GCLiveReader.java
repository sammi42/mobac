package atlasformats;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;

/**
 * 
 * A simple tool for dumping the index file of a Geocaching-Live map/atlas
 * 
 *  File format description taken from http://palmtopia.de/trac/GCLiveMapGen
 *
 */
public class GCLiveReader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			File f = new File(args[0]);
			FileInputStream fin = new FileInputStream(f);
			DataInputStream din = new DataInputStream(fin);
			int count = 0;
			byte[] buffer = new byte[16];
			try {
				while (true) {
					din.readFully(buffer);
					int zoom = 17 - getInt(buffer, 0, 2);
					int x = getInt(buffer, 2, 3);
					int y = getInt(buffer, 5, 3);
					int file_offset = getInt(buffer, 8, 4);
					int dataFile = getInt(buffer, 14, 2) & 0x0FFF;
					System.out.println(String.format("0x%05x | z=%2d x=%d y=%d \tdata-off: 0x%06x in file %03d", count, zoom, x,
							y, file_offset, dataFile));
					count += buffer.length;
				}
			} catch (EOFException e) {
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getInt(byte[] arr, int offset, int bytes) {
		int result = 0;
		for (int i = 0; i < bytes; i++) {
			result <<= 8;
			result |= arr[offset++] & 0xFF;
		}
		return result;
	}
}
