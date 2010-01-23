package mobac.program;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapDownloadSkippedException;
import mobac.gui.AtlasProgress;
import mobac.gui.AtlasProgress.AtlasCreationController;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.download.DownloadJobProducerThread;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.interfaces.DownloadableElement;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.Settings;
import mobac.program.tilestore.TileStore;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.tar.TarIndex;
import mobac.utilities.tar.TarIndexedArchive;

import org.apache.log4j.Logger;

public class AtlasThread extends Thread implements DownloadJobListener, AtlasCreationController {

	private static int threadNum = 0;
	private static Logger log = Logger.getLogger(AtlasThread.class);

	private DownloadJobProducerThread djp = null;
	private JobDispatcher downloadJobDispatcher;
	private AtlasProgress ap; // The GUI showing the progress

	private AtlasInterface atlasInterface;
	private AtlasCreator atlasCreator = null;
	private PauseResumeHandler pauseResumeHandler;

	private int activeDownloads = 0;
	private int jobsCompleted = 0;
	private int jobsRetryError = 0;
	private int jobsPermanentError = 0;

	public AtlasThread(AtlasInterface atlasInterface) throws AtlasTestException {
		super("AtlasThread " + getNextThreadNum());
		ap = new AtlasProgress(this);
		this.atlasInterface = atlasInterface;
		atlasCreator = atlasInterface.getOutputFormat().createAtlasCreatorInstance();
		testAtlas();
		TileStore.getInstance().closeAll(false);
		pauseResumeHandler = new PauseResumeHandler();
	}

	private void testAtlas() throws AtlasTestException {
		try {
			for (LayerInterface layer : atlasInterface) {
				for (MapInterface map : layer) {
					if (!atlasCreator.testMapSource(map.getMapSource()))
						throw new AtlasTestException("The selected atlas output format \""
								+ atlasInterface.getOutputFormat()
								+ "\" does not support the map source \"" + map.getMapSource()
								+ "\"");
				}
			}
		} catch (AtlasTestException e) {
			throw e;
		} catch (Exception e) {
			throw new AtlasTestException(e);
		}
	}

	private static synchronized int getNextThreadNum() {
		threadNum++;
		return threadNum;
	}

	public void run() {
		GUIExceptionHandler.registerForCurrentThread();
		log.info("Starting altas creation");
		ap.setDownloadControlerListener(this);
		try {
			createAtlas();
			log.info("Altas creation finished");
		} catch (OutOfMemoryError e) {
			System.gc();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null,
							"Mobile Atlas Creator has run out of memory.\n"
									+ "Please make sure you have started it via the "
									+ "provided startup scripts 'start.cmd' (Windows) "
									+ "/ 'start.sh' (Linux).\n"
									+ "Those scripts are increasing the maximum memory usable "
									+ "by Mobile Atlas Creator to 512 MB.", "Out of memory",
							JOptionPane.ERROR_MESSAGE);
					ap.closeWindow();
				}
			});
			log.error("Out of memory: ", e);
		} catch (InterruptedException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null, "Atlas download aborted", "Information",
							JOptionPane.INFORMATION_MESSAGE);
					ap.closeWindow();
				}
			});
			log.info("Altas creation was interrupted by user");
		} catch (Exception e) {
			log.error("Altas creation aborted because of an error: ", e);
			GUIExceptionHandler.showExceptionDialog(e);
		}
		System.gc();
	}

	protected void createAtlas() throws InterruptedException, IOException {

		/***
		 * In this section of code below, atlas is created.
		 **/
		long totalNrOfTiles = atlasInterface.calculateTilesToDownload();

		if (totalNrOfTiles > Integer.MAX_VALUE) {
			JOptionPane.showMessageDialog(null, "The number of tiles to download is too high!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			atlasCreator.startAtlasCreation(atlasInterface);
		} catch (AtlasTestException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Atlas format restriction violated", JOptionPane.ERROR_MESSAGE);
			return;
		}

		ap.init(atlasInterface);
		ap.setVisible(true);

		Settings s = Settings.getInstance();

		downloadJobDispatcher = new JobDispatcher(s.downloadThreadCount, pauseResumeHandler);
		try {
			for (LayerInterface layer : atlasInterface) {
				atlasCreator.initLayerCreation(layer);
				for (MapInterface map : layer) {
					try {
						while (!createMap(map))
							;
					} catch (InterruptedException e) {
						throw e; // User has aborted
					} catch (MapDownloadSkippedException e) {
						// Do nothing and continue with next map
					} catch (Exception e) {
						log.error("", e);
						String[] options = { "Continue", "Abort", "Show error report" };
						int a = JOptionPane.showOptionDialog(null, "An error occured: "
								+ e.getMessage() + "\n[" + e.getClass().getSimpleName() + "]\n\n",
								"Error", 0, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
						switch (a) {
						case 2:
							GUIExceptionHandler.processException(e);
						case 1:
							throw new InterruptedException();
						}
					}
				}
				atlasCreator.finishLayerCreation();
			}
		} catch (InterruptedException e) {
			atlasCreator.abortAtlasCreation();
			throw e;
		} catch (Error e) {
			atlasCreator.abortAtlasCreation();
			throw e;
		} finally {
			// In case of an abort: Stop create new download jobs
			if (djp != null)
				djp.cancel();
			downloadJobDispatcher.terminateAllWorkerThreads();
			if (!atlasCreator.isAborted())
				atlasCreator.finishAtlasCreation();
			ap.atlasCreationFinished();
		}

	}

	/**
	 * 
	 * @param map
	 * @return true if map creation process was finished and false if something
	 *         went wrong and the user decided to retry map download
	 * @throws Exception
	 */
	public boolean createMap(MapInterface map) throws Exception {
		TarIndex tileIndex = null;
		TarIndexedArchive tileArchive = null;

		jobsCompleted = 0;
		jobsRetryError = 0;
		jobsPermanentError = 0;

		ap.initMapDownload(map);
		if (currentThread().isInterrupted())
			throw new InterruptedException();

		// Prepare the tile store directory
		// ts.prepareTileStore(map.getMapSource());

		/***
		 * In this section of code below, tiles for Atlas is being downloaded
		 * and saved in the temporary layer tar file in the system temp
		 * directory.
		 **/
		int zoom = map.getZoom();

		final int tileCount = map.calculateTilesToDownload();

		ap.setZoomLevel(zoom);
		try {
			tileArchive = null;
			String tempSuffix = "MOBAC_" + atlasInterface.getName() + "_" + zoom + "_";
			File tileArchiveFile = File.createTempFile(tempSuffix, ".tar");
			// If something goes wrong the temp file only
			// persists until the VM exits
			tileArchiveFile.deleteOnExit();
			log.debug("Writing downloaded tiles to " + tileArchiveFile.getPath());
			tileArchive = new TarIndexedArchive(tileArchiveFile, tileCount);
			djp = new DownloadJobProducerThread(this, downloadJobDispatcher, tileArchive,
					(DownloadableElement) map);

			boolean failedMessageAnswered = false;

			while (djp.isAlive() || (downloadJobDispatcher.getWaitingJobCount() > 0)
					|| downloadJobDispatcher.isAtLeastOneWorkerActive()) {
				Thread.sleep(500);
				if (!failedMessageAnswered && (jobsRetryError > 50) && !ap.ignoreDownloadErrors()) {
					pauseResumeHandler.pause();
					String[] answers = new String[] { "Continue", "Retry", "Skip", "Abort" };
					String message = "<html>Multiple tile downloads have failed. "
							+ "Something may be wrong with your connection to the "
							+ "download server or your selected area. "
							+ "<br>Do you want to:<br><br>"
							+ "<u>Continue</u> map download and ignore the errors? (results in blank/missing tiles)<br>"
							+ "<u>Retry</u> to download this map, by starting over?<br>"
							+ "<u>Skip</u> the current map and continue to process other maps in the atlas?<br>"
							+ "<u>Abort</u> the current map and atlas creation process?<br></html>";
					int answer = JOptionPane.showOptionDialog(ap, message,
							"Multiple download errors - how to proceed?", 0,
							JOptionPane.QUESTION_MESSAGE, null, answers, answers[0]);
					failedMessageAnswered = true;
					switch (answer) {
					case 0: // Continue
						pauseResumeHandler.resume();
						break;
					case 1: // Retry
						djp.cancel();
						djp = null;
						downloadJobDispatcher.cancelOutstandingJobs();
						return false;
					case 2: // Skip
						downloadJobDispatcher.cancelOutstandingJobs();
						throw new MapDownloadSkippedException();
					default: // Abort or close dialog
						downloadJobDispatcher.cancelOutstandingJobs();
						downloadJobDispatcher.terminateAllWorkerThreads();
						throw new InterruptedException();
					}
				}
			}
			djp = null;
			log.debug("All download jobs has been completed!");
			tileArchive.writeEndofArchive();
			tileArchive.close();
			tileIndex = tileArchive.getTarIndex();
			if (tileIndex.size() < tileCount && !ap.ignoreDownloadErrors()) {
				log.debug("Expected tile count: " + tileCount + " downloaded tile count: "
						+ tileIndex.size());
				int answer = JOptionPane.showConfirmDialog(ap,
						"Something is wrong with download of atlas tiles.\n"
								+ "The amount of downladed tiles is not as "
								+ "high as it was calculated.\nTherfore tiles "
								+ "will be missing in the created atlas.\n\n"
								+ "Are you sure you want to continue "
								+ "and create the atlas anyway?",
						"Error - tiles are missing - do you want to continue anyway?",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
				if (answer != JOptionPane.YES_OPTION)
					throw new InterruptedException();
			}
			downloadJobDispatcher.cancelOutstandingJobs();
			log.debug("Starting to create atlas from downloaded tiles");

			atlasCreator.initializeMap(map, tileIndex);
			atlasCreator.createMap();
		} catch (Error e) {
			log.error("Error in createMap: " + e.getMessage(), e);
			throw e;
		} finally {
			if (tileIndex != null)
				tileIndex.closeAndDelete();
			else if (tileArchive != null)
				tileArchive.delete();
		}
		return true;
	}

	public void pauseResumeAtlasCreation() {
		if (pauseResumeHandler.isPaused()) {
			log.debug("Atlas creation resumed");
			pauseResumeHandler.resume();
		} else {
			log.debug("Atlas creation paused");
			pauseResumeHandler.pause();
		}
	}

	public boolean isPaused() {
		return pauseResumeHandler.isPaused();
	}

	public PauseResumeHandler getPauseResumeHandler() {
		return pauseResumeHandler;
	}

	/**
	 * Stop listener from {@link AtlasProgress}
	 */
	public void abortAtlasCreation() {
		try {
			DownloadJobProducerThread djp_ = djp;
			if (djp_ != null)
				djp_.cancel();
			if (downloadJobDispatcher != null)
				downloadJobDispatcher.terminateAllWorkerThreads();
			pauseResumeHandler.resume();
			this.interrupt();
		} catch (Exception e) {
			log.error("Exception thrown in stopDownload()" + e.getMessage());
		}
	}

	public int getActiveDownloads() {
		return activeDownloads;
	}

	public synchronized void jobStarted() {
		activeDownloads++;
	}

	public void jobFinishedSuccessfully(int bytesDownloaded) {
		synchronized (this) {
			ap.addDownloadedBytes(bytesDownloaded);
			ap.incMapDownloadProgress();
			activeDownloads--;
			jobsCompleted++;
		}
		ap.updateGUI();
	}

	public void jobFinishedWithError(boolean retry) {
		synchronized (this) {
			activeDownloads--;
			if (retry)
				jobsRetryError++;
			else {
				jobsPermanentError++;
				ap.incMapDownloadProgress();
			}
		}
		if (!ap.ignoreDownloadErrors())
			Toolkit.getDefaultToolkit().beep();
		ap.setErrorCounter(jobsRetryError, jobsPermanentError);
		ap.updateGUI();
	}

	public AtlasProgress getAtlasProgress() {
		return ap;
	}

}
