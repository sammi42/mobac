/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package rmp.rmpfile.entries;


/**
 * General class for storing the content of a rmp file
 * 
 */
public class GeneralRmpFileEntry implements RmpFileEntry {
	private byte[] content;
	private String filename;
	private String extension;

	public GeneralRmpFileEntry(byte[] content, String filename, String extension) {
		this.content = content;
		this.filename = filename;
		this.extension = extension;
	}

	public byte[] getFileContent() {
		return content;
	}

	public String getFileExtension() {
		return extension;
	}

	public String getFileName() {
		return filename;
	}

	@Override
	public String toString() {
		return "GeneralRmpFileEntry [content-len=" + content.length + ", " + filename + "."
				+ extension + "]";
	}
}
