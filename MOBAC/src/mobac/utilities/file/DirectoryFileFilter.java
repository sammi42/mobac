package mobac.utilities.file;

import java.io.File;
import java.io.FileFilter;

/**
 * A {@link FileFilter} only returning directories.
 */
public class DirectoryFileFilter implements FileFilter {

	public boolean accept(File f) {
		return f.isDirectory();
	}
}
