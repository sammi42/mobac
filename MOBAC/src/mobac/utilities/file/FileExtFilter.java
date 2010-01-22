package mobac.utilities.file;

import java.io.File;
import java.io.FileFilter;

public class FileExtFilter implements FileFilter {

	private final String acceptedFileExt;

	public FileExtFilter(String acceptedFileExt) {
		this.acceptedFileExt = acceptedFileExt;
	}

	public boolean accept(File pathname) {
		return pathname.getName().endsWith(acceptedFileExt);
	}

}
