/**
 * 
 */
package mobac.mapsources.mappacks.region_asia;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class YahooMapsTaiwan extends AbstractHttpMapSource {

	public YahooMapsTaiwan() {
		super("Yahoo Maps Taiwan", 1, 18, TileImageType.PNG, HttpMapSource.TileUpdate.None);
	}

	public String getTileUrl(int zoom, int x, int y) {
		int yahooY = (((1 << zoom) - 2) / 2) - y;
		int yahooZoom = 16 - zoom + 2;
		return "http://l.yimg.com/kp/tl?x=" + x + "&y=" + yahooY + "&z=" + yahooZoom;
	}
}