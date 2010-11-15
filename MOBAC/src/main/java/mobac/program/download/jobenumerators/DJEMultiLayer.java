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

import java.util.ArrayList;

import mobac.program.Logging;
import mobac.program.JobDispatcher.Job;
import mobac.program.interfaces.DownloadJobEnumerator;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MultiLayerMapSource;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndexedArchive;

/**
 * Enumerates / creates the download jobs for a map that uses a {@link MultiLayerMapSource}. The number of layers is not
 * restricted.
 * 
 * Internally for each layer an own {@link DownloadJobEnumerator} is created and used alternating.
 */
public class DJEMultiLayer implements DownloadJobEnumerator {

	protected ArrayList<DownloadJobEnumerator> layerDJE = new ArrayList<DownloadJobEnumerator>();

	int activeLayer = 0;

	/**
	 * map.getMapSource() must be {@link MultiLayerMapSource} instance!
	 * 
	 * @param map
	 * @param tileArchive
	 * @param listener
	 */
	public DJEMultiLayer(MapInterface map, TarIndexedArchive tileArchive, DownloadJobListener listener) {
		MapSource[] mapSources = Utilities.getMultiLayerMapSources(map.getMapSource());
		int layerNum = mapSources.length;

		for (MapSource ms : mapSources) {
			layerDJE.add(DJEFactory.createInstance(map, ms, --layerNum, tileArchive, listener));
		}
	}

	public boolean hasMoreElements() {
		for (DownloadJobEnumerator dje : layerDJE) {
			if (dje.hasMoreElements())
				return true;
		}
		return false;
	}

	public Job nextElement() {
		Job job = layerDJE.get(activeLayer).nextElement();
		if (job == null) {
			layerDJE.remove(activeLayer);
			return nextElement();
		}
		activeLayer = (activeLayer + 1) % layerDJE.size();
		Logging.LOG.debug("Next job: " + job);
		return job;
	}

}
