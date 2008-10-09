package tac.program;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tac.tar.PreparedTarEntry;
import tac.tar.TarArchive;
import tac.tar.TarRecord;
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
		TarArchive ta = null;
		try {
			ta = new TarArchive(new File(atlasTarDir, "cr.tar"), atlasDir);

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
			e.printStackTrace();
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

	protected void createMapTar(File atlasLayerDir) {
		String mapName = atlasLayerDir.getParentFile().getName();
		String layerName = atlasLayerDir.getName();
		File atlasTarMapDir = new File(atlasTarDir, mapName);
		File atlasTarLayerDir = new File(atlasTarMapDir, layerName);
		atlasTarLayerDir.mkdirs();

		List<File> folderContent = TarUtilities.getDirectoryContent(new File(atlasLayerDir, "set"));

		List<PreparedTarEntry> preparedEntries = new ArrayList<PreparedTarEntry>(folderContent
				.size() + 1);

		File mapFile = new File(atlasLayerDir, layerName + ".map");
		PreparedTarEntry preparedMapFileEntry = new PreparedTarEntry(mapFile, atlasLayerDir);
		preparedEntries.add(preparedMapFileEntry);

		// Calculate the tmi file size - as the tmi is the first file in the tar
		// archive every block offset of files after depend on the tmi file
		// size.
		int tmiFileSize = preparedMapFileEntry.getTmiEntryLength();
		for (File f : folderContent) {
			PreparedTarEntry entry = new PreparedTarEntry(f, atlasLayerDir);
			preparedEntries.add(entry);
			tmiFileSize += entry.getTmiEntryLength();
		}

		int tmiBlocks = TarRecord.calculateFileSizeInTar(tmiFileSize) / 512;
		int blocksUsed = 1 + tmiBlocks;

		StringWriter sw = new StringWriter(50 * folderContent.size());
		for (PreparedTarEntry entry : preparedEntries) {
			sw.write(String.format("block %10d: %s\r\n", new Object[] { blocksUsed,
					entry.getTarHeader().getFileName() }));
			blocksUsed += entry.getTarBlocksRequired();
		}

		byte[] tmiFileData = null;
		try {
			tmiFileData = sw.toString().getBytes("ASCII");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		TarArchive ta = null;
		try {
			ta = new TarArchive(new File(atlasTarLayerDir, layerName + ".tar"), atlasLayerDir);
			ta.writeFileFromData(layerName + ".tmi", tmiFileData);
			for (PreparedTarEntry entry : preparedEntries) {
				ta.writePreparedEntry(entry);
			}
			ta.writeEndofArchive();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ta != null)
				ta.close();
		}
	}
}
