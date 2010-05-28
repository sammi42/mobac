package mobac.mapsources;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobac.Main;
import mobac.exceptions.MapSourcesUpdateException;
import mobac.program.DirectoryManager;
import mobac.program.model.Settings;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.Utilities;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class MapSourcesUpdater {

	private static boolean mapSourcesExternalFileUsed = false;

	private static final Logger log = Logger.getLogger(MapSourcesUpdater.class);
	private static final String MAPSOURCES_REV_KEY = "mapsources.Rev";
	private static final String MAPSOURCES_DATE_KEY = "mapsources.Date";
	private static final String MAPSOURCES_PROPERTIES = "mapsources.properties";

	/**
	 * Extracts the revision number from the Subversion keyword entry rev/revision
	 */
	private static final Pattern SVN_REV = Pattern.compile("\\$Rev\\:\\s*(\\d*)\\s*\\$");

	/**
	 * Extracts the important part of the Subversion keyword entry Date/LastChangedDate so that it can be parsed by
	 * {@link MapSourcesUpdater#SVN_DATE_FORMAT}
	 */
	private static final Pattern SVN_DATE = Pattern.compile("\\$Date\\:\\s*([\\d\\s:\\-\\+]*) \\(.*\\)\\s*\\$");
	/**
	 * Date format for parsing the Subversion date keyword content
	 */
	private static final SimpleDateFormat SVN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

	/**
	 * Merges the mapsources property into the system property bundle
	 */
	public static void loadMapSourceProperties() {
		Properties systemProps = System.getProperties();
		MapSourcesUpdater.loadMapSourceProperties(systemProps);
	}

	public static int getMapSourcesRev(Properties p) {
		String revS = p.getProperty(MapSourcesUpdater.MAPSOURCES_REV_KEY);
		if (revS == null)
			return -1;
		return parseMapSourcesRev(revS);
	}

	private static int parseMapSourcesRev(String s) {
		if (s == null)
			return -1;
		Matcher m = MapSourcesUpdater.SVN_REV.matcher(s);
		if (!m.matches())
			return -1;
		s = m.group(1);
		return Integer.parseInt(s);
	}

	public static Date getMapSourcesDate(Properties p) {
		String revS = p.getProperty(MapSourcesUpdater.MAPSOURCES_DATE_KEY);
		if (revS == null)
			return null;
		return parseMapSourcesDate(revS);
	}

	private static Date parseMapSourcesDate(String s) {
		if (s == null)
			return null;
		Matcher m = MapSourcesUpdater.SVN_DATE.matcher(s);
		if (!m.matches())
			return null;
		String part = m.group(1);
		try {
			return MapSourcesUpdater.SVN_DATE_FORMAT.parse(part);
		} catch (ParseException e) {
			MapSourcesUpdater.log.error("", e);
			return null;
		}
	}

	/**
	 * This method is automatically called each time MOBAC starts-up. If the last update check is older than three days
	 * a new update check is performed.
	 * 
	 * @param async
	 *            <code>true</code>: run the update in a new background. Otherwise wait for the update to finish
	 */
	public static void automaticMapsourcesOnlineUpdate(boolean async) {
		Date lastUpdate = Settings.getInstance().mapSourcesUpdate.lastUpdate;
		if (lastUpdate == null)
			lastUpdate = getMapSourcesDate(System.getProperties());
		Date end = new Date();
		long diff = end.getTime() - lastUpdate.getTime();
		diff /= 1000 * 60 * 60 * 24;
		if (diff < 4) // check for an update every 4 days
			return;
		Runnable r = new Runnable() {

			public void run() {
				try {
					MapSourcesUpdater.log.info("Performing a map sources update");
					boolean result = mapsourcesOnlineUpdate();
					if (result)
						MapSourcesUpdater.log.info("Updated map sources file retrieved");
					else
						MapSourcesUpdater.log.info("No new update available");
				} catch (MapSourcesUpdateException e) {
					MapSourcesUpdater.log.error("Scheduled map sources update failed:", e);
				}
			}
		};
		if (async)
			new Thread(r, "MapSourcesUpdate").start();
		else
			r.run();
	}

	/**
	 * Performs the map source online update check.
	 * 
	 * @return <ul>
	 *         <li>0: mapsources.properties is up-to-date (no update available)</li>
	 *         </ul>
	 */
	public static boolean mapsourcesOnlineUpdate() throws MapSourcesUpdateException {
		URL url;
		try {
			File mapFile = new File(DirectoryManager.currentDir, MapSourcesUpdater.MAPSOURCES_PROPERTIES);
			String mapUpdateUrl = System.getProperty("mobac.updateurl");
			if (mapUpdateUrl == null)
				throw new MapSourcesUpdateException("No update url configured!");
			url = new URL(mapUpdateUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			Settings settings = Settings.getInstance();
			if (mapFile.isFile() && settings.mapSourcesUpdate.etag != null && settings.mapSourcesUpdate.etag != "")
				conn.addRequestProperty("If-None-Match", settings.mapSourcesUpdate.etag);
			try { // TODO temporarily introduced try/catch due to uncaught exception
				int code = conn.getResponseCode();
				MapSourcesUpdater.log.trace("Mapsources online update: \n\tUpdate url: " + mapUpdateUrl
						+ "\n\tResponse  : " + code + " " + conn.getResponseMessage() + "\n\tSize      : "
						+ conn.getContentLength() + " bytes \n\tETag      : " + conn.getHeaderField("ETag"));
				if (code == 304)
					// HTTP 304 = Not Modified => Same as on last update check
					return false;
				if (code != 200)
					throw new MapSourcesUpdateException("Invalid HTTP server response: " + code + " "
							+ conn.getResponseMessage());
				DataInputStream in = new DataInputStream(conn.getInputStream());

				if (conn.getContentLength() == 0)
					// If there is only an empty file available this indicates that
					// the mapsources format has changed and requires a new version
					// of Mobile Atlas Creator
					throw new MapSourcesUpdateException(
							"This version of Mobile Atlas Creator is no longer supported. \n"
									+ "Please update to the current version.");
				byte[] data = new byte[conn.getContentLength()];
				in.readFully(data);
				in.close();
				conn.disconnect(); // We don't need a connection to this server in
				// near future
				Properties onlineProps = new Properties();
				onlineProps.load(new ByteArrayInputStream(data));
				// int onlineRev = getMapSourcesRev(onlineProps);
				// int currentRev = parseMapSourcesRev(System.getProperty(MapSourcesUpdater.MAPSOURCES_REV_KEY));
				settings.mapSourcesUpdate.lastUpdate = new Date();
				settings.mapSourcesUpdate.etag = conn.getHeaderField("ETag");
				System.getProperties().putAll(onlineProps);
				FileOutputStream mapFs = null;
				try {
					mapFs = new FileOutputStream(mapFile);
					mapFs.write(data);
				} finally {
					Utilities.closeStream(mapFs);
				}
				for (MapSource ms : MapSourcesManager.getAllMapSources()) {
					if (ms instanceof UpdatableMapSource) {
						((UpdatableMapSource) ms).update();
					}
				}
				mapSourcesExternalFileUsed = true;
				return true;
			} catch (java.net.UnknownHostException e) {
				// TODO catch host unreachable:
				// 19:14:20,021 ERROR [MapSourcesUpdate] MapSourcesUpdater: mobac.dnsalias.org
				// java.net.UnknownHostException: mobac.dnsalias.org
				// at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:177)
				// at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:366)
				// at java.net.Socket.connect(Socket.java:525)
				return false;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new MapSourcesUpdateException(e);
		}
	}

	/**
	 * Merges the mapsources property into the Properties <code>targetprop</code>
	 * 
	 * @param targetProp
	 */
	public static void loadMapSourceProperties(Properties targetProp) {
		try {
			URL mapResUrl = Main.class.getResource(MapSourcesUpdater.MAPSOURCES_PROPERTIES);
			File mapFile = new File(DirectoryManager.currentDir, MapSourcesUpdater.MAPSOURCES_PROPERTIES);
			Properties resProps = new Properties();
			Properties fileProps = new Properties();
			Utilities.loadProperties(resProps, mapResUrl);
			Properties selectedProps;
			if (mapFile.isFile()) {
				Utilities.loadProperties(fileProps, mapFile);
				int fileRev = getMapSourcesRev(fileProps);
				int resRev = getMapSourcesRev(resProps);
				MapSourcesUpdater.log.trace("mapsources.properties revisons (resource/file): " + resRev + " / "
						+ fileRev);
				selectedProps = (fileRev < resRev) ? resProps : fileProps;
			} else {
				selectedProps = resProps;
			}
			mapSourcesExternalFileUsed = (selectedProps != resProps);
			if (mapSourcesExternalFileUsed)
				log.debug("Used mapsources.properties: file");
			else
				log.debug("Used mapsources.properties: resource");
			targetProp.putAll(selectedProps);
		} catch (Exception e) {
			MapSourcesUpdater.log.error("Error while reading mapsources.properties: ", e);
			GUIExceptionHandler.showExceptionDialog(e);
		}
	}

}
