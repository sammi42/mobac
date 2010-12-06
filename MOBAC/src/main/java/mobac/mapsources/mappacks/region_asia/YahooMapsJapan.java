/**
 * 
 */
package mobac.mapsources.mappacks.region_asia;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class YahooMapsJapan extends AbstractHttpMapSource {

	public YahooMapsJapan() {
		super("Yahoo Maps Japan", 1, 19, TileImageType.PNG, HttpMapSource.TileUpdate.IfModifiedSince);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		int yahooY = (((1 << zoom) - 2) / 2) - tiley;
		int yahooZoom = zoom + 1;
		return "http://ta.map.yahoo.co.jp/yta/map?v=4.3&r=1&x=" + tilex + "&y=" + yahooY + "&z=" + yahooZoom;
	}

}