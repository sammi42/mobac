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
package mobac.program.download.jobenumerators;

import mobac.program.interfaces.DownloadJobEnumerator;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.Map;
import mobac.program.model.MapPolygon;
import mobac.utilities.tar.TarIndexedArchive;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


public class DJEFactory {

	protected static DownloadJobEnumerator createInstance(MapInterface map, MapSource mapSource,
			int layer, TarIndexedArchive tileArchive, DownloadJobListener listener) {
		if (map instanceof Map)
			return new DJERectangle((Map) map, mapSource, layer, tileArchive, listener);
		if (map instanceof MapPolygon)
			return new DJEPolygon((MapPolygon) map, mapSource, layer,
					tileArchive, listener);
		throw new RuntimeException("Unsupported map type" + map.getClass());
	}
}
