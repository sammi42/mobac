/**
 * 
 */
package mobac.mapsources.mappacks.misc_worldwide;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class YahooMaps extends AbstractHttpMapSource {

	public YahooMaps() {
		super("Yahoo Maps", 1, 16, TileImageType.JPG, HttpMapSource.TileUpdate.IfModifiedSince);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		int yahooTileY = (((1 << zoom) - 2) / 2) - tiley;
		int yahooZoom = getMaxZoom() - zoom + 2;
		return "http://maps.yimg.com/hw/tile?locale=en&imgtype=png&yimgv=1.2&v=4.1&x=" + tilex + "&y=" + yahooTileY
				+ "+6163&z=" + yahooZoom;
	}

}