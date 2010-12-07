package mobac.mapsources.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import mobac.mapsources.custom.CustomMapSource;
import mobac.mapsources.custom.CustomMultiLayerMapSource;
import mobac.program.interfaces.MapSource;
import mobac.utilities.Utilities;
import mobac.utilities.file.FileExtFilter;

import org.apache.log4j.Logger;

public class CustomMapSourceLoader {

	private final Logger log = Logger.getLogger(MapPackManager.class);
	private final File mapSourcesDir;
	private ArrayList<MapSource> mapSources;

	public CustomMapSourceLoader(File mapSourcesDir) {
		this.mapSourcesDir = mapSourcesDir;
		mapSources = new ArrayList<MapSource>();
	}

	public void loadCustomMapSources() {
		JAXBContext context;
		Unmarshaller unmarshaller;
		try {
			context = JAXBContext.newInstance(new Class[] { CustomMapSource.class, CustomMultiLayerMapSource.class });
			unmarshaller = context.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException("Unable to create JAXB context for custom map sources", e);
		}
		File[] customMapSourceFiles = mapSourcesDir.listFiles(new FileExtFilter(".xml"));
		for (File f : customMapSourceFiles) {
			InputStream in = null;
			try {
				in = new FileInputStream(f);
				MapSource customMapSource = (MapSource) unmarshaller.unmarshal(in);
				mapSources.add(customMapSource);
			} catch (Exception e) {
				log.error("failed to load custom map source \"" + f.getName() + "\": " + e.getMessage(), e);
			} finally {
				Utilities.closeStream(in);
			}
		}
	}

	public List<MapSource> getMapSources() {
		return mapSources;
	}
}
