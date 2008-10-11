package tac.program;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.InvalidPropertiesFormatException;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.gui.preview.MapSources;
import tac.utilities.Utilities;

public class Settings {

	private static Settings instance;

	private static final String SETTINGS_FILE = "settings.xml";
	private static final String MAPS_SIZE = "maps.size";
	private static final String TILE_STORE = "tile.store.enabled";
	private static final String PREVIEW_ZOOM = "preview.zoom";
	private static final String PREVIEW_LAT = "preview.lat";
	private static final String PREVIEW_LON = "preview.lon";
	private static final String PREVIEW_MAPSOURCE = "preview.mapsource";

	private int maxMapSize = 0;

	private boolean tileStoreEnabled = true;

	private int previewDefaultZoom = 3;
	private EastNorthCoordinate previewDefaultCoordinate = new EastNorthCoordinate(9,50);

	private TileSource defaultMapSource = MapSources.getMapSources()[0];
	
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
			defaultMapSource = MapSources.getSourceByName(p.getProperty(PREVIEW_MAPSOURCE));
		} catch (FileNotFoundException e) {
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
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
			p.setProperty(PREVIEW_MAPSOURCE, defaultMapSource.getName());
			os = new FileOutputStream(new File(getUserDir(), SETTINGS_FILE));
			p.storeToXML(os, null);
			result = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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

	public TileSource getDefaultMapSource() {
		return defaultMapSource;
	}

	public void setDefaultMapSource(TileSource defaultMapSource) {
		this.defaultMapSource = defaultMapSource;
	}
	
}