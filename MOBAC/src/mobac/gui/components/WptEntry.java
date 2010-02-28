package mobac.gui.components;

import mobac.data.gpx.gpx11.WptType;
import mobac.gui.mapview.GpxLayer;

public class WptEntry extends GpxEntry {
	private WptType wpt;
	
	public WptEntry(WptType wpt, GpxLayer layer) {
		this.wpt = wpt;
		this.setLayer(layer);
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

	public WptType getWpt() {
		return wpt;
	}		
}