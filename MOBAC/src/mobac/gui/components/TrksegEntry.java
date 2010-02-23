package mobac.gui.components;

import mobac.data.gpx.gpx11.TrksegType;

public class TrksegEntry {
	private TrksegType trkseg;
	private String name;
	
	public TrksegEntry(TrksegType trkseg, int segnum) {
		this.trkseg = trkseg;
		this.name = "segment " + Integer.toString(segnum);
	}	
	
	public String toString() {
		return name;
	}		
}