/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.mapsources;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Vector;

import mobac.mapsources.impl.DebugMapSource;
import mobac.mapsources.impl.LocalhostTestSource;
import mobac.mapsources.loader.CustomMapSourceLoader;
import mobac.mapsources.loader.MapPackManager;
import mobac.program.DirectoryManager;
import mobac.program.interfaces.MapSource;
import mobac.program.model.Settings;
import mobac.program.model.TileImageType;

public class DefaultMapSourcesManager extends MapSourcesManager {

	// public static final MapSource DEFAULT = new Mapnik();
	private ArrayList<MapSource> MAP_SOURCES = new ArrayList<MapSource>(30);

	private static MapSource LOCALHOST_TEST_MAPSOURCE = new LocalhostTestSource("Localhost", TileImageType.PNG);
	private static MapSource DEBUG_TEST_MAPSOURCE = new DebugMapSource();

	static {
		MapSourcesUpdater.loadMapSourceProperties();
	}

	public DefaultMapSourcesManager() {
		File mapSourcesDir = new File(DirectoryManager.programDir, "mapsources");
		try {
			MapPackManager mpm = new MapPackManager(mapSourcesDir);
			mpm.loadMapPacks();
			MAP_SOURCES.addAll(mpm.getMapSources());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		}
		CustomMapSourceLoader cmsl = new CustomMapSourceLoader(mapSourcesDir);
		cmsl.loadCustomMapSources();
		MAP_SOURCES.addAll(cmsl.getMapSources());
	}

	public static void initialize() {
		INSTANCE = new DefaultMapSourcesManager();
	}

	@Override
	public Vector<MapSource> getAllMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>();
		if (Settings.getInstance().isDevModeEnabled()) {
			mapSources.add(LOCALHOST_TEST_MAPSOURCE);
			mapSources.add(DEBUG_TEST_MAPSOURCE);
		}
		for (MapSource ms : MAP_SOURCES)
			mapSources.add(ms);
		return mapSources;
	}

	@Override
	public Vector<MapSource> getAllLayerMapSources() {
		Vector<MapSource> all = getAllMapSources();
		TreeSet<MapSource> uniqueSources = new TreeSet<MapSource>(new Comparator<MapSource>() {

			public int compare(MapSource o1, MapSource o2) {
				return o1.getName().compareTo(o2.getName());
			}

		});
		for (MapSource ms : all) {
			if (ms instanceof AbstractMultiLayerMapSource) {
				for (MapSource lms : ((AbstractMultiLayerMapSource) ms)) {
					uniqueSources.add(lms);
				}
			} else
				uniqueSources.add(ms);
		}
		Vector<MapSource> result = new Vector<MapSource>(uniqueSources);
		return result;
	}

	@Override
	public Vector<MapSource> getEnabledMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>();
		if (Settings.getInstance().isDevModeEnabled()) {
			mapSources.add(LOCALHOST_TEST_MAPSOURCE);
			mapSources.add(DEBUG_TEST_MAPSOURCE);
		}
		TreeSet<String> disabledMapSources = new TreeSet<String>(Settings.getInstance().getDisabledMapSources());
		for (MapSource ms : MAP_SOURCES) {
			if (!disabledMapSources.contains(ms.getName()))
				mapSources.add(ms);
		}
		return mapSources;
	}

	@Override
	public MapSource getDefaultMapSource() {
		return getSourceByName("Mapnik");// DEFAULT;
	}

	@Override
	public MapSource getSourceByName(String name) {
		for (MapSource ms : MAP_SOURCES) {
			if (ms.getName().equals(name))
				return ms;
		}
		if (Settings.getInstance().isDevModeEnabled()) {
			if (LOCALHOST_TEST_MAPSOURCE.getName().equals(name))
				return LOCALHOST_TEST_MAPSOURCE;
			if (DEBUG_TEST_MAPSOURCE.getName().equals(name))
				return DEBUG_TEST_MAPSOURCE;
		}
		return null;
	}

}
