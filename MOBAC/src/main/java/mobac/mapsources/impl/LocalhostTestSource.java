package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;

public class LocalhostTestSource extends AbstractMapSource {

	private boolean allowStore;
	
	public LocalhostTestSource(String name, boolean allowStore) {
		super(name, 0, 22, "png");
		this.allowStore = allowStore;
	}
	
	public String getTileUrl(int zoom, int tilex, int tiley) {
		return "http://127.0.0.1/tile?x=" + tilex + "&y=" + tiley + "&z=" + zoom;
	}

	@Override
	public boolean allowFileStore() {
		return allowStore;
	}

	
}
