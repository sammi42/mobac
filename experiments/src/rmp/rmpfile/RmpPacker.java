/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package rmp.rmpfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import rmp.rmpfile.entries.RmpFileEntry;

/**
 * Class that writes files in RMP archive format
 * 
 */
public class RmpPacker {
	private static final Logger log = Logger.getLogger(RmpPacker.class);

	private ArrayList<RmpFileEntry> entries;

	/**
	 * Constructor
	 */
	public RmpPacker() {
		entries = new ArrayList<RmpFileEntry>();
	}

	/**
	 * Add a file to the archive
	 * 
	 * @param file
	 *            file to add
	 */
	public void addFile(RmpFileEntry file) {
		entries.add(file);
	}

	/**
	 * Writes the content of the archive into a file
	 * 
	 * @param path
	 *            Path for file to write
	 * @throws IOException
	 *             Error accessing disk
	 */
	public void writeToDisk(File path) throws IOException {
		log.debug("Writing RMP to " + path.getAbsolutePath());
		int offset;

		/* --- Create file --- */
		FileOutputStream fo = new FileOutputStream(path);
		ChecksumOutputStream co = new ChecksumOutputStream(fo);

		/* --- Write header with number of files --- */
		Tools.writeValue(co, entries.size(), 4);
		Tools.writeValue(co, entries.size(), 4);

		/* --- Write the directory --- */
		offset = entries.size() * 24 + 40;
		log.debug("Writing directory: " + entries.size() + " entries");
		for (RmpFileEntry rmpEntry : entries) {

			log.trace("Entry: " + rmpEntry);
			/* --- Write directory entry --- */
			Tools.writeFixedString(co, rmpEntry.getFileName(), 9);
			Tools.writeFixedString(co, rmpEntry.getFileExtension(), 7);
			Tools.writeValue(co, offset, 4);
			Tools.writeValue(co, rmpEntry.getFileContent().length, 4);

			/* --- Calculate offset of next file. File length is always even --- */
			offset += rmpEntry.getFileContent().length;
			if ((rmpEntry.getFileContent().length % 2) != 0)
				offset += 1;
		}

		/* --- Write the header checksum --- */
		co.writeChecksum();

		co = new ChecksumOutputStream(fo);

		/* --- Write the directory-end marker --- */
		Tools.writeFixedString(co, "MAGELLAN", 30);

		/* --- Write the files --- */
		for (RmpFileEntry file : entries) {
			co.write(file.getFileContent());
			/* --- Add a 0 byte if length of file is not even --- */
			if ((file.getFileContent().length % 2) != 0)
				co.write(0);
		}

		/* --- Write the trailer --- */
		Tools.writeFixedString(co, "MAGELLAN", 8);

		/* --- Write checksum --- */
		co.writeChecksum();

		/* --- Close file --- */
		co.close();
	}
}
