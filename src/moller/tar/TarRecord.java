package moller.tar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TarRecord {

	private byte [] fileData;

	public TarRecord(File theFile) {

		long fileLength = theFile.length();

		if (fileLength < 512) {
			fileData = new byte [512];
		}
		else {
			if (fileLength % 512 == 0) {
				fileData = new byte [(int)fileLength];
			}
			else {
				fileData = new byte [(((int)fileLength / 512) + 1) * 512];
			}
		}

		this.setRecordContent(theFile);
	}

	public void setRecordContent(File theFile) {

		try {
			FileInputStream inputFile = new FileInputStream(theFile);
			inputFile.read(fileData, 0, (int)theFile.length());
			inputFile.close();
		}
		catch (IOException iox) {
			iox.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Fel");
			// TODO: handle exception
		}
	}

	public byte [] getRecordContent() {
		return fileData;
	}
}