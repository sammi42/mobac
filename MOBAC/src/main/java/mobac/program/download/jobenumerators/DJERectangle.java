package mobac.program.download.jobenumerators;

import java.awt.Point;

import mobac.program.JobDispatcher.Job;
import mobac.program.download.DownloadJob;
import mobac.program.interfaces.DownloadJobEnumerator;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.model.Map;
import mobac.utilities.tar.TarIndexedArchive;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


/**
 * Enumerates / creates the download jobs for a regular rectangle single layer
 * map.
 */
public class DJERectangle implements DownloadJobEnumerator {

	final protected DownloadJobListener listener;
	final protected int xMin;
	final protected int xMax;
	final protected int yMax;
	final protected int zoom;
	final protected int layer;
	final protected MapSource mapSource;
	final protected TarIndexedArchive tileArchive;

	protected int x, y;
	protected DownloadJob nextJob;

	/**
	 * This enumerator is the unfolded version for two encapsulated loops:
	 * 
	 * <pre>
	 * for (int y = yMin; y &lt;= yMax; y++) {
	 * 	for (int x = xMin; x &lt;= xMax; x++) {
	 * 		DownloadJob job = new DownloadJob(downloadDestinationDir, tileSource, x, y, zoom,
	 * 				AtlasThread.this);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param map
	 * @param tileArchive
	 * @param listener
	 */
	public DJERectangle(Map map, TarIndexedArchive tileArchive, DownloadJobListener listener) {
		this(map, map.getMapSource(), 0, tileArchive, listener);
	}

	protected DJERectangle(Map map, MapSource mapSource, int layer, TarIndexedArchive tileArchive,
			DownloadJobListener listener) {
		this.listener = listener;
		Point minCoord = map.getMinTileCoordinate();
		Point maxCoord = map.getMaxTileCoordinate();
		int tileSize = map.getMapSource().getMapSpace().getTileSize();
		this.xMin = minCoord.x / tileSize;
		this.xMax = maxCoord.x / tileSize;
		int yMin = minCoord.y / tileSize;
		this.yMax = maxCoord.y / tileSize;
		this.zoom = map.getZoom();
		this.tileArchive = tileArchive;
		this.mapSource = mapSource;
		this.layer = layer;
		y = yMin;
		x = xMin;

		nextJob = new DownloadJob(mapSource, layer, x, y, zoom, tileArchive, listener);
	}

	public boolean hasMoreElements() {
		return (nextJob != null);
	}

	public Job nextElement() {
		Job job = nextJob;
		x++;
		if (x > xMax) {
			y++;
			x = xMin;
			if (y > yMax) {
				nextJob = null;
				return job;
			}
		}
		nextJob = new DownloadJob(mapSource, layer, x, y, zoom, tileArchive, listener);
		return job;
	}
}
