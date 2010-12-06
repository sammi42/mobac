/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.mapsources.mappacks.region_europe_east;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.UpdatableMapSource;
import mobac.mapsources.mapspace.MercatorPower2MapSpaceEllipsoidal;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;

/**
 * Yandex Sat
 */
public class YandexSat extends AbstractHttpMapSource implements UpdatableMapSource {

	private static int SERVER_NUM = 1;

	private String urlPattern;

	public YandexSat() {
		super("YandexSat", 1, 18, TileImageType.JPG, HttpMapSource.TileUpdate.IfModifiedSince);
		update();
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		SERVER_NUM = (SERVER_NUM % 3) + 3;
		String tmp = urlPattern;
		tmp = tmp.replace("{$servernum}", Integer.toString(SERVER_NUM));
		tmp = tmp.replace("{$x}", Integer.toString(tilex));
		tmp = tmp.replace("{$y}", Integer.toString(tiley));
		tmp = tmp.replace("{$z}", Integer.toString(zoom));
		return tmp;
	}

	@Override
	public MapSpace getMapSpace() {
		return MercatorPower2MapSpaceEllipsoidal.INSTANCE_256;
	}

	@Override
	public String toString() {
		return "Yandex Sat (Russia)";
	}

	public void update() {
		urlPattern = MapSourceTools.loadMapUrl(this, "url");
	}

}
