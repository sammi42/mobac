package tac.tar;

import java.io.File;

public class TarHeader {

	private File baseFilePath;

	private int fileNameLength;
	private char[] fileName;
	private char[] fileMode;
	private char[] fileOwnerUserID;
	private char[] fileOwnerGroupID;
	private char[] fileSize;
	private char[] lastModificationTime;
	private char[] checksum;
	private char[] linkIndicator;
	private char[] nameOfLinkedFile;
	private char[] padding;

	public TarHeader() {
		fileName = new char[100];
		fileMode = new char[8];
		fileOwnerUserID = new char[8];
		fileOwnerGroupID = new char[8];
		fileSize = new char[12];
		lastModificationTime = new char[12];
		checksum = new char[8];
		linkIndicator = new char[1];
		nameOfLinkedFile = new char[100];
		padding = new char[255];
	}

	public TarHeader(File theFile, File theBaseFilePath) {
		this();
		baseFilePath = theBaseFilePath;

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

	public TarHeader(String fileName, int fileSize, boolean isDirectory) {
		this();
		this.setFileName(fileName);
		this.setFileMode();
		this.setFileOwnerUserID();
		this.setFileOwnerGroupID();
		this.setFileSize(fileSize);
		this.setLastModificationTime(System.currentTimeMillis());
		this.setLinkIndicator(isDirectory);
		this.setNameOfLinkedFile();
		this.setPadding();
		this.setChecksum();
	}

	public void read(byte[] buffer) {
		String fn = new String(buffer, 0, 100);
		fileName = fn.toCharArray();
		fileNameLength = fn.indexOf('\0');
		fileMode = new String(buffer, 100, 8).toCharArray();
		fileOwnerUserID = new String(buffer, 108, 8).toCharArray();
		fileOwnerGroupID = new String(buffer, 116, 8).toCharArray();
		fileSize = new String(buffer, 124, 12).toCharArray();
		lastModificationTime = new String(buffer, 136, 12).toCharArray();
		checksum = new String(buffer, 148, 8).toCharArray();
		linkIndicator = new String(buffer, 156, 1).toCharArray();
		nameOfLinkedFile = new String(buffer, 157, 100).toCharArray();
	}

	// S E T - Methods
	public void setFileName(File theFile, File theBaseFilePath) {

		String filePath = theFile.getAbsolutePath();
		String basePath = theBaseFilePath.getAbsolutePath();
		if (!filePath.startsWith(basePath))
			throw new RuntimeException("File \"" + filePath
					+ "\" is outside of archive base path \"" + basePath + "\"!");

		String tarFileName = filePath.substring(basePath.length(), filePath.length());

		tarFileName = tarFileName.replace('\\', '/');

		if (tarFileName.startsWith("/"))
			tarFileName = tarFileName.substring(1, tarFileName.length());

		if (theFile.isDirectory())
			tarFileName = tarFileName + "/";
		setFileName(tarFileName);
	}

	public void setFileName(String newFileName) {
		char[] theFileName = newFileName.toCharArray();

		fileNameLength = newFileName.length();
		for (int i = 0; i < fileName.length; i++) {
			if (i < theFileName.length) {
				fileName[i] = theFileName[i];
			} else {
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
		setFileSize(fileSizeLong);
	}

	public void setFileSize(long fileSize) {
		char[] fileSizeCharArray = Long.toString(fileSize, 8).toCharArray();

		int offset = 11 - fileSizeCharArray.length;

		for (int i = 0; i < 12; i++) {
			if (i < offset) {
				this.fileSize[i] = ' ';
			} else if (i == 11) {
				this.fileSize[i] = ' ';
			} else {
				this.fileSize[i] = fileSizeCharArray[i - offset];
			}
		}
	}

	public void setLastModificationTime(File theFile) {

		setLastModificationTime(theFile.lastModified());
	}

	public void setLastModificationTime(long lastModifiedTime) {
		lastModifiedTime /= 1000;

		char[] fileLastModifiedTimeCharArray = Long.toString(lastModifiedTime, 8).toCharArray();

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

		for (int i = 0; i < 100; i++) {
			checksumInt = checksumInt + (int) fileName[i];
		}

		for (int i = 0; i < 8; i++) {
			checksumInt = checksumInt + (int) fileMode[i];
		}

		for (int i = 0; i < 8; i++) {
			checksumInt = checksumInt + (int) fileOwnerUserID[i];
		}

		for (int i = 0; i < 8; i++) {
			checksumInt = checksumInt + (int) fileOwnerGroupID[i];
		}

		for (int i = 0; i < 12; i++) {
			checksumInt = checksumInt + (int) fileSize[i];
		}

		for (int i = 0; i < 12; i++) {
			checksumInt = checksumInt + (int) lastModificationTime[i];
		}

		for (int i = 0; i < 1; i++) {
			checksumInt = checksumInt + (int) linkIndicator[i];
		}

		checksumInt = checksumInt + (8 * 32);

		char[] checkSumIntChar = Integer.toString(checksumInt, 8).toCharArray();

		int offset = 8 - checkSumIntChar.length;

		for (int i = checksum.length - 1; i >= 0; i--) {

			if (i == checksum.length - 1) {
				checksum[i] = 0;
			}

			if (i == checksum.length - 2) {
				checksum[i] = ' ';
			}

			if (i < checksum.length - 2) {

				if (i - (offset - 2) >= 0) {
					checksum[i] = checkSumIntChar[i - (offset - 2)];
				} else {
					checksum[i] = ' ';
				}
			}
		}
	}

	public void setLinkIndicator(File theFile) {
		setLinkIndicator(theFile.isDirectory());
	}

	public void setLinkIndicator(boolean isDirectory) {
		if (isDirectory) {
			linkIndicator[0] = '5';
		} else {
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
	public String getFileName() {
		return new String(fileName, 0, fileNameLength);
	}

	public int getFileNameLength() {
		return fileNameLength;
	}

	public char[] getFileMode() {
		return fileMode;
	}

	public char[] getFileOwnerUserID() {
		return fileOwnerUserID;
	}

	public char[] getFileOwnerGroupID() {
		return fileOwnerGroupID;
	}

	public char[] getFileSize() {
		return fileSize;
	}

	public int getFileSizeInt() {
		return Integer.parseInt(new String(fileSize).trim(), 8);
	}

	public char[] getLastModificationTime() {
		return lastModificationTime;
	}

	public char[] getChecksum() {
		return checksum;
	}

	public char[] getLinkIndicator() {
		return linkIndicator;
	}

	public char[] getNameOfLinkedFile() {
		return nameOfLinkedFile;
	}

	public char[] getPadding() {
		return padding;
	}

	public byte[] getBytes() {

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

		byte[] result = sb.toString().getBytes();
		if (result.length != 512)
			throw new RuntimeException("Invaliud tar header size: " + result.length);
		return result;
	}

}