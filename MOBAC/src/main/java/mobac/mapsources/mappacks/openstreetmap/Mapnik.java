/**
 * 
 */
package mobac.mapsources.mappacks.openstreetmap;

import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.AbstractOsmTileSource;
import mobac.program.interfaces.HttpMapSource;

public class Mapnik extends AbstractOsmTileSource {

	public Mapnik() {
		super("Mapnik");
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		return OsmMapSources.MAP_MAPNIK + super.getTileUrl(zoom, tilex, tiley);
	}

	public HttpMapSource.TileUpdate getTileUpdate() {
		return HttpMapSource.TileUpdate.IfNoneMatch;
	}

	@Override
	public String toString() {
		return "OpenStreetMap Mapnik";
	}

}