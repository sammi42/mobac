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
package mobac.mapsources.custom;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.mapsources.MultiLayerMapSource;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

/**
 * Custom tile store provider for multi-layer map sources, configurable via settings.xml.
 */
@XmlRootElement
public class CustomMultiLayerMapSource extends CustomMapSource implements MultiLayerMapSource {

	@XmlElement(required = true, name="backgroundMapSource")
	private CustomMapSource background = null;

	public MapSource getBackgroundMapSource() {
		return background;
	}

}
