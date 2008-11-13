package tac.program;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.InvalidPropertiesFormatException;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import tac.gui.preview.MapSources;
import tac.utilities.Utilities;

public class Settings {

	private static Logger log = Logger.getLogger(Settings.class);

	private static Settings instance;

	private static final String SETTINGS_FILE = "settings.xml";
	private static final String MAPS_MAXSIZE = "maps.maxsize";
	private static final String TILE_STORE = "tile.store.enabled";
	private static final String PREVIEW_ZOOM = "preview.zoom";
	private static final String PREVIEW_LAT = "preview.lat";
	private static final String PREVIEW_LON = "preview.lon";
	private static final String MAPSOURCE = "mapsource";
	private static final String ATLAS_NAME = "atlas.name";
	private static final String PROXY_HOST = "proxy.http.host";
	private static final String PROXY_PORT = "proxy.http.port";
	private static final String SELECTION_LAT_MAX = "selection.max.lat";
	private static final String SELECTION_LON_MAX = "selection.max.lon";
	private static final String SELECTION_LAT_MIN = "selection.min.lat";
	private static final String SELECTION_LON_MIN = "selection.min.lon";
	private static final String THREAD_COUNT = "download.thread.count";
	private static final String CONNECTION_TIMEOUT = "download.timeout";
	private static final String GOOGLE_MAPS_URL = "google.maps.url";
	private static final String GOOGLE_EARTH_URL = "google.earth.url";

	private int maxMapSize = 32768;

	private boolean tileStoreEnabled = true;

	private int previewDefaultZoom = 3;
	private EastNorthCoordinate previewDefaultCoordinate = new EastNorthCoordinate(50, 9);

	private EastNorthCoordinate selectionMax = new EastNorthCoordinate();
	private EastNorthCoordinate selectionMin = new EastNorthCoordinate();

	private String defaultMapSource = MapSources.getDefaultMapSourceName();

	private String atlasName = "";

	private String userAgent = UserAgent.FF2_XP;

	private int threadCount = 4;

	// Timeout in seconds (default 10 seconds)
	private int connectionTimeout = 10;
	
	private String googleMapsUrl = "";
	private String googleEarthUrl = "";

	private Settings() {
	}

	public static Settings getInstance() {
		if (instance != null)
			return instance;
		synchronized (Settings.class) {
			if (instance == null) {
				instance = new Settings();
			}
			return instance;
		}
	}

	public static String getUserDir() {
		return System.getProperty("user.dir");
	}

	public void loadOrQuit() {
		try {
			load();
		} catch (IOException e) {
			log.error(e);
			JOptionPane.showMessageDialog(null,
					"Could not create file settings.xml program will exit.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public void load() throws IOException {
		InputStream is = null;
		try {
			SettingsProperties p = new SettingsProperties();
			is = new FileInputStream(new File(getUserDir(), SETTINGS_FILE));
			p.loadFromXML(is);
			threadCount = p.getIntProperty(THREAD_COUNT, threadCount);
			connectionTimeout = p.getIntProperty(CONNECTION_TIMEOUT, connectionTimeout);
			maxMapSize = p.getIntProperty(MAPS_MAXSIZE, maxMapSize);
			tileStoreEnabled = p.getBooleanProperty(TILE_STORE, tileStoreEnabled);
			previewDefaultZoom = p.getIntProperty(PREVIEW_ZOOM, previewDefaultZoom);
			previewDefaultCoordinate.lat = p.getDouble6Property(PREVIEW_LAT,
					previewDefaultCoordinate.lat);
			previewDefaultCoordinate.lon = p.getDouble6Property(PREVIEW_LON,
					previewDefaultCoordinate.lon);
			selectionMax.lat = p.getDouble6Property(SELECTION_LAT_MAX, selectionMax.lat);
			selectionMax.lon = p.getDouble6Property(SELECTION_LON_MAX, selectionMax.lon);
			selectionMin.lat = p.getDouble6Property(SELECTION_LAT_MIN, selectionMin.lat);
			selectionMin.lon = p.getDouble6Property(SELECTION_LON_MIN, selectionMin.lon);
			defaultMapSource = p.getProperty(MAPSOURCE);
			atlasName = p.getProperty(ATLAS_NAME, atlasName);
			googleEarthUrl = p.getProperty(GOOGLE_EARTH_URL, googleEarthUrl);
			googleMapsUrl = p.getProperty(GOOGLE_MAPS_URL, googleMapsUrl);
			String proxyHost = p.getProperty(PROXY_HOST);
			String proxyPort = p.getProperty(PROXY_PORT);
			if (proxyHost != null)
				System.setProperty("http.proxyHost", proxyHost);
			if (proxyPort != null)
				System.setProperty("http.proxyPort", proxyPort);

		} catch (FileNotFoundException e) {
		} catch (InvalidPropertiesFormatException e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(is);
		}
	}

	public boolean store() throws IOException {
		boolean result = false;
		OutputStream os = null;
		try {
			SettingsProperties p = new SettingsProperties();
			p.setIntProperty(THREAD_COUNT, threadCount);
			p.setIntProperty(CONNECTION_TIMEOUT, connectionTimeout);
			p.setIntProperty(MAPS_MAXSIZE, maxMapSize);
			p.setIntProperty(PREVIEW_ZOOM, previewDefaultZoom);
			p.setBooleanProperty(TILE_STORE, tileStoreEnabled);
			p.setDouble6Property(PREVIEW_LAT, previewDefaultCoordinate.lat);
			p.setDouble6Property(PREVIEW_LON, previewDefaultCoordinate.lon);
			p.setProperty(MAPSOURCE, defaultMapSource);
			p.setProperty(ATLAS_NAME, atlasName);
			p.setStringProperty(PROXY_HOST, System.getProperty("http.proxyHost"));
			p.setStringProperty(PROXY_PORT, System.getProperty("http.proxyPort"));

			p.setStringProperty(GOOGLE_MAPS_URL, googleMapsUrl);
			p.setStringProperty(GOOGLE_EARTH_URL, googleEarthUrl);

			p.setDouble6Property(SELECTION_LAT_MAX, selectionMax.lat);
			p.setDouble6Property(SELECTION_LON_MAX, selectionMax.lon);
			p.setDouble6Property(SELECTION_LAT_MIN, selectionMin.lat);
			p.setDouble6Property(SELECTION_LON_MIN, selectionMin.lon);

			os = new FileOutputStream(new File(getUserDir(), SETTINGS_FILE));
			p.storeToXML(os, null);
			result = true;
		} catch (Exception e) {
			log.error("Error while saving settings!", e);
		} finally {
			Utilities.closeStream(os);
		}
		return result;
	}

	public int getMaxMapsSize() {
		return maxMapSize;
	}

	public void setMaxMapSize(int mapsSize) {
		this.maxMapSize = mapsSize;
	}

	public boolean isTileStoreEnabled() {
		return tileStoreEnabled;
	}

	public void setTileStoreEnabled(boolean tileStoreEnabled) {
		this.tileStoreEnabled = tileStoreEnabled;
	}

	public int getPreviewDefaultZoom() {
		return previewDefaultZoom;
	}

	public void setPreviewDefaultZoom(int previewDefaultZoom) {
		this.previewDefaultZoom = previewDefaultZoom;
	}

	public EastNorthCoordinate getPreviewDefaultCoordinate() {
		return previewDefaultCoordinate;
	}

	public void setPreviewDefaultCoordinate(EastNorthCoordinate previewDefaultCoordinate) {
		this.previewDefaultCoordinate = previewDefaultCoordinate;
	}

	public String getDefaultMapSource() {
		return defaultMapSource;
	}

	public void setDefaultMapSource(String defaultMapSource) {
		this.defaultMapSource = defaultMapSource;
	}

	public String getAtlasName() {
		return atlasName;
	}

	public void setAtlasName(String atlasName) {
		this.atlasName = atlasName;
	}

	public EastNorthCoordinate getSelectionMax() {
		return selectionMax;
	}

	public void setSelectionMax(EastNorthCoordinate selectionMax) {
		this.selectionMax = selectionMax;
	}

	public EastNorthCoordinate getSelectionMin() {
		return selectionMin;
	}

	public void setSelectionMin(EastNorthCoordinate selectionMin) {
		this.selectionMin = selectionMin;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getGoogleMapsUrl() {
		return googleMapsUrl;
	}

	public void setGoogleMapsUrl(String googleMapsUrl) {
		this.googleMapsUrl = googleMapsUrl;
	}

	public String getGoogleEarthUrl() {
		return googleEarthUrl;
	}

	public void setGoogleEarthUrl(String googleEarthUrl) {
		this.googleEarthUrl = googleEarthUrl;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

}