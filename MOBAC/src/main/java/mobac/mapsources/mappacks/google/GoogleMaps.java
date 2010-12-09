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

import javax.swing.JOptionPane;

import mobac.exceptions.MapSourceInitializationException;
import mobac.mapsources.MapSourceUrlUpdater;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class GoogleMaps extends GoogleMapSource {

	private static final String INIT_URL = "http://maps.google.com/?ie=UTF8&ll=0,0&spn=0,0&z=2";
	private static final String INIT_REGEX = "^http://mt\\d\\.google\\.com/.*";

	public GoogleMaps() {
		super("Google Maps", 0, 19, TileImageType.PNG, HttpMapSource.TileUpdate.None);
	}

	@Override
	public void update() {
	}

	@Override
	protected void initernalInitialize() throws MapSourceInitializationException {
		List<String> imgUrls;
		try {
			imgUrls = MapSourceUrlUpdater.extractImgSrcList(INIT_URL, INIT_REGEX);
		} catch (IOException e) {
			throw new MapSourceInitializationException(e);
		}
		if (imgUrls.size() == 0)
			throw new MapSourceInitializationException(
					"No suitable sample urls found for generating a new template url");
		String s = imgUrls.get(0);
		s = s.replaceAll("http://mt.\\.google", "http://mt{$servernum}.google");
		JOptionPane.showMessageDialog(null,s);
	}

}
