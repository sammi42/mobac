package tac.program;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import tac.tar.PreparedTarEntry;
import tac.tar.TarArchive;
import tac.tar.TarUtilities;
import tac.utilities.Utilities;

/**
 * 
 * <a href="http://linuxtechs.net/kruch/tb/forum/viewtopic.php?t=21">TrekBuddy
 * Atlas format description</a>
 * 
 * <a href="http://www.linuxtechs.net/kruch/tb/forum/viewtopic.php?t=897">
 * TrekBuddy tmi map tar index file description</a>
 */
public class AtlasTarCreator {

	private Logger log = Logger.getLogger(AtlasTarCreator.class);

	private File atlasDir;
	private File atlasTarDir;

	private File[] atlasLayerDirs;
	private List<File> atlasMapDirs;

	/**
	 * 
	 * @param atlasDir
	 * @param atlasTarDir
	 *            empty target directory
	 */
	public AtlasTarCreator(File atlasDir, File atlasTarDir) {
		super();
		this.atlasDir = atlasDir;
		this.atlasTarDir = atlasTarDir;
		atlasLayerDirs = Utilities.listSubDirectories(atlasDir);
		atlasMapDirs = new LinkedList<File>();
		for (File dir : atlasLayerDirs)
			Utilities.addSubDirectories(atlasMapDirs, dir, 0);
	}

	public void createAtlasCrTarArchive() {
		log.trace("Creating cr.tar for atlas in dir \"" + atlasTarDir.getPath() + "\"");
		TarArchive ta = null;
		File crFile = new File(atlasTarDir, "cr.tar");
		try {
			ta = new TarArchive(crFile, atlasDir);

			ta.writeFile(new File(atlasDir, "cr.tba"));

			for (File mapDir : atlasMapDirs) {
				List<File> folderContent = TarUtilities.getDirectoryContent(mapDir);
				for (File f : folderContent) {
					// Add directories and .map files to cr.tar
					if (f.isDirectory() || (f.getName().toLowerCase().endsWith(".map")))
						ta.writeFile(f);
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

	public void createMapTars() {
		for (File atlasMapDir : atlasMapDirs) {
			createMapTar(atlasMapDir);
		}
	}

	protected void createMapTar(File atlasMapDir) {
		log.trace("Creating tar for map in dir \"" + atlasMapDir.getPath() + "\"");
		String layerName = atlasMapDir.getParentFile().getName();
		String mapName = atlasMapDir.getName();
		File atlasTarMapDir = new File(atlasTarDir, layerName);
		File atlasTarLayerDir = new File(atlasTarMapDir, mapName);
		atlasTarLayerDir.mkdirs();

		List<File> folderContent = TarUtilities.getDirectoryContent(new File(atlasMapDir, "set"));

		List<PreparedTarEntry> preparedEntries = new ArrayList<PreparedTarEntry>(folderContent
				.size() + 1);

		File mapFile = new File(atlasMapDir, mapName + ".map");
		PreparedTarEntry preparedMapFileEntry = new PreparedTarEntry(mapFile, atlasMapDir);
		preparedEntries.add(preparedMapFileEntry);

		StringWriter sw = new StringWriter(50 * folderContent.size());
		sw.write(preparedMapFileEntry.getTmiLine(0));
		int blocksUsed = preparedMapFileEntry.getTarBlocksRequired();

		for (File f : folderContent) {
			PreparedTarEntry entry = new PreparedTarEntry(f, atlasMapDir);
			preparedEntries.add(entry);
			sw.write(entry.getTmiLine(blocksUsed));
			blocksUsed += entry.getTarBlocksRequired();
		}

		File tmiFile = new File(atlasTarLayerDir, mapName + ".tmi");
		FileWriter fw = null;
		try {
			fw = new FileWriter(tmiFile);
			fw.write(sw.toString());
		} catch (IOException e) {
			log.error("Failed writing tmi file \"" + tmiFile.getPath() + "\"", e);
		} finally {
			Utilities.closeWriter(fw);
		}

		TarArchive ta = null;
		File tarFile = new File(atlasTarLayerDir, mapName + ".tar");
		try {
			ta = new TarArchive(tarFile, atlasMapDir);
			for (PreparedTarEntry entry : preparedEntries) {
				ta.writePreparedEntry(entry);
			}
			ta.writeEndofArchive();
		} catch (IOException e) {
			log.error("Error while writing tar archive \"" + tarFile.getPath() + "\"", e);
		} finally {
			if (ta != null)
				ta.close();
		}
	}
}
