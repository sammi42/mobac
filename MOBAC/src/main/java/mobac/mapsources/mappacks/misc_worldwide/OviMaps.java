/**
 * 
 */
package mobac.mapsources.mappacks.misc_worldwide;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class OviMaps extends AbstractHttpMapSource {

	public OviMaps() {
		super("Ovi Maps", 1, 18, TileImageType.PNG);
		tileUpdate = HttpMapSource.TileUpdate.IfModifiedSince;
	}

	public String getTileUrl(int zoom, int x, int y) {
		return "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day/" + zoom + "/" + x + "/" + y
				+ "/256/png8?token=...&referer=maps.ovi.com";
	}

	@Override
	public String toString() {
		return "Ovi/Nokia Maps";
	}

}