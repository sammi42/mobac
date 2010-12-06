package mobac.mapsources.impl;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * Map from Multimap.com - incomplete for high zoom levels Uses Quad-Tree coordinate notation
 */
public class MultimapCom extends AbstractHttpMapSource {

	public MultimapCom() {
		// zoom level supported:
		// 0 (fixed url) world.png
		// 1-5 "mergend binary encoding"
		// 6-? uses MS MAP tiles at some parts of the world
		super("Multimap.com", 1, 17, TileImageType.PNG, HttpMapSource.TileUpdate.IfModifiedSince);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {

		String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
		if (tileNum.length() > 12)
			tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6, 12) + "/" + tileNum.substring(12);
		else if (tileNum.length() > 6)
			tileNum = tileNum.substring(0, 6) + "/" + tileNum.substring(6);

		String base;
		if (zoom < 6)
			base = "http://mc1.tiles-cdn.multimap.com/ptiles/map/mi915/";
		else if (zoom < 14)
			base = "http://mc2.tiles-cdn.multimap.com/ptiles/map/mi917/";
		else
			base = "http://mc3.tiles-cdn.multimap.com/ptiles/map/mi931/";
		tileNum = base + (zoom + 1) + "/" + tileNum + ".png?client=public_api&service_seq=14458";
		return tileNum;
	}
}