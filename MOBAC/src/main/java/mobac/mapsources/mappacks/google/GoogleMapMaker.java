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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobac.exceptions.MapSourceInitializationException;
import mobac.mapsources.MapSourceUrlUpdater;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;
import mobac.utilities.Charsets;

/**
 * "Google Map Maker" Source Class http://www.google.com/mapmaker
 */
public class GoogleMapMaker extends GoogleMapSource {

	private static final String INIT_URL = "http://www.google.com/mapmaker";
	private static final String INIT_REGEX = "\\\"gwm.([\\d]+)\\\"";

	public GoogleMapMaker() {
		super("Google Map Maker", 1, 17, TileImageType.PNG, HttpMapSource.TileUpdate.LastModified);
	}

	@Override
	protected void initernalInitialize() throws MapSourceInitializationException {
		String html;
		try {
			html = MapSourceUrlUpdater.loadHtmlDocument(INIT_URL, Charsets.UTF_8);
			Pattern p = Pattern.compile(INIT_REGEX);
			Matcher m = p.matcher(html);
			if (!m.find())
				throw new MapSourceInitializationException("gwm parameter pattern not found");
			String number = m.group(1);
			serverUrl = "http://gt{$servernum}.google.com/mt/n=404&v=gwm." + number + "&x={$x}&y={$y}&z={$z}";
		} catch (IOException e) {
			throw new MapSourceInitializationException(e);
		}
	}

}
