package mobac.utilities.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class GpxFileFilter extends FileFilter {

	private boolean onlyGpx11;

	public GpxFileFilter(boolean onlyGpx11) {
		this.onlyGpx11 = onlyGpx11;
	}

	@Override
	public boolean accept(File f) {
		return f.isDirectory() || f.getName().endsWith(".gpx");
	}

	@Override
	public String getDescription() {
		if (onlyGpx11)
			return "GPX 1.1 files (*.gpx)";
		else
			return "GPX 1.0/1.1 files (*.gpx)";

	}

}
