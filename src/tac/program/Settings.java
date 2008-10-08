package tac.program;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import tac.utilities.Utilities;

public class Settings {

	private static Settings instance;

	private Properties p;

	private Settings() {
		p = new Properties();
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

	public void load() throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(new File(System.getProperty("user.dir"), "settings.xml"));
			p.loadFromXML(is);
		} catch (FileNotFoundException e) {
			this.createDefaultSettingsFile();
			this.load();
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} finally {
			Utilities.closeStream(is);
		}
	}

	public void store() throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(System.getProperty("user.dir"), "settings.xml"));
			p.storeToXML(os, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			Utilities.closeStream(os);
		}
	}

	public void setProperty(String key, String value) {
		p.setProperty(key, value);
	}

	public String getProperty(String key) {
		return p.getProperty(key);
	}

	public void setTileStoreEnabled(boolean enabled) {
		p.setProperty("tile.store.enabled", String.valueOf(enabled));
	}

	public boolean isTileStoreEnabled() {
		if (p.getProperty("tile.store.enabled").equals("true"))
			return true;
		else
			return false;
	}

	public void setMapSize(int i) {
		p.setProperty("maps.size", Integer.toString(i));
	}

	public int getMapSize() {
		return Integer.parseInt(p.getProperty("maps.size"));
	}

	public void createDefaultSettingsFile() throws IOException {
		p.setProperty("tile.store.enabled", "true");
		p.setProperty("maps.size", "2048");
		this.store();
	}
}