package tac.tar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TarRecord {

	private byte[] fileData;

	public TarRecord(File theFile) throws IOException {

		long fl = theFile.length();
		if (fl > Integer.MAX_VALUE)
			throw new RuntimeException("File size too large");
		int fileLength = (int) fl;

		if (fileLength < 512) {
			fileData = new byte[512];
		} else {
			int mod = fileLength % 512;
			// align buffer size on 512 byte block length
			if (mod != 0)
				fileLength += 512 - mod;
			fileData = new byte[fileLength];
		}
		this.setRecordContent(theFile);
	}

	public void setRecordContent(File theFile) throws IOException {

		FileInputStream inputFile = null;
		try {
			inputFile = new FileInputStream(theFile);
			inputFile.read(fileData, 0, (int) theFile.length());
		} finally {
			if (inputFile != null)
				inputFile.close();
		}
	}

	public byte[] getRecordContent() {
		return fileData;
	}
}