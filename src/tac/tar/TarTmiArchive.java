package tac.tar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import tac.program.MapCreator;
import tac.utilities.CountingOutputStream;
import tac.utilities.Utilities;

/**
 * 
 * Extended version of {@link TarArchive} that automatically creates the
 * TrekBuddy tmi-file while writing the archive entries.
 * 
 * @author r_x
 * 
 * @see <a href="http://www.linuxtechs.net/kruch/tb/forum/viewtopic.php?t=897">
 *      TrekBuddy tmi map tar index file description< /a>
 */
public class TarTmiArchive extends TarArchive {

	Writer tmiWriter;

	public TarTmiArchive(File tarFile, File baseDir) throws IOException {
		super(tarFile, baseDir);
		tarFileStream = new CountingOutputStream(tarFileStream);
		String tmiFilename = tarFile.getAbsolutePath();
		if (tmiFilename.toLowerCase().endsWith(".tar"))
			tmiFilename = tmiFilename.substring(0, tmiFilename.length() - 4);

		tmiWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmiFilename
				+ ".tmi"), MapCreator.TEXT_FILE_CHARSET));
	}

	@Override
	public void writeEndofArchive() throws IOException {
		super.writeEndofArchive();
		tmiWriter.flush();
	}

	@Override
	public void close() {
		super.close();
		Utilities.closeWriter(tmiWriter);
	}

	protected void addTmiEntry(TarHeader th) throws IOException {
		int streamPos = ((CountingOutputStream) tarFileStream).getBytesWritten();
		int block = streamPos / 512;
		String line = String.format("block %10d: %s\n", new Object[] { block, th.getFileName() });
		tmiWriter.write(line);
	}

	@Override
	public void writeFile(File fileOrDirToAdd) throws IOException {
		TarHeader th = new TarHeader(fileOrDirToAdd, baseDir);
		addTmiEntry(th);
		tarFileStream.write(th.getBytes());

		if (!fileOrDirToAdd.isDirectory()) {
			TarRecord tr = new TarRecord(fileOrDirToAdd);
			tarFileStream.write(tr.getRecordContent());
		}
	}

	@Override
	public void writeDirectory(String dirName) throws IOException {
		TarHeader th = new TarHeader(dirName, 0);
		tarFileStream.write(th.getBytes());
	}

	@Override
	public void writeFileFromData(String fileName, byte[] data) throws IOException {
		TarHeader th = new TarHeader(fileName, data.length);
		addTmiEntry(th);
		tarFileStream.write(th.getBytes());
		TarRecord tr = new TarRecord(data);
		tarFileStream.write(tr.getRecordContent());
	}

}
