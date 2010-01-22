package mobac.utilities.tar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Extended version of {@link TarArchive} that automatically creates
 * {@link Hashtable} with the starting offsets of every archived file.
 */
public class TarIndexedArchive extends TarArchive {

	private TarIndexTable tarIndex;

	public TarIndexedArchive(File tarFile, int approxFileCount) throws IOException {
		super(tarFile, null);
		tarIndex = new TarIndexTable(approxFileCount);
	}

	@Override
	protected void writeTarHeader(TarHeader th) throws IOException {
		long streamPos = getTarFilePos();
		tarIndex.addTarEntry(th.getFileName(), streamPos);
		super.writeTarHeader(th);
	}

	public void delete() {
		if (tarFile != null) {
			boolean b = tarFile.delete();
			if (!b && tarFile.isFile())
				tarFile.deleteOnExit();
		}
	}

	public TarIndex getTarIndex() {
		try {
			return new TarIndex(tarFile, tarIndex);
		} catch (FileNotFoundException e) {
			// should never happen
			return null;
		}
	}

}
