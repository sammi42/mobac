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

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobac.program.Logging;
import mobac.utilities.Utilities;

public class YandexUrlUpdater implements Runnable {

	private static final String UPDATE_URL = "http://api-maps.yandex.ru/1.1.7/xml/data.xml";
	private static final Pattern MAP_VER_PATTERN = Pattern
			.compile("Internal.MapData.DataVersions=\\{([^\\}]*)\\}");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logging.disableLogging();
		new YandexUrlUpdater().run();
		UrlUpdater.getInstance().writeUpdatedMapsourcesPropertiesFile();
	}

	public void run() {
		Pattern pa = Pattern.compile("(.*):\\\"(.*)\\\"");
		UrlUpdater urlUpdater = UrlUpdater.getInstance();
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) new URL(UPDATE_URL).openConnection();
			conn.connect();
			byte[] data = Utilities.getInputBytes(conn.getInputStream());
			String dataStr = new String(data);
			Matcher m1 = MAP_VER_PATTERN.matcher(dataStr);
			if (!m1.find()) {
				System.out.println("Not found!");
				return;
			}
			String mapVersionStr = m1.group(1);

			String[] mapVersions = mapVersionStr.split(",");

			for (String s : mapVersions) {
				Matcher m2 = pa.matcher(s);
				if (!m2.matches())
					continue;
				String type = m2.group(1);
				String version = m2.group(2);
				String key = null;
				if ("map".equals(type))
					key = "YandexMap.url";
				else if ("sat".equals(type))
					key = "YandexSat.url";
				if (key == null)
					continue;
				String url = urlUpdater.getMapSourceUrl(key);
				String newUrl = url.replaceFirst("&v=[^&]+&", "&v=" + version + "&");
				if (!newUrl.equals(url)) {
					System.out.println(key);
					urlUpdater.updateMapSopurceUrl(key, newUrl);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
