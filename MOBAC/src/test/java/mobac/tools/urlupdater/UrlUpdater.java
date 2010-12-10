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
package mobac.tools.urlupdater;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import mobac.mapsources.DefaultMapSourcesManager;
import mobac.program.Logging;
import mobac.program.ProgramInfo;
import mobac.utilities.Utilities;

public class UrlUpdater {

	static List<String> KEYS = new ArrayList<String>();

	static {
		KEYS.add("mapsources.Date");
		KEYS.add("mapsources.Rev");
	}

	private Properties mapSourcesProperties = new Properties();

	private int updatedUrlsCount = 0;

	private static UrlUpdater INSTANCE = null;

	public static UrlUpdater getInstance() {
		if (INSTANCE == null)
			INSTANCE = new UrlUpdater();
		return INSTANCE;
	}

	private UrlUpdater() {
		FileInputStream in = null;
		try {
			in = new FileInputStream("src/main/resources/mobac/mapsources.properties");
			mapSourcesProperties.load(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Utilities.closeStream(in);
		}
	}

	public void updateMapSopurceUrl(String mapKey, String newUrl) {
		mapSourcesProperties.put(mapKey, newUrl);
		updatedUrlsCount++;
	}

	public String getMapSourceUrl(String mapKey) {
		return mapSourcesProperties.getProperty(mapKey);
	}

	public void writeUpdatedMapsourcesPropertiesFile() {
		if (updatedUrlsCount == 0)
			return;
		ByteArrayOutputStream bo = new ByteArrayOutputStream(4096);
		PrintWriter pw = new PrintWriter(bo, true);

		for (String key : KEYS) {
			pw.println(key + "=" + mapSourcesProperties.getProperty(key));
			mapSourcesProperties.remove(key);
		}

		Enumeration<Object> enu = mapSourcesProperties.keys();
		Vector<String> keyList = new Vector<String>();
		while (enu.hasMoreElements())
			keyList.add((String) enu.nextElement());
		Collections.sort(keyList);

		for (String key : keyList)
			pw.println(key + "=" + mapSourcesProperties.getProperty(key));

		pw.flush();
		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream("src/main/resources/mobac/mapsources.properties");
			fo.write(bo.toByteArray());
			System.out.println("mapsources.properties has been updated");
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			Utilities.closeStream(fo);
		}
	}

	public int getUpdatedUrlsCount() {
		return updatedUrlsCount;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logging.disableLogging();
		ProgramInfo.initialize();
		DefaultMapSourcesManager.initialize();
		getInstance();
		new GoogleUrlUpdater().run();
		new YandexUrlUpdater().run();
		getInstance().writeUpdatedMapsourcesPropertiesFile();
	}

}
