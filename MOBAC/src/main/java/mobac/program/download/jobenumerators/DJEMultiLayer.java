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

import mobac.mapsources.MultiLayerMapSource;
import mobac.program.JobDispatcher.Job;
import mobac.program.interfaces.DownloadJobEnumerator;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.tar.TarIndexedArchive;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


/**
 * Enumerates / creates the download jobs for a map that uses a
 * {@link MultiLayerMapSource}. A maximum of two layers are supported.
 * 
 * Internally for each layer an own {@link DownloadJobEnumerator} is created and
 * used alternating.
 */
public class DJEMultiLayer implements DownloadJobEnumerator {

	protected final DownloadJobEnumerator[] layerDJE = new DownloadJobEnumerator[2];

	int activeLayer = 0;

	/**
	 * map.getMapSource() must be {@link MultiLayerMapSource} instance!
	 * 
	 * @param map
	 * @param tileArchive
	 * @param listener
	 */
	public DJEMultiLayer(MapInterface map, TarIndexedArchive tileArchive,
			DownloadJobListener listener) {
		MultiLayerMapSource overlayMapSource = (MultiLayerMapSource) map.getMapSource();
		MapSource baseMapSource = overlayMapSource.getBackgroundMapSource();
		layerDJE[0] = DJEFactory.createInstance(map, baseMapSource, 0, tileArchive, listener);
		layerDJE[1] = DJEFactory.createInstance(map, overlayMapSource, 1, tileArchive, listener);
	}

	public boolean hasMoreElements() {
		return layerDJE[0].hasMoreElements() || layerDJE[1].hasMoreElements();
	}

	public Job nextElement() {
		Job job = layerDJE[activeLayer].nextElement();
		activeLayer = (activeLayer + 1) % 2;
		return job;
	}

}
