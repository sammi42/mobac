package tac.program;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class TileStore {
	
	private static TileStore sObj;
	private static List<TreeSet<String>> tileStoreDituGoogleCom;
	private static List<TreeSet<String>> tileStoreMapsGoogleCom;
	
	private TileStore() {
	}
	
	public static synchronized TileStore getInstance() {
		if (sObj == null) {
			sObj = new TileStore();
			tileStoreDituGoogleCom = new ArrayList<TreeSet<String>>(17);
			tileStoreMapsGoogleCom = new ArrayList<TreeSet<String>>(17);
			for (int i = 0; i < 17; i++) {
				tileStoreDituGoogleCom.add(new TreeSet<String>());
				tileStoreMapsGoogleCom.add(new TreeSet<String>());
			}
		}
		return sObj;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException(); 
    }
	
	/**
	 * Method to see if a specific tile is already existing in the tilestore
	 * 
	 * @param zoomLevel, in which zoom level the search for the specific tile shall be performed
	 * @param tileName, the name of the tile for which the search will be performed.
	 * @return if the specified tile is found in this TileStore object the true is returned
	 *         otherwise false is returned.
	 */
	public boolean contains (int zoomLevel, String tileName, String tileStore){
		
		if(tileStore.equals("ditu.google.com")) {
			if ((tileStoreDituGoogleCom.get(zoomLevel - 1)).contains(tileName)) 
				return true;
		}
		else {
			if ((tileStoreMapsGoogleCom.get(zoomLevel - 1)).contains(tileName))
				return true;
		}
		return false;
	}
	
	/**
	 * Add a tile to this TileStore object.
	 * 
	 * @param zoomLevel, to which zoom level the tile shall be added
	 * @param tileName, the name of the tile that shall be added
	 */
	public void add (int zoomLevel, String tileName, String tileStore) {
		if (tileStore.equals("ditu.google.com")) {
			tileStoreDituGoogleCom.get(zoomLevel - 1).add(tileName);	
		}
		else {
			tileStoreMapsGoogleCom.get(zoomLevel - 1).add(tileName);
		}
	}
	
	public String get (int zoomLevel, String tileStore) {
		if (tileStore.equals("ditu.google.com")) {
			return tileStoreDituGoogleCom.get(zoomLevel).toString();	
		}
		else {
			return tileStoreMapsGoogleCom.get(zoomLevel).toString();
		}
	}
		
	/**
	 * Remove a specific tile in this TileStore object.
	 * 
	 * @param zoomLevel, from which zoom level the tile shall be removed.
	 * @param tileName, which tile that shall be removed
	 */
	public void remove (int zoomLevel, String tileName, String tileStore) {
		if (tileStore.equals("ditu.google.com")) {
			tileStoreDituGoogleCom.get(zoomLevel - 1).remove(tileName);	
		}
		else {
			tileStoreMapsGoogleCom.get(zoomLevel - 1).remove(tileName);
		}
	}
	
	/**
	 * Remove all tiles in this TileStore object
	 */
	public void removeAll () {
		for (int i = 0; i < 17; i++) {
			(tileStoreDituGoogleCom.get(i)).clear();
			(tileStoreMapsGoogleCom.get(i)).clear();
		}
	}
	
	/**
	 * This method is used to load all current tiles in the persistent tilestore 
	 * to this TileStore object. This method shall only be invoked at application
	 * start
	 */
	public void init() {
		
		String fileSeparator = System.getProperty("file.separator");
		File dituTiles;
		File mapsTiles;
		
		for (int i = 1; i < 18; i++) {
			dituTiles = new File(System.getProperty("user.dir") + fileSeparator + "tilestore" + fileSeparator + "ditu.google.com" + fileSeparator + i);
			mapsTiles = new File(System.getProperty("user.dir") + fileSeparator + "tilestore" + fileSeparator + "maps.google.com" + fileSeparator + i); 
			for (String s : dituTiles.list()) {
				sObj.add(i, s, "ditu.google.com");
			}
			for (String s : mapsTiles.list()) {
				sObj.add(i, s, "maps.google.com");
			}
		}
	}
	
	public void debugDitu() {
		for (int i = 1; i < 18; i++) {
			System.out.println(sObj.get(i - 1, "ditu.google.com"));
		}
	}
	
	public void debugMaps() {
		for (int i = 1; i < 18; i++) {
			System.out.println(sObj.get(i - 1, "maps.google.com"));
		}
	}
}