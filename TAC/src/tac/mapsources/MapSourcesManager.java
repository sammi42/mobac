package tac.mapsources;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
import tac.mapsources.impl.MiscMapSources.CycleMap;
import tac.mapsources.impl.MiscMapSources.Mapnik;
import tac.mapsources.impl.MiscMapSources.OsmHikingMap;
import tac.mapsources.impl.MiscMapSources.TilesAtHome;
import tac.mapsources.impl.RegionalMapSources.Cykloatlas;
import tac.mapsources.impl.RegionalMapSources.DoCeluPL;
import tac.mapsources.impl.RegionalMapSources.OutdooractiveCom;
import tac.mapsources.impl.RegionalMapSources.UmpWawPl;
import tac.mapsources.impl.WmsSources.TerraserverUSA;
import tac.program.Logging;
import tac.program.model.Settings;
import tac.utilities.Utilities;

public class MapSourcesManager {

	private static final Logger log = Logger.getLogger(MapSourcesManager.class);

	private static final String MAPSOURCES_UPDATE_URL = "http://trekbuddyatlasc.sourceforge.net/"
			+ "mapsources-update/v1/mapsources.properties";

	public static final MapSource DEFAULT = new Mapnik();
	private static MapSource[] MAP_SOURCES;

	static {
		loadMapSourceProperties();
		MAP_SOURCES = new MapSource[] { //
				//
				new GoogleMaps(), new GoogleMapMaker(), new GoogleMapsChina(), new GoogleEarth(),
				new GoogleTerrain(), new MiscMapSources.YahooMaps(), DEFAULT, new TilesAtHome(),
				new CycleMap(), new OsmHikingMap(), /* new OpenArialMap(), */new MicrosoftMaps(),
				new MicrosoftMapsChina(), new MicrosoftVirtualEarth(), new MicrosoftHybrid(),
				new OutdooractiveCom(), new MiscMapSources.MultimapCom(), new Cykloatlas(),
				new TerraserverUSA(), new UmpWawPl(), new DoCeluPL(),
		// The following map sources do not work because of an unknown
		// protection - cookie?
		// new TuristikaMapSk()
		// new MapPlus()
		};
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
			mapSources.add(new LocalhostTestSource());
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
		return null;
	}

	/**
	 * Merges the mapsources property into the system property bundle
	 */
	public static void loadMapSourceProperties() {
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
			if (selectedProps == resProps)
				log.debug("Used mapsources.properties: resource");
			else
				log.debug("Used mapsources.properties: file");
			Properties systemProps = System.getProperties();
			systemProps.putAll(selectedProps);

		} catch (Exception e) {
			log.error("Error while reading mapsources.properties: ", e);
		}
	}

	private static int getMapSourcesRev(Properties p) {
		String revS = p.getProperty("mapsources.Rev");
		if (revS == null)
			return -1;
		return parseMapSourcesRev(revS);
	}

	private static int parseMapSourcesRev(String s) {
		if (s == null)
			return -1;
		final Pattern SVN_REV = Pattern.compile("\\$Rev\\:\\s*(\\d*)\\s*\\$");
		Matcher m = SVN_REV.matcher(s);
		if (!m.matches())
			return -1;
		s = m.group(1);
		return Integer.parseInt(s);
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
			if (mapFile.isFile() && s.mapsourcesEtag != null && s.mapsourcesEtag != "")
				conn.addRequestProperty("If-None-Match", s.mapsourcesEtag);
			int code = conn.getResponseCode();
			log.trace("Mapsources online update: \n\tUpdate url: " + MAPSOURCES_UPDATE_URL
					+ "\n\tResponse:   " + code + " " + conn.getResponseMessage());
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
			Properties onlineProps = new Properties();
			onlineProps.load(new ByteArrayInputStream(data));
			int onlineRev = getMapSourcesRev(onlineProps);
			int currentRev = parseMapSourcesRev(System.getProperty("mapsources.Rev"));
			if (onlineRev > currentRev) {
				System.getProperties().putAll(onlineProps);
				FileOutputStream mapFs = null;
				try {
					mapFs = new FileOutputStream(mapFile);
					mapFs.write(data);
				} finally {
					Utilities.closeStream(mapFs);
				}
				s.mapsourcesEtag = conn.getHeaderField("ETag");
				for (MapSource ms : getAllMapSources()) {
					if (ms instanceof UpdatableMapSource) {
						((UpdatableMapSource) ms).update();
					}
				}
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new MapSourcesUpdateException(e);
		}
	}

	public static void main(String[] args) {
		try {
			Logging.configureLogging();
			System.out.println(mapsourcesOnlineUpdate());
		} catch (MapSourcesUpdateException e) {
			e.printStackTrace();
		}
	}
}
