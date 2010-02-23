package mobac.gui.components;

import mobac.data.gpx.gpx11.TrkType;

public class TrkEntry {
	private TrkType trk;
	
	public TrkEntry(TrkType trk) {
		this.trk = trk;
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
}