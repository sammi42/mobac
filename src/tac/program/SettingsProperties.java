package tac.program;

import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import tac.utilities.Utilities;

public class SettingsProperties extends Properties {

	private static final long serialVersionUID = 1L;

	public SettingsProperties() {
		super();
	}

	public int getIntProperty(String key, int defaultValue) {
		try {
			String value = getProperty(key);
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public void setIntProperty(String key, int value) {
		setProperty(key, Integer.toString(value));
	}

	public boolean getBooleanProperty(String key, boolean defaultValue) {
		try {
			String value = getProperty(key);
			return Boolean.parseBoolean(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public void setBooleanProperty(String key, boolean value) {
		setProperty(key, Boolean.toString(value));
	}

	public double getDouble6Property(String key, double defaultValue) {
		try {
			String value = getProperty(key);
			return Utilities.FORMAT_6_DEC_ENG.parse(value).doubleValue();
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public void setDouble6Property(String key, double value) {
		setProperty(key, Utilities.FORMAT_6_DEC_ENG.format(value));
	}

	public void setStringProperty(String key, String value) {
		if (value == null)
			return;
		setProperty(key, value);
	}

	@Override
	public Set<Object> keySet() {
		return new TreeSet<Object>(super.keySet());
	}



	
}
