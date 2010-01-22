package mobac.tools;

import java.util.Vector;

import mobac.mapsources.MapSourcesManager;
import mobac.program.Logging;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


public class MapUpdateTypeLister {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logging.configureConsoleLogging();
		Vector<MapSource> mapSources = MapSourcesManager.getAllMapSources();
		for (MapSource mapSource : mapSources) {
			String name = mapSource.getName();
			name = name.substring(0, Math.min(25,name.length())); 
			System.out.println(String.format("%25s  %s", name, mapSource.getTileUpdate()));
		}
	}
}
