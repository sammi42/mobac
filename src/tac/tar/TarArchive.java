package tac.tar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class TarArchive {

	private OutputStream tarFileStream;
	private File baseDir;

	public TarArchive(File tarFile, File baseDir) throws FileNotFoundException {
		this.tarFileStream = new BufferedOutputStream(new FileOutputStream(tarFile));
		this.baseDir = baseDir;
	}

	public boolean writeContentFromDir(File dirToAdd) throws IOException {
		if (!dirToAdd.isDirectory())
			return false;
		TarHeader th = new TarHeader(dirToAdd, baseDir);
		tarFileStream.write(th.getBytes());
		File[] files = dirToAdd.listFiles();
		Arrays.sort(files);
		for (File f : files) {
			if (!f.isDirectory())
				writeFile(f);
			else
				writeContentFromDir(f);
		}
		return true;
	}

	public void writeFile(File fileOrDirToAdd) throws IOException {
		TarHeader th = new TarHeader(fileOrDirToAdd, baseDir);
		tarFileStream.write(th.getBytes());

		if (!fileOrDirToAdd.isDirectory()) {
			TarRecord tr = new TarRecord(fileOrDirToAdd);
			tarFileStream.write(tr.getRecordContent());
		}
	}

	public void writeEndofArchive() throws IOException {
		byte[] endOfArchive = new byte[1024];
		tarFileStream.write(endOfArchive);
		tarFileStream.flush();
	}

	public void close() {
		try {
			tarFileStream.close();
		} catch (Exception e) {
		}
		tarFileStream = null;
	}
}