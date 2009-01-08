package tac.program;

import java.io.File;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.program.JobDispatcher.Job;
import tac.program.interfaces.DownloadJobListener;

public class DownloadJob implements Job {

	static Logger log = Logger.getLogger(DownloadJob.class);

	int errorCounter = 0;

	TileSource tileSource;
	int xValue;
	int yValue;
	int zoomValue;
	File destinationFolder;
	DownloadJobListener listener;

	public DownloadJob(File destinationFolder, TileSource tileSource, int xValue, int yValue,
			int zoomValue, DownloadJobListener listener) {
		this.destinationFolder = destinationFolder;
		this.tileSource = tileSource;
		this.xValue = xValue;
		this.yValue = yValue;
		this.zoomValue = zoomValue;
		this.listener = listener;
	}

	public void run(JobDispatcher dispatcher) throws Exception {
		try {
			// Thread.sleep(1500);
			listener.jobStarted();
			int bytes = TileDownLoader.getImage(xValue, yValue, zoomValue, destinationFolder,
					tileSource, true);
			listener.jobFinishedSuccessfully(bytes);
		} catch (Exception e) {
			errorCounter++;
			// Reschedule job to try it later again
			if (errorCounter < 3) {
				listener.jobFinishedWithError(true);
				log.warn("Download of tile z" + zoomValue + "_x" + xValue + "_y" + yValue
						+ " failed (times: " + errorCounter + ") - rescheduling download job");
				dispatcher.addErrorJob(this);
			} else {
				listener.jobFinishedWithError(false);
				log.error("Download of tile z" + zoomValue + "_x" + xValue + "_y" + yValue
						+ "failed again. Retry limit reached, "
						+ "job will not be rescheduled (no further try)");
			}
			throw e;
		}
	}

	@Override
	public String toString() {
		return "DownloadJob x=" + xValue + " y=" + yValue + " z=" + zoomValue;
	}

}
