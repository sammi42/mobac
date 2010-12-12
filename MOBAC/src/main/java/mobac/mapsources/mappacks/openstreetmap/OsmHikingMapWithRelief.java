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
/**
 * 
 */
package mobac.mapsources.mappacks.openstreetmap;

import java.awt.Color;

import mobac.mapsources.AbstractMultiLayerMapSource;
import mobac.mapsources.mappacks.openstreetmap.OsmHikingLayers.OsmHikingLayerMap;
import mobac.mapsources.mappacks.openstreetmap.OsmHikingLayers.OsmHikingLayerRelief;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageType;

public class OsmHikingMapWithRelief extends AbstractMultiLayerMapSource {

	public OsmHikingMapWithRelief() {
		super("OSM Hiking with Relief", TileImageType.PNG);
		mapSources = new MapSource[] { new OsmHikingLayerMap(), new OsmHikingLayerRelief() };
		initializeValues();
	}

	@Override
	public Color getBackgroundColor() {
		return Color.WHITE;
	}

	@Override
	public String toString() {
		return "OpenStreetMap Hiking +Relief (Ger)";
	}

}