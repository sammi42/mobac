package tac.program.download;

import java.awt.Point;
import java.util.Enumeration;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.JobDispatcher.Job;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.MapInterface;
import tac.utilities.tar.TarIndexedArchive;

public class DownloadJobEnumerator implements Enumeration<Job> {

	final private DownloadJobListener listener;
	final private int xMin;
	final private int xMax;
	final private int yMax;
	final private int zoom;
	final private int layer;
	final private MapSource mapSource;
	final private TarIndexedArchive tileArchive;

	private int x, y;
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
	public DownloadJobEnumerator(MapInterface map, TarIndexedArchive tileArchive,
			DownloadJobListener listener) {
		this(map, map.getMapSource(), 0, tileArchive, listener);
	}

	public DownloadJobEnumerator(MapInterface map, MapSource mapSource, int layer,
			TarIndexedArchive tileArchive, DownloadJobListener listener) {
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
