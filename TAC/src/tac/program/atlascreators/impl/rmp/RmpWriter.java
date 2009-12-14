/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package tac.program.atlascreators.impl.rmp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import tac.program.atlascreators.impl.rmp.interfaces.RmpFileEntry;
import tac.program.atlascreators.impl.rmp.rmpfile.RmpIni;
import tac.utilities.Utilities;
import tac.utilities.stream.RandomAccessFileOutputStream;

/**
 * Class that writes files in RMP archive format
 * 
 */
public class RmpWriter {
	private static final Logger log = Logger.getLogger(RmpWriter.class);

	private final ArrayList<EntryInfo> entries = new ArrayList<EntryInfo>();
	private final File rmpFile;
	private final RandomAccessFile rmpOutputFile;
	private int projectedEntryCount;

	private ChecksumOutputStream entryOut;

	/**
	 * @param imageName
	 * @param layerCount
	 *            projected number of layers that will be written to this rmp
	 *            file
	 * @param rmpFile
	 * @throws IOException
	 */
	public RmpWriter(String imageName, int layerCount, File rmpFile) throws IOException {
		this.rmpFile = rmpFile;
		// We only use one A00 entry per map/layer - therefore we can
		// pre-calculate the number of entries:
		// RmpIni + (TLM & A00) per layer + Bmp2Bit + Bmp4bit
		this.projectedEntryCount = (3 + (2 * layerCount));
		if (rmpFile.exists())
			Utilities.deleteFile(rmpFile);
		log.debug("Writing data to " + rmpFile.getAbsolutePath());
		rmpOutputFile = new RandomAccessFile(rmpFile, "rw");
		// Calculate offset to the directory end
		int directoryEndOffset = projectedEntryCount * 24 + 10;
		rmpOutputFile.seek(directoryEndOffset);
		entryOut = new ChecksumOutputStream(new RandomAccessFileOutputStream(rmpOutputFile));
		/* --- Write the directory-end marker --- */
		RmpTools.writeFixedString(entryOut, "MAGELLAN", 30);

		RmpIni rmpIni = new RmpIni(imageName, layerCount);

		/* --- Create packer and fill it with content --- */
		writeFileEntry(rmpIni);
	}

	public void writeFileEntry(RmpFileEntry entry) throws IOException {
		EntryInfo info = new EntryInfo();
		info.name = entry.getFileName();
		info.extendsion = entry.getFileExtension();
		info.offset = (int) rmpOutputFile.getFilePointer();
		entry.writeFileContent(entryOut);
		info.length = ((int) rmpOutputFile.getFilePointer()) - info.offset;
		if ((info.length % 2) != 0)
			entryOut.write(0);
		entries.add(info);
		log.debug("Written data of entry " + entry + " bytes=" + info.length);
	}

	/**
	 * Writes the directory of the archive into the rmp file
	 * 
	 * @throws IOException
	 *             Error accessing disk
	 */
	public void writeDirectory() throws IOException {
		if (projectedEntryCount != entries.size())
			throw new RuntimeException("Entry count does not correspond "
					+ "to the projected layer count: \nProjected: " + projectedEntryCount
					+ "\nPresent:" + entries.size());

		// Finalize the list of written entries
		RmpTools.writeFixedString(entryOut, "MAGELLAN", 8);
		entryOut.writeChecksum();

		log.debug("Finished writing entries, updating directory");

		/* --- Create file --- */
		rmpOutputFile.seek(0);
		OutputStream out = new RandomAccessFileOutputStream(rmpOutputFile);
		ChecksumOutputStream cout = new ChecksumOutputStream(out);

		/* --- Write header with number of files --- */
		RmpTools.writeValue(cout, entries.size(), 4);
		RmpTools.writeValue(cout, entries.size(), 4);

		/* --- Write the directory --- */
		log.debug("Writing directory: " + entries.size() + " entries");
		for (EntryInfo entryInfo : entries) {

			log.trace("Entry: " + entryInfo);
			/* --- Write directory entry --- */
			RmpTools.writeFixedString(cout, entryInfo.name, 9);
			RmpTools.writeFixedString(cout, entryInfo.extendsion, 7);
			RmpTools.writeValue(cout, entryInfo.offset, 4);
			RmpTools.writeValue(cout, entryInfo.length, 4);
		}

		/* --- Write the header checksum (2 bytes) --- */
		cout.writeChecksum();

	}

	public void close() {
		try {
			rmpOutputFile.close();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void delete() {
		close();
		rmpFile.delete();
	}

	private static class EntryInfo {
		String name;
		String extendsion;
		int offset;
		int length;

		@Override
		public String toString() {
			return "\"" + name + "." + extendsion + "\" offset=" + offset + " length=" + length;
		}

	}
}
