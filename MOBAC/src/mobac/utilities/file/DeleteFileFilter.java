package mobac.utilities.file;

import java.io.File;
import java.io.FileFilter;

/**
 * A {@link FileFilter} that deletes every file in the directory specified by
 * the {@link File} on which {@link File#listFiles(FileFilter)} using
 * {@link DeleteFileFilter} is executed. Therefore the {@link FileFilter}
 * concept is abused as {@link File} enumerator.
 * <p>
 * Example: <code>new File("C:/Temp").listFiles(new DeleteFileFilter());</code>
 * deletes all files but no directories in the directory C:\Temp.
 * </p>
 */
public class DeleteFileFilter implements FileFilter {

	int countSuccess = 0;
	int countFailed = 0;
	int countError = 0;

	public boolean accept(File file) {
		try {
			if (file.isDirectory())
				// We only delete files
				return false;
			boolean success = file.delete();
			if (success)
				countSuccess++;
			else
				countFailed++;
		} catch (Exception e) {
			countError++;
		}
		// We don't care about the filter result
		return false;
	}

	public int getCountSuccess() {
		return countSuccess;
	}

	public int getCountFailed() {
		return countFailed;
	}

	public int getCountError() {
		return countError;
	}

	@Override
	public String toString() {
		return "Delete file filter status (success, failed, error): " + countSuccess + " / "
				+ countFailed + " / " + countError + " files";
	}

}
