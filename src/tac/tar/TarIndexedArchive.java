package tac.tar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Extended version of {@link TarArchive} that automatically creates
 * {@link Hashtable} with the starting offsets of every archived file.
 */
public class TarIndexedArchive extends TarArchive {

	private Hashtable<String, Integer> tarIndex;

	public TarIndexedArchive(File tarFile, int approxFileCount) throws IOException {
		super(tarFile, null);
		tarIndex = new Hashtable<String, Integer>(approxFileCount);
	}

	@Override
	protected void writeTarHeader(TarHeader th) throws IOException {
		int streamPos = getTarFilePos();
		tarIndex.put(th.getFileName(), new Integer(streamPos));
		super.writeTarHeader(th);
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
