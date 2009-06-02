package tac.mapsources;

/**
 * Custom tile store provider, configurable via constructor.
 */
public class CustomMapSource extends AbstractMapSource {

	TileUpdate tileUpdate;
	String url;

	public CustomMapSource(String name, String url, int minZoom, int maxZoom, String tileType,
			TileUpdate tileUpdate) {
		super(name, minZoom, maxZoom, tileType);
		this.url = url;
		this.tileUpdate = tileUpdate;
	}

	public TileUpdate getTileUpdate() {
		return tileUpdate;
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		String tmp = url;
		tmp = tmp.replace("{$x}", Integer.toString(tilex));
		tmp = tmp.replace("{$y}", Integer.toString(tiley));
		tmp = tmp.replace("{$z}", Integer.toString(zoom));
		return tmp;
	}

	public boolean allowFileStore() {
		return true;
	}
}
