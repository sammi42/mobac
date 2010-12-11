package mobac.mapsources.loader;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

import mobac.mapsources.custom.CustomMapSource;
import mobac.mapsources.custom.CustomMultiLayerMapSource;
import mobac.program.interfaces.MapSource;
import mobac.utilities.file.FileExtFilter;

import org.apache.log4j.Logger;

public class CustomMapSourceLoader implements ValidationEventHandler {

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
			unmarshaller.setEventHandler(this);
		} catch (JAXBException e) {
			throw new RuntimeException("Unable to create JAXB context for custom map sources", e);
		}
		File[] customMapSourceFiles = mapSourcesDir.listFiles(new FileExtFilter(".xml"));
		for (File f : customMapSourceFiles) {
			try {
				MapSource customMapSource = (MapSource) unmarshaller.unmarshal(f);
				log.trace("Custom map source loaded: " + customMapSource + " from file \"" + f.getName() + "\"");
				mapSources.add(customMapSource);
			} catch (Exception e) {
				log.error("failed to load custom map source \"" + f.getName() + "\": " + e.getMessage(), e);
			}
		}
	}

	public List<MapSource> getMapSources() {
		return mapSources;
	}

	public boolean handleEvent(ValidationEvent event) {
		ValidationEventLocator loc = event.getLocator();
		String file = loc.getURL().getFile();
		try {
			file = URLDecoder.decode(file, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		int lastSlash = file.lastIndexOf('/');
		if (lastSlash > 0)
			file = file.substring(lastSlash + 1);
		JOptionPane.showMessageDialog(null, "<html><h3>Failed to load a custom map</h3><p><i>" + event.getMessage()
				+ "</i></p><br><p>file: \"<b>" + file + "</b>\"<br>line/column: <i>" + loc.getLineNumber() + "/"
				+ loc.getColumnNumber() + "</i></p>", "Error: custom map loading failed", JOptionPane.ERROR_MESSAGE);
		log.error(event.toString());
		return false;
	}

}
