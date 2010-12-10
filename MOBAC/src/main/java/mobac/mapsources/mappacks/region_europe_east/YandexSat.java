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

import mobac.exceptions.MapSourceInitializationException;
import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.MapSourceUrlUpdater;
import mobac.mapsources.mapspace.MercatorPower2MapSpaceEllipsoidal;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;
import mobac.utilities.Charsets;

/**
 * Yandex Sat
 */
public class YandexSat extends AbstractHttpMapSource {

	private static final String INIT_REGEX = "Internal.MapData.DataVersions=.*\\{.*sat:\\\"([\\d\\.]+)\\\"";

	private static final String TEMPLATE_URL = "http://sat0{$servernum}.maps.yandex.ru/tiles?l=sat&v={$version}&x={$x}&y={$y}&z={$z}";

	private static int SERVER_NUM = 1;

	private String version = "";

	public YandexSat() {
		super("YandexSat", 1, 18, TileImageType.JPG, HttpMapSource.TileUpdate.IfModifiedSince);
	}

	@Override
	protected void initernalInitialize() throws MapSourceInitializationException {
		version = MapSourceUrlUpdater.loadDocumentAndExtractGroup(YandexMap.INIT_URL, Charsets.UTF_8, INIT_REGEX);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		SERVER_NUM = (SERVER_NUM % 3) + 3;
		String tmp;
		tmp = MapSourceTools.formatMapUrl(TEMPLATE_URL, SERVER_NUM, zoom, tilex, tiley);
		tmp = tmp.replace("{$version}", version);
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

}
