package mobac.gui.components;

import java.io.File;

import mobac.gui.mapview.GpxLayer;

public class GpxEntry {
	private GpxLayer layer;
	private File file;
	
	public GpxEntry(File f, GpxLayer layer) {
		this.file = f;
		this.setLayer(layer);
	}
	
	public String toString() {
		String name = "";
		try {
			name = getLayer().getGpx().getMetadata().getName();
		} catch (NullPointerException e) {
			// no name set
		}
		if (name != null && !name.equals("")) {
			return name;
		} else {
			if (file == null) {
				return "unnamed (new gpx)";
			} else {
				return "unnamed (file " + file.getName() + ")";
			}
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File f) {
		file = f;
	}
	
	public void setLayer(GpxLayer layer) {
		this.layer = layer;
	}

	public GpxLayer getLayer() {
		return layer;
	}
}


