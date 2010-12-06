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
package mobac.mapsources.mappacks.region_europe_east;

import mobac.mapsources.AbstractMultiLayerMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageType;

public class CykloatlasWithRelief extends AbstractMultiLayerMapSource {

	public CykloatlasWithRelief() {
		super("CykloatlasWithRelief", TileImageType.PNG);
		mapSources = new MapSource[] { new Cykloatlas(), new CykloatlasRelief() };
		initializeValues();
	}

	@Override
	public String toString() {
		return "Cykloatlas with relief (CZ, SK)";
	}
}
