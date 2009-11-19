package tac.tools;

import java.util.Vector;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.mapsources.MapSourcesManager;
import tac.program.Logging;

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
