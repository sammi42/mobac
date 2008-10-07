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

public class Settings {

	private static Settings s;
	private static Properties p;

	private Settings() {
	}

	public static synchronized Settings getInstance() {
		if (s == null) {
			s = new Settings();
			p = new Properties();
		}
		return s;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void load() throws IOException {
		InputStream is;

		try {
			is = new FileInputStream(
					new File(System.getProperty("user.dir") + "/" + "settings.xml"));
			p.loadFromXML(is);
			is.close();
		} catch (FileNotFoundException e) {
			this.createDefaultSettingsFile();
			this.load();
		} catch (InvalidPropertiesFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void store() throws IOException {
		OutputStream os;

		try {
			os = new FileOutputStream(new File(System.getProperty("user.dir") + "/settings.xml"));
			p.storeToXML(os, null);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
		p.setProperty("google.download.site", "maps.google.com");
		p.setProperty("ditu.google.com", "http://mt0.google.cn/mt?v");
		p.setProperty("maps.google.com", "http://mt0.google.com/mt/v=nq.83&hl=sv");
		p.setProperty("maps.size", "2048");
		this.store();
	}
}