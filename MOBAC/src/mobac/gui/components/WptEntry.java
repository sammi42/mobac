package mobac.gui.components;

import mobac.data.gpx.gpx11.WptType;

public class WptEntry {
	private WptType wpt;
	
	public WptEntry(WptType wpt) {
		this.wpt = wpt;
	}	
	
	public String toString() {
		String name = "";
		try {
			name = wpt.getName();
		} catch (NullPointerException e) {
			// no name set
		}
		if (name != null && !name.equals("")) {
			return name;
		} else {
			return "unnamed waypoint";
		}
	}		
}