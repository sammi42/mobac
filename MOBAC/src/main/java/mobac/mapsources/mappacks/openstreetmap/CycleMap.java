/**
 * 
 */
package mobac.mapsources.mappacks.openstreetmap;

import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.AbstractOsmTileSource;
import mobac.program.interfaces.HttpMapSource;

public class CycleMap extends AbstractOsmTileSource {

	private static final String PATTERN = "http://%s.andy.sandbox.cloudmade.com/tiles/cycle/%d/%d/%d.png";

	private static final String[] SERVER = { "a", "b", "c" };

	private int SERVER_NUM = 0;

	public CycleMap() {
		super("OSM Cycle Map");
		this.maxZoom = 17;
		this.tileUpdate = HttpMapSource.TileUpdate.ETag;
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		String url = String.format(PATTERN, new Object[] { SERVER[SERVER_NUM], zoom, tilex, tiley });
		SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
		return url;
	}

	@Override
	public String toString() {
		return "OpenStreetMap Cyclemap";
	}

}