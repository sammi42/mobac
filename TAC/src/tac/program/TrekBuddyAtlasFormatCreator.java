package tac.program;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import tac.tar.TarArchive;
import tac.utilities.Utilities;

/**
 * 
 * <a href="http://linuxtechs.net/kruch/tb/forum/viewtopic.php?t=21">TrekBuddy
 * Atlas format description</a>
 * 
 */
public class TrekBuddyAtlasFormatCreator {

	private static Logger log = Logger.getLogger(TrekBuddyAtlasFormatCreator.class);

	/**
	 * 
	 * @param atlasDir
	 * @param name 
	 */
	public static void createAtlasTarArchive(File atlasDir, String name) {
		log.trace("Creating cr.tar for atlas in dir \"" + atlasDir.getPath() + "\"");

		File[] atlasLayerDirs = Utilities.listSubDirectories(atlasDir);
		List<File> atlasMapDirs = new LinkedList<File>();
		for (File dir : atlasLayerDirs)
			Utilities.addSubDirectories(atlasMapDirs, dir, 0);

		TarArchive ta = null;
		File crFile = new File(atlasDir, name + ".tar");
		try {
			ta = new TarArchive(crFile, atlasDir);

			ta.writeFileFromData(name + ".tba", "Atlas 1.0\r\n".getBytes());

			for (File mapDir : atlasMapDirs) {
				ta.writeFile(mapDir);
				File mapFile = new File(mapDir, mapDir.getName() + ".map");
				ta.writeFile(mapFile);
				try {
					mapFile.delete();
				} catch (Exception e) {
				}
			}
			ta.writeEndofArchive();
		} catch (IOException e) {
			log.error("Failed writing tar file \"" + crFile.getPath() + "\"", e);
		} finally {
			if (ta != null)
				ta.close();
		}
	}

	public static void createAtlasTbaFile(File atlasDir, String name) {
		File crtba = new File(atlasDir.getAbsolutePath(), name + ".tba");
		try {
			FileWriter fw = new FileWriter(crtba);
			fw.write("Atlas 1.0\r\n");
			fw.close();
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
