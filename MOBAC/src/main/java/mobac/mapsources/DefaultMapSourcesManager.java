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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import mobac.mapsources.impl.DebugMapSource;
import mobac.mapsources.impl.LocalhostTestSource;
import mobac.mapsources.impl.SimpleMapSource;
import mobac.mapsources.loader.BeanShellMapSourceLoader;
import mobac.mapsources.loader.CustomMapSourceLoader;
import mobac.mapsources.loader.MapPackManager;
import mobac.program.DirectoryManager;
import mobac.program.interfaces.MapSource;
import mobac.program.model.Settings;
import mobac.program.model.TileImageType;

public class DefaultMapSourcesManager extends MapSourcesManager {

	private LinkedHashMap<String, MapSource> allMapSources = new LinkedHashMap<String, MapSource>(50);

	static {
		MapSourcesUpdater.loadMapSourceProperties();
	}

	public DefaultMapSourcesManager() {
	}

	protected void loadMapSources() {
		if (Settings.getInstance().isDevModeEnabled()) {
			addMapSource(new LocalhostTestSource("Localhost", TileImageType.PNG));
			addMapSource(new DebugMapSource());
		}

		// Check for user specific configuration of mapsources directory
		String mapSourcesDirCfg = Settings.getInstance().directories.mapSourcesDirectory;
		File mapSourcesDir;
		if (mapSourcesDirCfg == null || mapSourcesDirCfg.trim().length() == 0)
			mapSourcesDir = DirectoryManager.mapSourcesDir;
		else
			mapSourcesDir = new File(mapSourcesDirCfg);
		try {
			MapPackManager mpm = new MapPackManager(mapSourcesDir);
			mpm.installUpdates();
			mpm.loadMapPacks();
			addAllMapSource(mpm.getMapSources());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		}
		CustomMapSourceLoader cmsl = new CustomMapSourceLoader(mapSourcesDir);
		cmsl.loadCustomMapSources();
		addAllMapSource(cmsl.getMapSources());
		BeanShellMapSourceLoader bsmsl = new BeanShellMapSourceLoader(mapSourcesDir);
		bsmsl.loadBeanShellMapSources();
		addAllMapSource(bsmsl.getMapSources());
		if (allMapSources.size() == 0)
			addMapSource(new SimpleMapSource());
	}

	protected void addAllMapSource(List<MapSource> mapSourceColl) {
		for (MapSource mapSource : mapSourceColl)
			addMapSource(mapSource);
	}

	protected void addMapSource(MapSource mapSource) {
		MapSource old = allMapSources.put(mapSource.getName(), mapSource);
		if (old != null)
			throw new RuntimeException("Duplicate map source name: " + mapSource.getName());
	}

	public static void initialize() {
		INSTANCE = new DefaultMapSourcesManager();
		((DefaultMapSourcesManager) INSTANCE).loadMapSources();
	}

	@Override
	public Vector<MapSource> getAllMapSources() {
		return new Vector<MapSource>(allMapSources.values());
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
		TreeSet<String> disabledMapSources = new TreeSet<String>(Settings.getInstance().getDisabledMapSources());
		for (MapSource ms : allMapSources.values()) {
			if (!disabledMapSources.contains(ms.getName()))
				mapSources.add(ms);
		}
		return mapSources;
	}

	@Override
	public MapSource getDefaultMapSource() {
		MapSource ms = getSourceByName("Mapnik");// DEFAULT;
		if (ms != null)
			return ms;
		// Fallback: return first
		return allMapSources.values().iterator().next();
	}

	@Override
	public MapSource getSourceByName(String name) {
		return allMapSources.get(name);
	}

}
