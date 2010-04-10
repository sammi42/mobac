package mobac.tools.urlupdater;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobac.utilities.Utilities;

public class YandexUrlUpdater {

	private static final String UPDATE_URL = "http://api-maps.yandex.ru/1.1.7/xml/data.xml";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Pattern mapVersionsPattern = Pattern
				.compile("Internal.MapData.DataVersions=\\{([^\\}]*)\\}");
		Pattern pa = Pattern.compile("(.*):\\\"(.*)\\\"");
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) new URL(UPDATE_URL).openConnection();
			conn.connect();
			byte[] data = Utilities.getInputBytes(conn.getInputStream());
			String dataStr = new String(data);
			Matcher m1 = mapVersionsPattern.matcher(dataStr);
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
				System.out.println(type + " " + version);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
