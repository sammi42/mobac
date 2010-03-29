package mobac.mapsources.mapspace;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

public class MapSpaceFactory {

	/**
	 * @param tileSize
	 * @param isSpherical
	 * @return
	 */
	public static MapSpace getInstance(int tileSize, boolean isSpherical) {
		if (isSpherical)
			return new MercatorPower2MapSpace(tileSize);
		else
			return new MercatorPower2MapSpaceEllipsoidal(tileSize);
	}

}
