package mobac.mapsources.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import mobac.mapsources.impl.BeanShellHttpMapSource;
import mobac.program.interfaces.MapSource;
import mobac.utilities.file.FileExtFilter;

import org.apache.log4j.Logger;

public class BeanShellMapSourceLoader {

	private final Logger log = Logger.getLogger(BeanShellMapSourceLoader.class);
	private final File mapSourcesDir;
	private ArrayList<MapSource> mapSources;

	public BeanShellMapSourceLoader(File mapSourcesDir) {
		this.mapSourcesDir = mapSourcesDir;
		mapSources = new ArrayList<MapSource>();
	}

	public void loadBeanShellMapSources() {
		File[] customMapSourceFiles = mapSourcesDir.listFiles(new FileExtFilter(".bsh"));
		for (File f : customMapSourceFiles) {
			try {
				BeanShellHttpMapSource mapSource = BeanShellHttpMapSource.load(f);
				log.trace("BeanShell map source loaded: " + mapSource + " from file \"" + f.getName() + "\"");
				mapSources.add(mapSource);
			} catch (Exception e) {
				log.error("failed to load custom map source \"" + f.getName() + "\": " + e.getMessage(), e);
				JOptionPane.showMessageDialog(null, "msg", "title", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public List<MapSource> getMapSources() {
		return mapSources;
	}
}
