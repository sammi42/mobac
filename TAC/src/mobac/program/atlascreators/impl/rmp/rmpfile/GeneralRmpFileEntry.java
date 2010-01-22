/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package mobac.program.atlascreators.impl.rmp.rmpfile;

import java.io.IOException;
import java.io.OutputStream;

import mobac.program.atlascreators.impl.rmp.interfaces.RmpFileEntry;


/**
 * General class for storing the content of a rmp file
 * 
 */
public class GeneralRmpFileEntry implements RmpFileEntry {
	protected final byte[] content;
	protected final String filename;
	protected final String extension;

	public GeneralRmpFileEntry(byte[] content, String filename, String extension) {
		this.content = content;
		this.filename = filename;
		this.extension = extension;
	}

	public void writeFileContent(OutputStream os) throws IOException {
		os.write(content);
	}

	public String getFileExtension() {
		return extension;
	}

	public String getFileName() {
		return filename;
	}

	@Override
	public String toString() {
		return "GeneralRmpFileEntry \"" + filename + "." + extension + "\" content-len="
				+ content.length;
	}
}
