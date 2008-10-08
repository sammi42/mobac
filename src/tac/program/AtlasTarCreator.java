package tac.program;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import tac.tar.TarArchive;
import tac.tar.TarUtilities;
import tac.utilities.Utilities;

/**
 * 
 * <a href="http://linuxtechs.net/kruch/tb/forum/viewtopic.php?t=21">TrekBuddy
 * Atlas format description</a>
 * 
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
		TarArchive ta = null;
		try {
			ta = new TarArchive(new File(atlasTarLayerDir, layerName + ".tar"), atlasLayerDir);
			ta.writeFile(new File(atlasLayerDir, layerName + ".map"));
			ta.writeContentFromDir(new File(atlasLayerDir, "set"));
			ta.writeEndofArchive();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ta != null)
				ta.close();
		}
	}
}
