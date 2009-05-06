package tac.program.model;

import java.beans.XMLEncoder;
import java.io.PrintStream;

import tac.mapsources.MapSources;

public class Profile {

	private String profileName;
	private String atlasName;

	private double latitudeMax;
	private double latitudeMin;
	private double longitudeMax;
	private double longitudeMin;

	private String mapSource;

	private int tileSizeWidth;
	private int tileSizeHeight;

	private boolean[] zoomLevels;

	// Default constructor
	public Profile() {

		profileName = "";
		atlasName = "";
		latitudeMax = 0.0;
		latitudeMin = 0.0;
		longitudeMax = 0.0;
		longitudeMin = 0.0;
		tileSizeWidth = 0;
		tileSizeHeight = 0;
		mapSource = MapSources.getDefaultMapSourceName();

		zoomLevels = new boolean[10];
		for (int i = 0; i < zoomLevels.length; i++)
			zoomLevels[i] = false;
	}

	// Constructor
	public Profile(String theProfileName, String theAtlasName, String mapSource,
			double theLatitudeMax, double theLatitudeMin, double theLongitudeMax,
			double theLongitudeMin, boolean[] theZoomLevels, int theTileSizeWidth,
			int theTileSizeHeight, int theCustomTileSizeWidth, int theCustomTileSizeHeight) {

		profileName = theProfileName;
		atlasName = theAtlasName;
		this.mapSource = mapSource;
		latitudeMax = theLatitudeMax;
		latitudeMin = theLatitudeMin;
		longitudeMax = theLongitudeMax;
		longitudeMin = theLongitudeMin;
		zoomLevels = theZoomLevels;
		tileSizeWidth = theTileSizeWidth;
		tileSizeHeight = theTileSizeHeight;
	}

	// SET Methods
	public void setProfileName(String theProfileName) {
		profileName = theProfileName;
	}

	public void setAtlasName(String theAtlasName) {
		atlasName = theAtlasName;
	}

	public void setLatitudeMax(Double theLatitudeMax) {
		latitudeMax = theLatitudeMax;
	}

	public void setLatitudeMin(Double theLatitudeMin) {
		latitudeMin = theLatitudeMin;
	}

	public void setLongitudeMax(Double theLongitudeMax) {
		longitudeMax = theLongitudeMax;
	}

	public void setLongitudeMin(Double theLongitudeMin) {
		longitudeMin = theLongitudeMin;
	}

	public void setZoomLevels(boolean[] theZoomLevels) {
		zoomLevels = theZoomLevels;
	}

	public void setTileSizeWidth(int tileSizeWidth) {
		this.tileSizeWidth = tileSizeWidth;
	}

	public void setTileSizeHeight(int tileSizeHeight) {
		this.tileSizeHeight = tileSizeHeight;
	}

	// GET Methods
	public String getProfileName() {
		return profileName;
	}

	public String getAtlasName() {
		return atlasName;
	}

	public Double getLatitudeMax() {
		return latitudeMax;
	}

	public Double getLatitudeMin() {
		return latitudeMin;
	}

	public Double getLongitudeMax() {
		return longitudeMax;
	}

	public Double getLongitudeMin() {
		return longitudeMin;
	}

	public boolean[] getZoomLevels() {
		return zoomLevels;
	}

	public int getTileSizeWidth() {
		return tileSizeWidth;
	}

	public int getTileSizeHeight() {
		return tileSizeHeight;
	}

	public String getMapSource() {
		return mapSource;
	}

	public void setMapSource(String mapSource) {
		this.mapSource = mapSource;
	}

	/**
	 * Test for a possible better implementation for saving Profiles
	 * @param args
	 */
	public static void main(String[] args) {
		PrintStream ps = new PrintStream(System.out);
		XMLEncoder out = new XMLEncoder(ps);
		out.writeObject(new Profile("test", "a", "Google", 1.0, 1.0, 2.0, 2.0, new boolean[] {
				true, true, false }, 10, 20, 30, 40));
		out.flush();
	}
}