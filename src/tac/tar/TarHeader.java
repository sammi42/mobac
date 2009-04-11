package tac.tar;

import java.io.File;

public class TarHeader {

	private File baseFilePath;

	private int fileNameLength;
	private final char[] fileName = new char[100];
	private final char[] fileMode = new char[8];
	private final char[] fileOwnerUserID = new char[8];
	private final char[] fileOwnerGroupID = new char[8];
	private final char[] fileSize = new char[12];
	private final char[] lastModificationTime = new char[12];
	private final char[] checksum = new char[8];
	private final char[] linkIndicator = new char[1];
	private final char[] nameOfLinkedFile = new char[100];
	private static final char[] padding = new char[255];

	public TarHeader() {
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
		this.setChecksum();
	}

	public void read(byte[] buffer) {
		String fn = new String(buffer, 0, 512);
		fn.getChars(0, 100, fileName, 0);
		fileNameLength = fn.indexOf('\0');
		fn.getChars(100, 108, fileMode, 0);
		fn.getChars(108, 116, fileOwnerUserID, 0);
		fn.getChars(116, 124, fileOwnerGroupID, 0);
		fn.getChars(124, 136, fileSize, 0);
		fn.getChars(136, 148, lastModificationTime, 0);
		fn.getChars(148, 156, checksum, 0);
		fn.getChars(156, 157, linkIndicator, 0);
		fn.getChars(157, 257, nameOfLinkedFile, 0);
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
		"   777 \0".getChars(0, 8, fileMode, 0);
	}

	public void setFileOwnerUserID() {
		"     0  \0".getChars(0, 8, fileOwnerUserID, 0);
	}

	public void setFileOwnerGroupID() {
		"     0  \0".getChars(0, 8, fileOwnerGroupID, 0);
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