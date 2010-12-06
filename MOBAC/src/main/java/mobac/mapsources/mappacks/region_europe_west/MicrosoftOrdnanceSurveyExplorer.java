/**
 * 
 */
package mobac.mapsources.mappacks.region_europe_west;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class MicrosoftOrdnanceSurveyExplorer extends AbstractHttpMapSource {

	public MicrosoftOrdnanceSurveyExplorer() {
		super("Ordnance Survey Explorer Maps (UK)", 12, 16, TileImageType.PNG, HttpMapSource.TileUpdate.IfModifiedSince);
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
		String urlAppend = "?g=41&productSet=mmOS";
		return "http://ecn.t2.tiles.virtualearth.net/tiles/r" + tileNum + "." + tileType + urlAppend;
	}

}