package mobac.gui.components;

import mobac.data.gpx.gpx11.RteType;
import mobac.gui.mapview.GpxLayer;

public class RteEntry extends GpxEntry {
	private RteType rte;
	
	public RteEntry(RteType rte, GpxLayer layer) {
		this.rte = rte;
		this.setLayer(layer);
		this.setWaypointParent(true);
	}	
		
	public String toString() {
		String name = "";
		try {
			name = rte.getName();
		} catch (NullPointerException e) {
			// no name set
		}
		if (name != null && !name.equals("")) {
			return name;
		} else {
			return "unnamed route";
		}
	}		
}