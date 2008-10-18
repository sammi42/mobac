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
	private static final String MAPS_SIZE = "maps.size";
	private static final String TILE_STORE = "tile.store.enabled";
	private static final String PREVIEW_ZOOM = "preview.zoom";
	private static final String PREVIEW_LAT = "preview.lat";
	private static final String PREVIEW_LON = "preview.lon";
	private static final String PREVIEW_MAPSOURCE = "preview.mapsource";
	private static final String ATLAS_NAME = "atlas.name";
	private static final String PROXY_HOST = "proxy.host";
	private static final String PROXY_PORT = "proxy.port";

	private int maxMapSize = 0;

	private boolean tileStoreEnabled = true;

	private int previewDefaultZoom = 3;
	private EastNorthCoordinate previewDefaultCoordinate = new EastNorthCoordinate(9, 50);

	private String defaultMapSource = MapSources.getMapSources()[0].getName();

	private String atlasName = "";

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
			maxMapSize = p.getIntProperty(MAPS_SIZE, maxMapSize);
			tileStoreEnabled = p.getBooleanProperty(TILE_STORE, tileStoreEnabled);
			previewDefaultZoom = p.getIntProperty(PREVIEW_ZOOM, previewDefaultZoom);
			previewDefaultCoordinate.lat = p.getDouble6Property(PREVIEW_LAT,
					previewDefaultCoordinate.lat);
			previewDefaultCoordinate.lon = p.getDouble6Property(PREVIEW_LON,
					previewDefaultCoordinate.lon);
			defaultMapSource = p.getProperty(PREVIEW_MAPSOURCE);
			atlasName = p.getProperty(ATLAS_NAME, atlasName);
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
			p.setIntProperty(MAPS_SIZE, maxMapSize);
			p.setIntProperty(PREVIEW_ZOOM, previewDefaultZoom);
			p.setBooleanProperty(TILE_STORE, tileStoreEnabled);
			p.setDouble6Property(PREVIEW_LAT, previewDefaultCoordinate.lat);
			p.setDouble6Property(PREVIEW_LON, previewDefaultCoordinate.lon);
			p.setProperty(PREVIEW_MAPSOURCE, defaultMapSource);
			p.setProperty(ATLAS_NAME, atlasName);
			p.setStringProperty(PROXY_HOST, System.getProperty("http.proxyHost"));
			p.setStringProperty(PROXY_PORT, System.getProperty("http.proxyPort"));
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

}