package tac.program.download;

import java.util.Enumeration;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.mapsources.MultiLayerMapSource;
import tac.program.JobDispatcher.Job;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndexedArchive;

/**
 * {@link DownloadJobEnumerator} for {@link MultiLayerMapSource} maps.
 * 
 * <p>
 * Toggles between two internal {@link DownloadJobEnumerator} - one for the base
 * map layer and one for the overlay.
 * </p>
 */
public class DownloadJobEnumeratorML implements Enumeration<Job> {

	DownloadJobEnumerator[] layerDJE = new DownloadJobEnumerator[1];

	int activeLayer = 0;

	/**
	 * map.getMapSource() must be {@link MultiLayerMapSource} instance!
	 * 
	 * @param map
	 * @param tileArchive
	 * @param listener
	 */
	public DownloadJobEnumeratorML(MapInterface map, TarIndexedArchive tileArchive,
			DownloadJobListener listener) {
		MultiLayerMapSource overlayMapSource = (MultiLayerMapSource) map.getMapSource();
		MapSource baseMapSource = overlayMapSource.getBackgroundMapSource();
		layerDJE[0] = new DownloadJobEnumerator(map, baseMapSource, tileArchive, listener);
		layerDJE[1] = new DownloadJobEnumerator(map, overlayMapSource, tileArchive, listener);
	}

	public boolean hasMoreElements() {
		return layerDJE[activeLayer].hasMoreElements();
	}

	public Job nextElement() {
		Job job = layerDJE[activeLayer].nextElement();
		activeLayer = activeLayer + 1 % 2;
		return job;
	}

}
