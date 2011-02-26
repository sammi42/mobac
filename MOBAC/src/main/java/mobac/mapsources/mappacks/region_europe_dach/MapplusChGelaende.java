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
package mobac.mapsources.mappacks.region_europe_dach;

import mobac.program.model.TileImageType;

/**
 * http://www.mapplus.ch
 */
public class MapplusChGelaende extends MapplusCh {

	public MapplusChGelaende() {
		name = "MapplusChGelaende";
		URL = "http://mp%d.mapplus.ch/tydcache/relief_dhm25/%d/%d/%d.png";
		tileType = TileImageType.PNG;
		maxZoom = 13;
	}

	@Override
	public String toString() {
		return "Map+ Gelände (Switzerland)";
	}

}
