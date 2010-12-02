/**
 * 
 */
package mobac.mapsources.mappacks.openstreetmap;

import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.AbstractOsmTileSource;
import mobac.program.interfaces.HttpMapSource;

public class TilesAtHome extends AbstractOsmTileSource {

	public TilesAtHome() {
		super("TilesAtHome");
		this.maxZoom = 17;
		this.tileUpdate = HttpMapSource.TileUpdate.IfModifiedSince;
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		return OsmMapSources.MAP_OSMA + super.getTileUrl(zoom, tilex, tiley);
	}

	@Override
	public String toString() {
		return "OpenStreetMap Osmarenderer";
	}

}