package mobac.mapsources.mappacks.region_europe_east;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * http://map.navitel.su
 * 
 * @version 1.1
 * @author Andrey Raygorodskiy (andrey(dot)raygorodskiy(at)gmail(dot)com)
 * @author r_x
 */
public class Navitel extends AbstractHttpMapSource {

	private static final String BASE_URL = "http://maps.navitel.su/navitms.fcgi?t=%08d,%08d,%02d";

	public Navitel() {
		super("Navitel.su", 3, 17, TileImageType.PNG, HttpMapSource.TileUpdate.None);
	}

	@Override
	public String toString() {
		return "Navitel (Russian)";
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		tiley = (1 << zoom) - tiley - 1;
		return String.format(BASE_URL, tilex, tiley, zoom);
	}
}