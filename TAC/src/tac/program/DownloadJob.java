package tac.program;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.UnrecoverableDownloadException;
import tac.program.JobDispatcher.Job;
import tac.program.interfaces.DownloadJobListener;
import tac.tar.TarIndexedArchive;

public class DownloadJob implements Job {

	static Logger log = Logger.getLogger(DownloadJob.class);

	int errorCounter = 0;

	MapSource tileSource;
	int xValue;
	int yValue;
	int zoomValue;
	TarIndexedArchive tileArchive;
	DownloadJobListener listener;

	public DownloadJob(MapSource tileSource, int xValue, int yValue, int zoomValue,
			TarIndexedArchive tileArchive, DownloadJobListener listener) {
		this.tileSource = tileSource;
		this.xValue = xValue;
		this.yValue = yValue;
		this.zoomValue = zoomValue;
		this.tileArchive = tileArchive;
		this.listener = listener;
	}

	public void run(JobDispatcher dispatcher) throws Exception {
		try {
			// Thread.sleep(1500);
			listener.jobStarted();
			int bytes = TileDownLoader.getImage(xValue, yValue, zoomValue, tileSource, tileArchive);
			listener.jobFinishedSuccessfully(bytes);
		} catch (UnrecoverableDownloadException e) {
			listener.jobFinishedWithError(false);
			log.error("Download of tile z" + zoomValue + "_x" + xValue + "_y" + yValue
					+ "failed with an unrecoverable error: " + e.getCause());
		} catch (InterruptedException e) {
			throw e;
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
