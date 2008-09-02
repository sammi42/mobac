package moller.tac;

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
		String fileSeparator = System.getProperty("file.separator");
		InputStream is;
		
		try {
			is = new FileInputStream(new File(System.getProperty("user.dir") + fileSeparator + "settings.xml"));
			p.loadFromXML(is);
			is.close();
		} catch (FileNotFoundException e) {
			this.createDefaultSettingsFile();
			this.load();
		}
		catch (InvalidPropertiesFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void store() throws IOException{
		
		String fileSeparator = System.getProperty("file.separator");
		OutputStream os;
		
		try {
			os = new FileOutputStream(new File(System.getProperty("user.dir") + fileSeparator + "settings.xml"));
			p.storeToXML(os, null);
			os.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
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
	public boolean getTileStoreEnabled() {
		if (p.getProperty("tile.store.enabled").equals("true"))
			return true;
		else
			return false;
	}
	public void setSelectedGoogleDownloadSite(String s) {
		p.setProperty("google.download.site", s);
	}
	public String getSelectedGoogleDownloadSite() {
		return p.getProperty("google.download.site");
	}
	
	public void setDituGoogleCom(String s) {
		p.setProperty("ditu.google.com", s);
	}
	public String getDituGoogleCom(){
		return p.getProperty("ditu.google.com");
	}
	
	public void setMapsGoogleCom(String s) {
		p.setProperty("maps.google.com", s);
	}
	public String getMapsGoogleCom(){
		return p.getProperty("maps.google.com");
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
		p.setProperty("ditu.google.com", "http://mt1.google.cn/googlechina/maptile?v");
		p.setProperty("maps.google.com", "http://mt0.google.com/mt?n");
		p.setProperty("maps.size", "2048");
		this.store();
	}
}