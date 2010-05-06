package mobac.utilities.file;

import java.io.File;
import java.io.FileFilter;

import mobac.utilities.Utilities;


public class DirInfoFileFilter implements FileFilter {

	long dirSize = 0;
	int fileCount = 0;

	public DirInfoFileFilter() {
	}

	public boolean accept(File f) {
		if (f.isDirectory())
			return false;
		Utilities.checkForInterruptionRt();
		dirSize += f.length();
		fileCount++;
		return false;
	}

	public long getDirSize() {
		return dirSize;
	}

	public int getFileCount() {
		return fileCount;
	}
}
