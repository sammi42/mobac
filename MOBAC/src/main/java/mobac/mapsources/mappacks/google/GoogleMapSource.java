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
package mobac.mapsources.mappacks.google;

import java.io.IOException;
import java.util.List;

import mobac.exceptions.MapSourceInitializationException;
import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceUrlUpdater;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class GoogleMapSource extends AbstractHttpMapSource {

	public static String LANG = "en";

	private int serverNum = 0;
	protected int minServerNum = 1;
	protected int maxServerNum = 4;

	public String serverUrl;

	public GoogleMapSource(String name, int minZoom, int maxZoom, TileImageType tileType,
			HttpMapSource.TileUpdate tileUpdate) {
		super(name, minZoom, maxZoom, tileType, tileUpdate);
	}

	protected int getNextServerNum() {
		int x = serverNum;
		serverNum = (serverNum + minServerNum) % maxServerNum;
		return x;
	}

	public String getTileUrl(int zoom, int x, int y) {
		String tmp = serverUrl;
		tmp = tmp.replace("{$servernum}", Integer.toString(getNextServerNum()));
		tmp = tmp.replace("{$lang}", LANG);
		tmp = tmp.replace("{$x}", Integer.toString(x));
		tmp = tmp.replace("{$y}", Integer.toString(y));
		tmp = tmp.replace("{$z}", Integer.toString(zoom));
		return tmp;
	}

	protected void initializeServerUrl(String htmlUrl, String regex) throws MapSourceInitializationException {
		List<String> imgUrls;
		try {
			imgUrls = MapSourceUrlUpdater.extractImgSrcList(htmlUrl, regex);
		} catch (IOException e) {
			throw new MapSourceInitializationException(e);
		}
		if (imgUrls.size() == 0)
			throw new MapSourceInitializationException(
					"No suitable sample urls found for generating a new template url");
		String s = imgUrls.get(0);
		s = s.replaceFirst("http://mt.\\.google\\.", "http://mt{\\$servernum}.google.");
		s = s.replaceFirst("hl=(\\w)+", "hl={\\$hl}");
		s = s.replaceFirst("x=\\d+", "x={\\$x}");
		s = s.replaceFirst("y=\\d+", "y={\\$y}");
		s = s.replaceFirst("z=\\d+", "z={\\$z}");
		serverUrl = s;
	}

}
