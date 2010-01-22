package mobac.utilities.tar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import mobac.program.atlascreators.AtlasCreator;
import mobac.utilities.Utilities;


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
		String tmiFilename = tarFile.getAbsolutePath();
		if (tmiFilename.toLowerCase().endsWith(".tar"))
			tmiFilename = tmiFilename.substring(0, tmiFilename.length() - 4);

		tmiWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmiFilename
				+ ".tmi"), AtlasCreator.TEXT_FILE_CHARSET));
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

	@Override
	protected void writeTarHeader(TarHeader th) throws IOException {
		long streamPos = getTarFilePos();
		int block = (int) (streamPos >> 9);
		String line = String.format("block %10d: %s\n", new Object[] { block, th.getFileName() });
		tmiWriter.write(line);
		super.writeTarHeader(th);
	}

}
