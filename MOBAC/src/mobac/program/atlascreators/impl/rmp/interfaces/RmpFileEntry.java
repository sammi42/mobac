/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package mobac.program.atlascreators.impl.rmp.interfaces;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface for all files that are stored in a RMP file
 * 
 */
public interface RmpFileEntry {
	/**
	 * Returns the content of the file as byte array
	 * 
	 * @throws InterruptedException
	 */
	public void writeFileContent(OutputStream os) throws IOException, InterruptedException;

	/**
	 * Returns the name of the file without extension
	 */
	public String getFileName();

	/**
	 * Returns the extension of the file
	 */
	public String getFileExtension();

}
