package tac.program;

import java.awt.Point;
import java.util.Enumeration;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.JobDispatcher.Job;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndexedArchive;

public class DownloadJobEnumerator implements Enumeration<Job> {

	private DownloadJobListener listener;
	private int xMin;
	private int xMax;
	private int yMax;
	private int zoom;
	private MapSource mapSource;
	private TarIndexedArchive tileArchive;

	private int x, y;
	private DownloadJob nextJob;

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
	 */
	public DownloadJobEnumerator(int xMin, int xMax, int yMin, int yMax, int zoom,
			MapSource mapSource, TarIndexedArchive tileArchive, DownloadJobListener listener) {
		this.listener = listener;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.zoom = zoom;
		this.tileArchive = tileArchive;
		this.mapSource = mapSource;
		y = yMin;
		x = xMin;

		nextJob = new DownloadJob(mapSource, x, y, zoom, tileArchive, listener);
	}

	public DownloadJobEnumerator(MapInterface map, TarIndexedArchive tileArchive,
			DownloadJobListener listener) {
		this.listener = listener;
		Point minCoord = map.getMinTileCoordinate();
		Point maxCoord = map.getMaxTileCoordinate();
		
		this.xMin = minCoord.x / Tile.SIZE;
		this.xMax = maxCoord.x / Tile.SIZE;
		int yMin = minCoord.y / Tile.SIZE;
		this.yMax = maxCoord.y / Tile.SIZE;
		this.zoom = map.getZoom();
		this.tileArchive = tileArchive;
		this.mapSource = map.getMapSource();
		y = yMin;
		x = xMin;

		nextJob = new DownloadJob(mapSource, x, y, zoom, tileArchive, listener);
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
		nextJob = new DownloadJob(mapSource, x, y, zoom, tileArchive, listener);
		return job;
	}
}
