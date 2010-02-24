package mobac.gui.components;

import mobac.data.gpx.gpx11.TrksegType;
import mobac.gui.mapview.GpxLayer;

public class TrksegEntry extends GpxEntry {
	private TrksegType trkseg;
	private String name;
	
	public TrksegEntry(TrksegType trkseg, int segnum, GpxLayer layer) {
		this.trkseg = trkseg;
		this.name = "segment " + Integer.toString(segnum);
		this.setLayer(layer);
		this.setWaypointParent(true);
	}	
	
	public String toString() {
		return name;
	}		
}