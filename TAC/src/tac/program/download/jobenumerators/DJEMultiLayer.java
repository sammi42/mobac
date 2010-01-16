package tac.program.download.jobenumerators;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.mapsources.MultiLayerMapSource;
import tac.program.JobDispatcher.Job;
import tac.program.interfaces.DownloadJobEnumerator;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.MapInterface;
import tac.utilities.tar.TarIndexedArchive;

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
