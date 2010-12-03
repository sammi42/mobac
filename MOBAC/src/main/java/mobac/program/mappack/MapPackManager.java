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
package mobac.program.mappack;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import mobac.program.Logging;
import mobac.program.interfaces.MapSource;
import mobac.utilities.Utilities;
import mobac.utilities.file.FileExtFilter;

import org.apache.log4j.Logger;

public class MapPackManager {

	private static final String MAP_PACK_PACKAGE = "mobac.mapsources.mappacks";

	private final Logger log = Logger.getLogger(MapPackManager.class);

	private final int requiredMapPackVersion;

	private final File mapPackDir;

	private ArrayList<MapSource> mapSources;

	public MapPackManager(File mapPackDir) {
		this.mapPackDir = mapPackDir;
		mapSources = new ArrayList<MapSource>();
		requiredMapPackVersion = Integer.parseInt(System.getProperty("mobac.mappackversion"));
	}

	public MapSource[] getMapSources() {
		MapSource[] ms = new MapSource[mapSources.size()];
		mapSources.toArray(ms);
		return ms;
	}

	public void installUpdates() throws IOException {
		File[] newMapPacks = mapPackDir.listFiles(new FileExtFilter(".jar.new"));
		for (File newMapPack : newMapPacks) {
			String name = newMapPack.getName();
			name = name.substring(0, name.length() - 4); // remove ".new"
			File oldMapPack = new File(mapPackDir, name);
			if (oldMapPack.isFile()) {
				// TODO: Check if new map pack file is still compatible

				Utilities.deleteFile(oldMapPack);
			}
			newMapPack.renameTo(oldMapPack);
		}
	}

	public void loadMapPacks() throws IOException {
		File[] mapPacks = mapPackDir.listFiles(new FileExtFilter(".jar"));
		ArrayList<URL> urlList = new ArrayList<URL>();
		for (File mapPackFile : mapPacks) {
			try {
				testMapPack(mapPackFile);
				URL url = mapPackFile.toURI().toURL();
				urlList.add(url);
			} catch (IOException e) {
				log.error("Failed to load map pack: " + mapPackFile);
			}
		}
		URL[] urls = new URL[urlList.size()];
		urlList.toArray(urls);

		ClassLoader urlCl = new MapPackClassLoader(MAP_PACK_PACKAGE, urls, ClassLoader.getSystemClassLoader());

		final Iterator<MapSource> iterator = ServiceLoader.load(MapSource.class, urlCl).iterator();
		while (iterator.hasNext()) {
			try {
				MapSource ms = iterator.next();
				mapSources.add(ms);
				System.out.println(ms);
			} catch (Error e) {
				log.error("Faild to load a map source from map pack: " + e.getMessage(), e);
			}
		}

	}

	protected void testMapPack(File mapPackFile) throws IOException {
		String fileName = mapPackFile.getName();
		JarFile jf = new JarFile(mapPackFile);
		try {
			Manifest mf = jf.getManifest();
			Attributes a = mf.getMainAttributes();
			String mpv = a.getValue("MapPackVersion");
			if (mpv == null)
				throw new IOException("MapPackVersion info missing!");
			int mapPackVersion = Integer.parseInt(mpv);
			if (requiredMapPackVersion != mapPackVersion)
				throw new IOException("This pack \"" + fileName + "\" is not compatible with this MOBAC version.");
			ZipEntry entry = jf.getEntry("META-INF/services/mobac.program.interfaces.MapSource");
			if (entry == null)
				throw new IOException("MapSources services list is missing in file " + fileName);
		} finally {
			jf.close();
		}

	}

	public static void main(String[] args) {
		Logging.configureConsoleLogging();
		System.setProperty("mobac.mappackversion", "1");
		try {
			MapPackManager mpm = new MapPackManager(new File("mapsources"));
			mpm.loadMapPacks();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
