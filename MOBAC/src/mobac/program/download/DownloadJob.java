package mobac.program.download;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import mobac.exceptions.DownloadFailedException;
import mobac.exceptions.UnrecoverableDownloadException;
import mobac.program.JobDispatcher;
import mobac.program.JobDispatcher.Job;
import mobac.program.interfaces.DownloadJobListener;
import mobac.utilities.tar.TarIndexedArchive;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


public class DownloadJob implements Job {

	private static final int MAX_RETRIES = 1;

	static Logger log = Logger.getLogger(DownloadJob.class);

	int errorCounter = 0;

	final MapSource mapSource;
	final int layer;
	final int xValue;
	final int yValue;
	final int zoomValue;
	final TarIndexedArchive tileArchive;
	final DownloadJobListener listener;

	public DownloadJob(MapSource mapSource, int layer, int xValue, int yValue, int zoomValue,
			TarIndexedArchive tileArchive, DownloadJobListener listener) {
		this.mapSource = mapSource;
		this.layer = layer;
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
			int bytes = TileDownLoader.getImage(layer, xValue, yValue, zoomValue, mapSource,
					tileArchive);
			listener.jobFinishedSuccessfully(bytes);
		} catch (UnrecoverableDownloadException e) {
			listener.jobFinishedWithError(false);
			log.error("Download of tile z" + zoomValue + "_x" + xValue + "_y" + yValue
					+ " failed with an unrecoverable error: " + e.getCause());
		} catch (InterruptedException e) {
			throw e;
		} catch (SocketTimeoutException e) {
			processError(dispatcher, e);
		} catch (ConnectException e) {
			processError(dispatcher, e);
		} catch (DownloadFailedException e) {
			processError(dispatcher, e);
		} catch (Exception e) {
			processError(dispatcher, e);
			throw e;
		}
	}

	private void processError(JobDispatcher dispatcher, Exception e) {
		errorCounter++;
		// Reschedule job to try it later again
		if (errorCounter <= MAX_RETRIES) {
			listener.jobFinishedWithError(true);
			log.warn("Download of tile z" + zoomValue + "_x" + xValue + "_y" + yValue
					+ " failed: \"" + e.getMessage() + "\" (tries: " + errorCounter
					+ ") - rescheduling download job");
			dispatcher.addErrorJob(this);
		} else {
			listener.jobFinishedWithError(false);
			log.error("Download of tile z" + zoomValue + "_x" + xValue + "_y" + yValue
					+ " failed again: \"" + e.getMessage() + "\". Retry limit reached, "
					+ "job will not be rescheduled (no further try)");
		}
	}

	@Override
	public String toString() {
		return "DownloadJob l=" + layer + " x=" + xValue + " y=" + yValue + " z=" + zoomValue;
	}

}
