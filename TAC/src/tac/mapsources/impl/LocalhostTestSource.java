package tac.mapsources.impl;

import tac.mapsources.AbstractMapSource;

public class LocalhostTestSource extends AbstractMapSource {

	public LocalhostTestSource() {
		super("Localhost test", 0, 22, "png");
	}
	
	public String getTileUrl(int zoom, int tilex, int tiley) {
		return "http://127.0.0.1/tile?x=" + tilex + "&y=" + tiley + "&z=" + zoom;
	}

	@Override
	public boolean allowFileStore() {
		return false;
	}

	
}
