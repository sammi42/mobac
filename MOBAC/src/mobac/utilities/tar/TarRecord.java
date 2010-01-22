package mobac.utilities.tar;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import mobac.utilities.Utilities;


/**
 * A tar record contains the data of the file to be stored. The data size has to
 * be dividable by 512 (internal tar block length).
 * 
 */
public class TarRecord {

	private byte[] fileData;

	public TarRecord(File theFile) throws IOException {
		fileData = new byte[calculateFileSizeInTar(theFile)];
		this.setRecordContent(theFile);
	}

	public TarRecord(byte[] data) throws IOException {
		this(data, 0, data.length);
	}

	public TarRecord(byte[] data, int off, int len) throws IOException {
		fileData = new byte[calculateFileSizeInTar(len)];
		System.arraycopy(data, off, fileData, 0, len);
	}

	public static int calculateFileSizeInTar(File theFile) {
		long fl = theFile.length();
		if (fl > Integer.MAX_VALUE)
			throw new RuntimeException("File size too large");
		return calculateFileSizeInTar((int) fl);
	}

	public static int calculateFileSizeInTar(int fileLength) {
		if (fileLength < 512) {
			return 512;
		} else {
			int mod = fileLength % 512;
			// align buffer size on 512 byte block length
			if (mod != 0)
				fileLength += 512 - mod;
			return fileLength;
		}
	}

	public void setRecordContent(File theFile) throws IOException {

		FileInputStream inputFile = null;
		DataInputStream dIn;
		try {
			inputFile = new FileInputStream(theFile);
			dIn = new DataInputStream(inputFile);
			dIn.readFully(fileData, 0, (int) theFile.length());
			dIn.close();
		} finally {
			Utilities.closeStream(inputFile);
		}
	}

	public byte[] getRecordContent() {
		return fileData;
	}
}