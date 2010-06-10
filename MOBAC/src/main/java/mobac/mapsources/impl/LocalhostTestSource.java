package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;

public class LocalhostTestSource extends AbstractMapSource {

	private final boolean allowStore;

	private String baseUrl;

	public LocalhostTestSource(String name, boolean allowStore) {
		super(name, 0, 22, "png");
		this.allowStore = allowStore;
		baseUrl = "http://127.0.0.1/tile?";
	}

	public LocalhostTestSource(String name, int port, boolean allowStore) {
		this(name, allowStore);
		baseUrl = "http://127.0.0.1:" + port + "/tile?";
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		return baseUrl + "x=" + tilex + "&y=" + tiley + "&z=" + zoom;
	}

	@Override
	public boolean allowFileStore() {
		return allowStore;
	}

}
