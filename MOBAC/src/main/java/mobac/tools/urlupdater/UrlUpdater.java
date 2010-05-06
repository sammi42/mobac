package mobac.tools.urlupdater;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import mobac.mapsources.MapSourcesUpdater;
import mobac.program.Logging;
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
		MapSourcesUpdater.loadMapSourceProperties(mapSourcesProperties);
		System.getProperties().putAll(mapSourcesProperties);
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
			fo = new FileOutputStream("src/mobac/mapsources.properties");
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
		getInstance();
		new GoogleUrlUpdater().run();
		new YandexUrlUpdater().run();
		getInstance().writeUpdatedMapsourcesPropertiesFile();
	}

}
