package mobac.mapsources.mappacks.misc_worldwide;

import java.util.Locale;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * http://www.topomapper.com/
 * 
 * @author "leo-kn"
 */
public class Topomapper extends AbstractHttpMapSource {

	private static final String URL = "http://78.46.61.141/cgi-bin/tilecache-2.10/tilecache.py?"
			+ "LAYERS=topomapper_gmerc&SERVICE=WMS&BBOX=%6f,%6f,%6f,%6f";

	public Topomapper() {
		super("Topomapper.com", 0, 13, TileImageType.JPG, HttpMapSource.TileUpdate.None);
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {

		double f = 40075018.6855784862 / Math.pow(2, zoom);

		double x1 = -20037508.3427892431 + tilex * f;
		double x2 = -20037508.3427892431 + (tilex + 1) * f;
		double y1 = 20037508.3427892431 - (tiley + 1) * f;
		double y2 = 20037508.3427892431 - (tiley + 2) * f;

		return String.format(Locale.ENGLISH, URL, x1, y1, x2, y2);
	}
}