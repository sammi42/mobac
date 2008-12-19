package tac.program;

import java.io.File;
import java.util.Enumeration;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.program.JobDispatcher.Job;
import tac.program.interfaces.DownloadJobListener;

public class DownloadJobEnumerator implements Enumeration<Job> {

	private File downloadDestinationDir;
	private DownloadJobListener listener;
	private int xMin;
	private int xMax;
	private int yMax;
	private int zoom;
	private TileSource tileSource;

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
			TileSource tileSource, File downloadDestinationDir, DownloadJobListener listener) {
		super();
		this.downloadDestinationDir = downloadDestinationDir;
		this.listener = listener;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.zoom = zoom;
		this.tileSource = tileSource;
		y = yMin;
		x = xMin;

		nextJob = new DownloadJob(downloadDestinationDir, tileSource, x, y, zoom, listener);
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
		nextJob = new DownloadJob(downloadDestinationDir, tileSource, x, y, zoom, listener);
		return job;
	}
}
