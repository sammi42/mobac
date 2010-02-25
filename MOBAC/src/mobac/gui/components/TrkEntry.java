package mobac.gui.components;

import mobac.data.gpx.gpx11.TrkType;
import mobac.gui.mapview.GpxLayer;

public class TrkEntry extends GpxEntry {
	private TrkType trk;
	
	public TrkEntry(TrkType trk, GpxLayer layer) {
		this.trk = trk;
		this.setLayer(layer);
		this.setWaypointParent(false);
	}	
	
	public String toString() {
		String name = "";
		try {
			name = trk.getName();
		} catch (NullPointerException e) {
			// no name set
		}
		if (name != null && !name.equals("")) {
			return name;
		} else {
			return "unnamed track";
		}
	}

	public TrkType getTrk() {
		return trk;
	}		
}