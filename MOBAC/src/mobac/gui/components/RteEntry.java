package mobac.gui.components;

import mobac.data.gpx.gpx11.RteType;

public class RteEntry {
	private RteType rte;
	
	public RteEntry(RteType rte) {
		this.rte = rte;
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