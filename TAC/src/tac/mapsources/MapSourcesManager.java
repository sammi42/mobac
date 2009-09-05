package tac.mapsources;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.Main;
import tac.exceptions.MapSourcesUpdateException;
import tac.mapsources.impl.LocalhostTestSource;
import tac.mapsources.impl.MiscMapSources;
import tac.mapsources.impl.Google.GoogleEarth;
import tac.mapsources.impl.Google.GoogleMapMaker;
import tac.mapsources.impl.Google.GoogleMaps;
import tac.mapsources.impl.Google.GoogleMapsChina;
import tac.mapsources.impl.Google.GoogleTerrain;
import tac.mapsources.impl.Microsoft.MicrosoftHybrid;
import tac.mapsources.impl.Microsoft.MicrosoftMaps;
import tac.mapsources.impl.Microsoft.MicrosoftMapsChina;
import tac.mapsources.impl.Microsoft.MicrosoftVirtualEarth;
import tac.mapsources.impl.MiscMapSources.MultimapCom;
import tac.mapsources.impl.MiscMapSources.MultimapOSUkCom;
import tac.mapsources.impl.OsmMapSources.CycleMap;
import tac.mapsources.impl.OsmMapSources.Mapnik;
import tac.mapsources.impl.OsmMapSources.OsmHikingMap;
import tac.mapsources.impl.OsmMapSources.TilesAtHome;
import tac.mapsources.impl.RegionalMapSources.Cykloatlas;
import tac.mapsources.impl.RegionalMapSources.DoCeluPL;
import tac.mapsources.impl.RegionalMapSources.OutdooractiveCom;
import tac.mapsources.impl.RegionalMapSources.UmpWawPl;
import tac.mapsources.impl.WmsSources.TerraserverUSA;
import tac.program.model.Settings;
import tac.utilities.Utilities;

public class MapSourcesManager {

	private static final Logger log = Logger.getLogger(MapSourcesManager.class);

	public static final String MAPSOURCES_REV_KEY = "mapsources.Rev";
	public static final String MAPSOURCES_DATE_KEY = "mapsources.Date";

	/**
	 * Extracts the revision number from the Subversion keyword entry
	 * rev/revision
	 */
	static final Pattern SVN_REV = Pattern.compile("\\$Rev\\:\\s*(\\d*)\\s*\\$");

	/**
	 * Extracts the important part of the Subversion keyword entry
	 * Date/LastChangedDate so that it can be parsed by {@link #SVN_DATE_FORMAT}
	 */
	static final Pattern SVN_DATE = Pattern
			.compile("\\$Date\\:\\s*([\\d\\s:\\-\\+]*) \\(.*\\)\\s*\\$");
	/**
	 * Date format for parsing the Subversion date keyword content
	 */
	static final SimpleDateFormat SVN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

	private static final String MAPSOURCES_UPDATE_URL = "http://trekbuddyatlasc.sourceforge.net/"
			+ "mapsources-update/v1/mapsources.properties";

	private static boolean mapSourcesExternalFileUsed = false;

	public static final MapSource DEFAULT = new Mapnik();
	private static MapSource[] MAP_SOURCES;

	private static MapSource LOCALHOST_TEST_MAPSOURCE = new LocalhostTestSource();

	static {
		loadMapSourceProperties();
		MAP_SOURCES = new MapSource[] { //
				//
				new GoogleMaps(), new GoogleMapMaker(), new GoogleMapsChina(), new GoogleEarth(),
				new GoogleTerrain(), new MiscMapSources.YahooMaps(), DEFAULT, new TilesAtHome(),
				new CycleMap(), new OsmHikingMap(), /* new OpenArialMap(), */new MicrosoftMaps(),
				new MicrosoftMapsChina(), new MicrosoftVirtualEarth(), new MicrosoftHybrid(),
				new OutdooractiveCom(), new MultimapCom(), new MultimapOSUkCom(), new Cykloatlas(),
				new TerraserverUSA(), new UmpWawPl(), new DoCeluPL(), };
	}

	public static Vector<MapSource> getAllMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>();
		if (Settings.getInstance().isDevModeEnabled())
			mapSources.add(new LocalhostTestSource());
		for (MapSource ms : MAP_SOURCES)
			mapSources.add(ms);
		for (MapSource ms : Settings.getInstance().customMapSources)
			mapSources.add(ms);
		return mapSources;
	}

	public static Vector<MapSource> getEnabledMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>();
		if (Settings.getInstance().isDevModeEnabled())
			mapSources.add(LOCALHOST_TEST_MAPSOURCE);
		TreeSet<String> disabledMapSources = new TreeSet<String>(Settings.getInstance()
				.getDisabledMapSources());
		for (MapSource ms : MAP_SOURCES) {
			if (!disabledMapSources.contains(ms.getName()))
				mapSources.add(ms);
		}
		for (MapSource ms : Settings.getInstance().customMapSources)
			mapSources.add(ms);
		return mapSources;
	}

	public static String getDefaultMapSourceName() {
		return DEFAULT.getName();
	}

	public static MapSource getSourceByName(String name) {
		for (MapSource ms : MAP_SOURCES) {
			if (ms.getName().equals(name))
				return ms;
		}
		for (MapSource ms : Settings.getInstance().customMapSources) {
			if (ms.getName().equals(name))
				return ms;
		}
		if (Settings.getInstance().isDevModeEnabled()
				&& LOCALHOST_TEST_MAPSOURCE.getName().equals(name))
			return LOCALHOST_TEST_MAPSOURCE;
		return null;
	}

	/**
	 * Merges the mapsources property into the system property bundle
	 */
	public static void loadMapSourceProperties() {
		Properties systemProps = System.getProperties();
		loadMapSourceProperties(systemProps);
	}

	/**
	 * Merges the mapsources property into the Properties
	 * <code>targetprop</code>
	 * 
	 * @param targetProp
	 */
	public static void loadMapSourceProperties(Properties targetProp) {
		try {
			URL mapResUrl = Main.class.getResource("mapsources.properties");
			File mapFile = new File(Settings.getUserDir(), "mapsources.properties");
			Properties resProps = new Properties();
			Properties fileProps = new Properties();
			Utilities.loadProperties(resProps, mapResUrl);
			Properties selectedProps;
			if (mapFile.isFile()) {
				Utilities.loadProperties(fileProps, mapFile);
				int fileRev = getMapSourcesRev(fileProps);
				int resRev = getMapSourcesRev(resProps);
				log.trace("mapsources.properties revisons (resource/file): " + resRev + " / "
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
			log.error("Error while reading mapsources.properties: ", e);
		}
	}

	public static int getMapSourcesRev(Properties p) {
		String revS = p.getProperty(MAPSOURCES_REV_KEY);
		if (revS == null)
			return -1;
		return parseMapSourcesRev(revS);
	}

	private static int parseMapSourcesRev(String s) {
		if (s == null)
			return -1;
		Matcher m = SVN_REV.matcher(s);
		if (!m.matches())
			return -1;
		s = m.group(1);
		return Integer.parseInt(s);
	}

	public static Date getMapSourcesDate(Properties p) {
		String revS = p.getProperty(MAPSOURCES_DATE_KEY);
		if (revS == null)
			return null;
		return parseMapSourcesDate(revS);
	}

	private static Date parseMapSourcesDate(String s) {
		if (s == null)
			return null;
		Matcher m = SVN_DATE.matcher(s);
		if (!m.matches())
			return null;
		String part = m.group(1);
		try {
			return SVN_DATE_FORMAT.parse(part);
		} catch (ParseException e) {
			log.error("", e);
			return null;
		}
	}

	public static void regularMapsourcesOnlineUpdate(boolean async) {
		Date lastUpdate = Settings.getInstance().mapSourcesUpdate.lastUpdate;
		if (lastUpdate == null)
			lastUpdate = getMapSourcesDate(System.getProperties());
		Date end = new Date();
		long diff = end.getTime() - lastUpdate.getTime();
		diff /= 1000 * 60 * 60 * 24;
		if (diff < 7) // online update every week
			return;
		Runnable r = new Runnable() {

			public void run() {
				try {
					log.info("Performing a map sources update");
					boolean result = mapsourcesOnlineUpdate();
					if (result)
						log.info("Updated map sources file retrieved");
					else
						log.info("No new update available");
				} catch (MapSourcesUpdateException e) {
					log.error("Scheduled map sources update failed:", e);
				}
			}
		};
		if (async)
			new Thread(r, "MapSourcesUpdate").start();
		else
			r.run();
	}

	/**
	 * 
	 * @return <ul>
	 *         <li>0: mapsources.properties is up-to-date (no update available)</li>
	 *         </ul>
	 */
	public static boolean mapsourcesOnlineUpdate() throws MapSourcesUpdateException {
		URL url;
		try {
			File mapFile = new File(Settings.getUserDir(), "mapsources.properties");
			url = new URL(MAPSOURCES_UPDATE_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			Settings s = Settings.getInstance();
			if (mapFile.isFile() && s.mapSourcesUpdate.etag != null
					&& s.mapSourcesUpdate.etag != "")
				conn.addRequestProperty("If-None-Match", s.mapSourcesUpdate.etag);
			int code = conn.getResponseCode();
			log.trace("Mapsources online update: \n\tUpdate url: " + MAPSOURCES_UPDATE_URL
					+ "\n\tResponse  : " + code + " " + conn.getResponseMessage()
					+ "\n\tSize      : " + conn.getContentLength() + " bytes \n\tETag      : "
					+ conn.getHeaderField("ETag"));
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
				// of TrekBuddy Atlas Creator
				throw new MapSourcesUpdateException(
						"This version of TrekBuddy Atlas Creator is no longer supported. \n"
								+ "Please update to the current version.");
			byte[] data = new byte[conn.getContentLength()];
			in.readFully(data);
			in.close();
			conn.disconnect(); // We don't need a connection to this server in
			// near future
			Properties onlineProps = new Properties();
			onlineProps.load(new ByteArrayInputStream(data));
			int onlineRev = getMapSourcesRev(onlineProps);
			int currentRev = parseMapSourcesRev(System.getProperty(MAPSOURCES_REV_KEY));
			s.mapSourcesUpdate.lastUpdate = new Date();
			s.mapSourcesUpdate.etag = conn.getHeaderField("ETag");
			if (onlineRev > currentRev || !mapSourcesExternalFileUsed) {
				System.getProperties().putAll(onlineProps);
				FileOutputStream mapFs = null;
				try {
					mapFs = new FileOutputStream(mapFile);
					mapFs.write(data);
				} finally {
					Utilities.closeStream(mapFs);
				}
				for (MapSource ms : getAllMapSources()) {
					if (ms instanceof UpdatableMapSource) {
						((UpdatableMapSource) ms).update();
					}
				}
				mapSourcesExternalFileUsed = true;
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new MapSourcesUpdateException(e);
		}
	}

}
