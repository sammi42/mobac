package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;

public class LocalhostTestSource extends AbstractMapSource {

	private final boolean allowStore;

	private String baseUrl;

	public LocalhostTestSource(String name, String tileType, boolean allowStore) {
		this(name, 80, tileType, allowStore);
	}

	public LocalhostTestSource(String name, int port, String tileType, boolean allowStore) {
		super(name, 0, 22, tileType);
		this.allowStore = allowStore;
		baseUrl = "http://127.0.0.1:" + port + "/tile." + tileType + "?";
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		return baseUrl + "x=" + tilex + "&y=" + tiley + "&z=" + zoom;
	}

	@Override
	public boolean allowFileStore() {
		return allowStore;
	}

}
