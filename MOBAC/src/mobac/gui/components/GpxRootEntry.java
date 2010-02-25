package mobac.gui.components;

import mobac.gui.mapview.GpxLayer;

public class GpxRootEntry extends GpxEntry {
	
	public GpxRootEntry(GpxLayer layer) {
		this.setLayer(layer);
		this.setWaypointParent(true);
	}
	
	public String toString() {
		String name = "";
		try {
			name = getLayer().getGpx().getMetadata().getName();
		} catch (NullPointerException e){
		}
		if (name != null && !name.equals("")) {
			return name;
		} else {
			if (getLayer().getFile() == null) {
				return "unnamed (new gpx)";
			} else {
				return "unnamed (file " + getLayer().getFile().getName() + ")";
			}
		}
	}
}


