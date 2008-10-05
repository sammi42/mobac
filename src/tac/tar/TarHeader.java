package tac.tar;

import java.io.File;

public class TarHeader {

	private File baseFilePath;

	private char [] fileName;
	private char [] fileMode;
	private char [] fileOwnerUserID;
	private char [] fileOwnerGroupID;
	private char [] fileSize;
	private char [] lastModificationTime;
	private char [] checksum;
	private char [] linkIndicator;
	private char [] nameOfLinkedFile;
	private char [] padding;

	public TarHeader() {

		fileName = new char [100];
		fileMode = new char [8];
		fileOwnerUserID = new char [8];
		fileOwnerGroupID = new char [8];
		fileSize = new char [12];
		lastModificationTime = new char [12];
		checksum = new char [8];
		linkIndicator = new char [1];
		nameOfLinkedFile = new char [100];
		padding = new char [255];
	}

	public TarHeader(File theFile, File theBaseFilePath) {

		baseFilePath = theBaseFilePath;

		fileName = new char [100];
		fileMode = new char [8];
		fileOwnerUserID = new char [8];
		fileOwnerGroupID = new char [8];
		fileSize = new char [12];
		lastModificationTime = new char [12];
		checksum = new char [8];
		linkIndicator = new char [1];
		nameOfLinkedFile = new char [100];
		padding = new char [255];

		this.setFileName(theFile, baseFilePath);
		this.setFileMode();
		this.setFileOwnerUserID();
		this.setFileOwnerGroupID();
		this.setFileSize(theFile);
		this.setLastModificationTime(theFile);
		this.setLinkIndicator(theFile);
		this.setNameOfLinkedFile();
		this.setPadding();
		this.setChecksum();
	}

	// S E T - Methods
	public void setFileName(File theFile, File theBaseFilePath) {

		String tarFileName = theFile.getAbsolutePath().substring(theBaseFilePath.getAbsolutePath().length(), theFile.getAbsolutePath().length());

		tarFileName = tarFileName.replace((char)92, (char)47);
		
		if (tarFileName.startsWith("/")) {
			tarFileName = tarFileName.substring(1, tarFileName.length());
		}
				
		if (theFile.isDirectory()) {
			tarFileName = tarFileName + "/"; //System.getProperty("file.separator");
		}

		char [] theFileName = tarFileName.toCharArray();

		for (int i = 0; i < fileName.length; i++) {

			if (i < theFileName.length) {
				fileName[i] = theFileName[i];
			}
			else {
				fileName[i] = 0;
			}
		}
	}

	public void setFileMode() {

		fileMode[0] = ' ';
		fileMode[1] = ' ';
		fileMode[2] = ' ';
		fileMode[3] = '7';
		fileMode[4] = '7';
		fileMode[5] = '7';
		fileMode[6] = ' ';
		fileMode[7] = 0;
	}

	public void setFileOwnerUserID() {

		fileOwnerUserID[0] = ' ';
		fileOwnerUserID[1] = ' ';
		fileOwnerUserID[2] = ' ';
		fileOwnerUserID[3] = ' ';
		fileOwnerUserID[4] = ' ';
		fileOwnerUserID[5] = '0';
		fileOwnerUserID[6] = ' ';
		fileOwnerUserID[7] = 0;
	}

	public void setFileOwnerGroupID() {

		fileOwnerGroupID[0] = ' ';
		fileOwnerGroupID[1] = ' ';
		fileOwnerGroupID[2] = ' ';
		fileOwnerGroupID[3] = ' ';
		fileOwnerGroupID[4] = ' ';
		fileOwnerGroupID[5] = '0';
		fileOwnerGroupID[6] = ' ';
		fileOwnerGroupID[7] = 0;
	}

	public void setFileSize(File theFile) {

		long fileSizeLong = 0;

		if (!theFile.isDirectory()) {
			fileSizeLong = theFile.length();
		}

		char [] fileSizeCharArray = Long.toString(fileSizeLong, 8).toCharArray();

		int offset = 11 - fileSizeCharArray.length;

		for (int i = 0; i < 12; i++) {
			if (i < offset) {
				fileSize[i] = ' ';
			}
			else if (i == 11) {
				fileSize[i] = ' ';
			}
			else {
				fileSize[i] = fileSizeCharArray[i - offset];
			}
		}
	}

	public void setLastModificationTime(File theFile) {

		long lastModifiedTime = 0;
		
		lastModifiedTime = theFile.lastModified();
		lastModifiedTime = theFile.lastModified() / 1000;

		char [] fileLastModifiedTimeCharArray = Long.toString(lastModifiedTime, 8).toCharArray();
		
		for (int i = 0; i < fileLastModifiedTimeCharArray.length; i++) {
			lastModificationTime[i] = fileLastModifiedTimeCharArray[i];
		}

		if (fileLastModifiedTimeCharArray.length < 12) {
			for (int i = fileLastModifiedTimeCharArray.length; i < 12; i++) {
				lastModificationTime[i] = ' ';
			}
		}
	}

	public void setChecksum() {

		int checksumInt = 0;

		for (int i = 0; i < 100; i ++) {
			checksumInt = checksumInt + (int)fileName[i];
		}

		for (int i = 0; i < 8; i ++) {
			checksumInt = checksumInt + (int)fileMode[i];
		}

		for (int i = 0; i < 8; i ++) {
			checksumInt = checksumInt + (int)fileOwnerUserID[i];
		}

		for (int i = 0; i < 8; i ++) {
			checksumInt = checksumInt + (int)fileOwnerGroupID[i];
		}

		for (int i = 0; i < 12; i ++) {
			checksumInt = checksumInt + (int)fileSize[i];
		}

		for (int i = 0; i < 12; i ++) {
			checksumInt = checksumInt + (int)lastModificationTime[i];
		}

		for (int i = 0; i < 1; i ++) {
			checksumInt = checksumInt + (int)linkIndicator[i];
		}

		checksumInt = checksumInt + (8 * 32);
		
		char [] checkSumIntChar = Integer.toString(checksumInt, 8).toCharArray();

		int offset = 8 - checkSumIntChar.length;

		for (int i = checksum.length - 1; i >= 0; i--) {

			if(i == checksum.length - 1) {
				checksum[i] = 0;
			}

			if(i == checksum.length - 2) {
				checksum[i] = ' ';
			}

			if (i < checksum.length - 2) {

				if (i - (offset - 2) >= 0) {
					checksum[i] = checkSumIntChar[i - (offset - 2)];
				}
				else {
					checksum[i] = ' ';
				}
			}
		}
	}

	public void setLinkIndicator(File theFile) {

		if (theFile.isDirectory()) {
			linkIndicator[0] = '5';
		}
		else {
			linkIndicator[0] = '0';
		}
	}

	public void setNameOfLinkedFile() {
		for (int i = 0; i < 100; i++) {
			nameOfLinkedFile[i] = 0;
		}
	}

	public void setPadding() {
		for (int i = 0; i < 255; i++) {
			padding[i] = 0;
		}
	}

	// G E T - Methods
	public char [] getFileName() {
		return fileName;
	}

	public char [] getFileMode() {
		return fileMode;
	}

	public char [] getFileOwnerUserID() {
		return fileOwnerUserID;
	}

	public char [] getFileOwnerGroupID() {
		return fileOwnerGroupID;
	}

	public char [] getFileSize() {
		return fileSize;
	}

	public char [] getLastModificationTime() {
		return lastModificationTime;
	}

	public char [] getChecksum() {
		return checksum;
	}

	public char [] getLinkIndicator() {
		return linkIndicator;
	}

	public char [] getNameOfLinkedFile() {
		return nameOfLinkedFile;
	}

	public char [] getPadding() {
		return padding;
	}

	public String getHeaderAsString() {

		StringBuffer sb = new StringBuffer(512);

		sb.append(fileName);
		sb.append(fileMode);
		sb.append(fileOwnerUserID);
		sb.append(fileOwnerGroupID);
		sb.append(fileSize);
		sb.append(lastModificationTime);
		sb.append(checksum);
		sb.append(linkIndicator);
		sb.append(nameOfLinkedFile);
		sb.append(padding);
		
		return sb.toString();
	}
}