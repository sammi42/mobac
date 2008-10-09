package tac.tar;

import java.io.File;
import java.io.IOException;

public class PreparedTarEntry {

	private TarHeader tarHeader;
	private File fileToBeTared;
	public File getFileToBeTared() {
		return fileToBeTared;
	}

	private int tarBlocksRequired;

	public PreparedTarEntry(File fileToBeTared, File baseFilePath) {
		super();
		this.fileToBeTared = fileToBeTared;
		tarHeader = new TarHeader(fileToBeTared, baseFilePath);
		tarBlocksRequired = 1;
		if (!fileToBeTared.isDirectory()) {
			int fileSizeInTar = TarRecord.calculateFileSizeInTar(fileToBeTared);
			// One Block for the header
			tarBlocksRequired += fileSizeInTar / 512;
		}
	}

	public TarHeader getTarHeader() {
		return tarHeader;
	}

	public TarRecord getTarRecord() throws IOException {
		return new TarRecord(fileToBeTared);
	}

	public int getTarBlocksRequired() {
		return tarBlocksRequired;
	}

	/**
	 * @return Number of characters an entry (line) in the tmi file will take
	 */
	public int getTmiEntryLength() {
		return 20 + getTarHeader().getFileNameLength();
	}
}
