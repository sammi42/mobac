package tac.program;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
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

import tac.mapsources.MapSources;
import tac.mapsources.Google.GoogleSource;
import tac.program.model.AtlasOutputFormat;
import tac.program.model.EastNorthCoordinate;
import tac.program.model.TileImageFormat;
import tac.utilities.Utilities;

public class Settings {

	private static Logger log = Logger.getLogger(Settings.class);

	private static Settings instance;

	private static final String SETTINGS_FILE = "settings.xml";
	private static final String DEVMODE = "devmode";
	private static final String MAPS_MAXSIZE = "maps.maxsize";
	private static final String TILE_STORE = "tilestore.enabled";
	private static final String PREVIEW_ZOOM = "preview.zoom";
	private static final String PREVIEW_LAT = "preview.lat";
	private static final String PREVIEW_LON = "preview.lon";
	private static final String MAPSOURCE = "mapsource";
	private static final String TILE_CUSTOM = "tile.custom";
	private static final String TILE_HEIGHT = "tile.height";
	private static final String TILE_WIDTH = "tile.width";
	private static final String TILE_FORMAT = "tile.format";
	private static final String ATLAS_NAME = "atlas.name";
	private static final String ATLAS_FORMAT = "atlas.format";
	private static final String PROXY_HOST = "proxy.http.host";
	private static final String PROXY_PORT = "proxy.http.port";
	private static final String SELECTION_LAT_MAX = "selection.max.lat";
	private static final String SELECTION_LON_MAX = "selection.max.lon";
	private static final String SELECTION_LAT_MIN = "selection.min.lat";
	private static final String SELECTION_LON_MIN = "selection.min.lon";
	private static final String THREAD_COUNT = "download.thread.count";
	private static final String CONNECTION_TIMEOUT = "download.timeout";
	private static final String GOOGLE_LANGUAGE = "google.maps.lang";
	private static final String WINDOW_WIDTH = "window.width";
	private static final String WINDOW_HEIGHT = "window.height";
	private static final String WINDOW_POS_X = "window.pos.x";
	private static final String WINDOW_POS_Y = "window.pos.y";
	private static final String WINDOW_MAXIMIZED = "window.maximized";
	private static final String FULL_SCREEN_ENABLED = "full-screen-enabled";

	private int maxMapSize = 32767;

	private boolean tileStoreEnabled = true;

	private int previewDefaultZoom = 3;
	private EastNorthCoordinate previewDefaultCoordinate = new EastNorthCoordinate(50, 9);

	private EastNorthCoordinate selectionMax = new EastNorthCoordinate();
	private EastNorthCoordinate selectionMin = new EastNorthCoordinate();

	private String defaultMapSource = MapSources.getDefaultMapSourceName();

	private String atlasName = "Layer name";

	private String userAgent = UserAgent.FF2_XP;

	private int threadCount = 4;

	private boolean customTileSize = false;
	private int tileHeight = 256;
	private int tileWidth = 256;
	private TileImageFormat tileImageFormat = TileImageFormat.PNG;
	private AtlasOutputFormat atlasOutputFormat = AtlasOutputFormat.TaredAtlas;

	// Timeout in seconds (default 10 seconds)
	private int connectionTimeout = 10;

	private String googleLanguage = "en";

	private boolean devMode = false;

	private Dimension windowDimension = new Dimension();
	private Point windowLocation = new Point(-1, -1);
	private Boolean windowMaximized = true;

	private Boolean fullScreenEnabled = false;

	private Settings() {
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		windowDimension.width = (int) (0.9f * dScreen.width);
		windowDimension.height = (int) (0.9f * dScreen.height);
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
			customTileSize = p.getBooleanProperty(TILE_CUSTOM, false);
			tileHeight = p.getIntProperty(TILE_HEIGHT, tileHeight);
			tileWidth = p.getIntProperty(TILE_WIDTH, tileWidth);
			tileImageFormat = TileImageFormat.valueOf(p.getProperty(TILE_FORMAT, tileImageFormat
					.name()));
			maxMapSize = p.getIntProperty(MAPS_MAXSIZE, maxMapSize);
			tileStoreEnabled = p.getBooleanProperty(TILE_STORE, tileStoreEnabled);
			previewDefaultZoom = p.getIntProperty(PREVIEW_ZOOM, previewDefaultZoom);
			previewDefaultCoordinate.lat = p.getDouble6Property(PREVIEW_LAT,
					previewDefaultCoordinate.lat);
			previewDefaultCoordinate.lon = p.getDouble6Property(PREVIEW_LON,
					previewDefaultCoordinate.lon);
			devMode = p.getBooleanProperty(DEVMODE, devMode);
			selectionMax.lat = p.getDouble6Property(SELECTION_LAT_MAX, selectionMax.lat);
			selectionMax.lon = p.getDouble6Property(SELECTION_LON_MAX, selectionMax.lon);
			selectionMin.lat = p.getDouble6Property(SELECTION_LAT_MIN, selectionMin.lat);
			selectionMin.lon = p.getDouble6Property(SELECTION_LON_MIN, selectionMin.lon);
			defaultMapSource = p.getProperty(MAPSOURCE);
			atlasName = p.getProperty(ATLAS_NAME, atlasName);
			atlasOutputFormat = AtlasOutputFormat.valueOf(p.getProperty(ATLAS_FORMAT,
					atlasOutputFormat.name()));
			setGoogleLanguage(p.getProperty(GOOGLE_LANGUAGE, googleLanguage));
			String proxyHost = p.getProperty(PROXY_HOST);
			String proxyPort = p.getProperty(PROXY_PORT);
			if (proxyHost != null)
				System.setProperty("http.proxyHost", proxyHost);
			if (proxyPort != null)
				System.setProperty("http.proxyPort", proxyPort);
			windowDimension.width = p.getIntProperty(WINDOW_WIDTH, windowDimension.width);
			windowDimension.height = p.getIntProperty(WINDOW_HEIGHT, windowDimension.height);
			windowLocation.x = p.getIntProperty(WINDOW_POS_X, windowLocation.x);
			windowLocation.y = p.getIntProperty(WINDOW_POS_Y, windowLocation.y);
			windowMaximized = p.getBooleanProperty(WINDOW_MAXIMIZED, windowMaximized);

			fullScreenEnabled = p.getBooleanProperty(FULL_SCREEN_ENABLED, fullScreenEnabled);

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
			if (devMode)
				p.setBooleanProperty(DEVMODE, devMode);
			p.setIntProperty(THREAD_COUNT, threadCount);
			p.setIntProperty(CONNECTION_TIMEOUT, connectionTimeout);
			p.setIntProperty(MAPS_MAXSIZE, maxMapSize);
			p.setIntProperty(PREVIEW_ZOOM, previewDefaultZoom);
			p.setBooleanProperty(TILE_CUSTOM, customTileSize);
			p.setIntProperty(TILE_HEIGHT, tileHeight);
			p.setIntProperty(TILE_WIDTH, tileWidth);
			p.setStringProperty(TILE_FORMAT, tileImageFormat.name());
			p.setBooleanProperty(TILE_STORE, tileStoreEnabled);
			p.setDouble6Property(PREVIEW_LAT, previewDefaultCoordinate.lat);
			p.setDouble6Property(PREVIEW_LON, previewDefaultCoordinate.lon);
			p.setProperty(MAPSOURCE, defaultMapSource);
			p.setProperty(ATLAS_NAME, atlasName);
			p.setProperty(ATLAS_FORMAT, atlasOutputFormat.name());
			p.setStringProperty(PROXY_HOST, System.getProperty("http.proxyHost"));
			p.setStringProperty(PROXY_PORT, System.getProperty("http.proxyPort"));

			p.setStringProperty(GOOGLE_LANGUAGE, googleLanguage);

			if (devMode)
				p.setBooleanProperty(DEVMODE, devMode);

			p.setDouble6Property(SELECTION_LAT_MAX, selectionMax.lat);
			p.setDouble6Property(SELECTION_LON_MAX, selectionMax.lon);
			p.setDouble6Property(SELECTION_LAT_MIN, selectionMin.lat);
			p.setDouble6Property(SELECTION_LON_MIN, selectionMin.lon);

			p.setIntProperty(WINDOW_WIDTH, windowDimension.width);
			p.setIntProperty(WINDOW_HEIGHT, windowDimension.height);
			p.setIntProperty(WINDOW_POS_X, windowLocation.x);
			p.setIntProperty(WINDOW_POS_Y, windowLocation.y);

			p.setBooleanProperty(WINDOW_MAXIMIZED, windowMaximized);
			p.setBooleanProperty(FULL_SCREEN_ENABLED, fullScreenEnabled);

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

	public int getMaxMapSize() {
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

	public String getGoogleLanguage() {
		return googleLanguage;
	}

	public void setGoogleLanguage(String googleLanguage) {
		this.googleLanguage = googleLanguage;
		GoogleSource.LANG = googleLanguage;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public boolean isCustomTileSize() {
		return customTileSize;
	}

	public void setCustomTileSize(boolean customTileSize) {
		this.customTileSize = customTileSize;
	}

	public int getTileHeight() {
		return tileHeight;
	}

	public void setTileHeight(int tileHeight) {
		this.tileHeight = tileHeight;
	}

	public int getTileWidth() {
		return tileWidth;
	}

	public void setTileWidth(int tileWidth) {
		this.tileWidth = tileWidth;
	}

	public boolean isDevModeEnabled() {
		return devMode;
	}

	public Dimension getWindowDimension() {
		return windowDimension;
	}

	public void setWindowDimension(Dimension windowDimension) {
		this.windowDimension = windowDimension;
	}

	public Point getWindowLocation() {
		return windowLocation;
	}

	public void setWindowLocation(Point windowLocation) {
		this.windowLocation = windowLocation;
	}

	public Boolean getWindowMaximized() {
		return windowMaximized;
	}

	public void setWindowMaximized(Boolean windowMaximized) {
		this.windowMaximized = windowMaximized;
	}

	public Boolean getFullScreenEnabled() {
		//TODO Adapt fullscreen mose
		//return fullScreenEnabled;
		return false;
	}

	public void setFullScreenEnabled(Boolean fullScreenEnabled) {
		this.fullScreenEnabled = fullScreenEnabled;
	}

	public TileImageFormat getTileImageFormat() {
		return tileImageFormat;
	}

	public void setTileImageFormat(TileImageFormat tileImageFormat) {
		this.tileImageFormat = tileImageFormat;
	}

	public AtlasOutputFormat getAtlasOutputFormat() {
		return atlasOutputFormat;
	}

	public void setAtlasOutputFormat(AtlasOutputFormat atlasOutputFormat) {
		this.atlasOutputFormat = atlasOutputFormat;
	}

}