package tac.program;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import tac.exceptions.MapDownloadSkippedException;
import tac.gui.AtlasProgress;
import tac.gui.AtlasProgress.DownloadControlerListener;
import tac.program.JobDispatcher.Job;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.DownloadableElement;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.program.mapcreators.MapCreator;
import tac.program.mapcreators.MapCreatorTrekBuddy;
import tac.program.mapcreators.MapCreatorTrekBuddyCustom;
import tac.program.model.AtlasOutputFormat;
import tac.program.model.Settings;
import tac.program.model.TileImageParameters;
import tac.tar.TarIndex;
import tac.tar.TarIndexedArchive;
import tac.utilities.TACExceptionHandler;
import tac.utilities.Utilities;

public class AtlasThread extends Thread implements DownloadJobListener, DownloadControlerListener {

	private static int threadNum = 0;
	private static Logger log = Logger.getLogger(AtlasThread.class);

	private DownloadJobProducer djp = null;
	private JobDispatcher downloadJobDispatcher;
	private AtlasProgress ap;

	private AtlasInterface atlasInterface;

	private int activeDownloads = 0;
	private int jobsProduced = 0;
	private int jobsCompleted = 0;
	private int jobsRetryError = 0;
	private int jobsPermanentError = 0;

	//private final TileStore ts;
	private File atlasDir = null;

	public AtlasThread(AtlasInterface atlasInterface) {
		super("AtlasThread " + getNextThreadNum());
		ap = new AtlasProgress(this);
		this.atlasInterface = atlasInterface;
		//ts = TileStore.getInstance();
	}

	private static synchronized int getNextThreadNum() {
		threadNum++;
		return threadNum;
	}

	public void run() {
		TACExceptionHandler.registerForCurrentThread();
		log.info("Starting altas creation");
		ap.setDownloadControlerListener(this);
		try {
			createAtlas();
			ap.atlasCreationFinished();
			log.info("Altas creation finished");
		} catch (OutOfMemoryError e) {
			System.gc();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null,
							"TrekBuddy Atlas Creator has run out of memory.\n"
									+ "Please make sure you have started it via the "
									+ "provided startup scripts 'start.cmd' (Windows) "
									+ "/ 'start.sh' (Linux).\n"
									+ "Those scripts are increasing the maximum memory "
									+ "usable by TAC to 512 MB.", "Out of memory",
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
			TACExceptionHandler.showExceptionDialog(e);
		}
		System.gc();
	}

	protected void createAtlas() throws InterruptedException, IOException {

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String atlasDirName = atlasInterface.getName() + "_" + sdf.format(date);
		File atlasOutputDir = Settings.getInstance().getAtlasOutputDirectory();

		atlasDir = new File(atlasOutputDir, atlasDirName);
		Utilities.mkDirs(atlasDir);

		/***
		 * In this section of code below, atlas is created.
		 **/

		long totalNrOfTiles = atlasInterface.calculateTilesToDownload();

		if (totalNrOfTiles > Integer.MAX_VALUE) {
			JOptionPane.showMessageDialog(null, "The number of tiles to download is too high!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		ap.init(atlasInterface);
		ap.setVisible(true);

		Settings s = Settings.getInstance();

		downloadJobDispatcher = new JobDispatcher(s.downloadThreadCount);
		try {
			for (LayerInterface layer : atlasInterface) {
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
						TACExceptionHandler.processException(e);
						// JOptionPane.showMessageDialog(null,
						// "An error occured: " + e.getMessage()
						// + "\n[" + e.getClass().getSimpleName() + "]\n\n"
						// + "Press OK to continue atlas creation.", "Error",
						// JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} finally {
			// In case of an abort: Stop create new download jobs
			if (djp != null)
				djp.cancel();
			downloadJobDispatcher.terminateAllWorkerThreads();
		}

		switch (atlasInterface.getOutputFormat()) {
		case TaredAtlas:
			AtlasTarCreator.createAtlasCrTarArchive(atlasDir);
			break;
		case UntaredAtlas:
			createTbaFile("cr");
			break;
		}

		ap.atlasCreationFinished();
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
		jobsProduced = 0;

		ap.initMapDownload(map);
		if (currentThread().isInterrupted())
			throw new InterruptedException();

		// Prepare the tile store directory
		//ts.prepareTileStore(map.getMapSource());

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
			String tempSuffix = "TAC_" + atlasInterface.getName() + "_" + zoom + "_";
			File tileArchiveFile = File.createTempFile(tempSuffix, ".tar");
			// If something goes wrong the temp file only
			// persists until the VM exits
			tileArchiveFile.deleteOnExit();
			log.debug("Writing downloaded tiles to " + tileArchiveFile.getPath());
			tileArchive = new TarIndexedArchive(tileArchiveFile, tileCount);
			djp = new DownloadJobProducer(tileArchive, (DownloadableElement) map);

			boolean failedMessageAnswered = false;

			while (djp.isAlive() || (downloadJobDispatcher.getWaitingJobCount() > 0)
					|| downloadJobDispatcher.isAtLeastOneWorkerActive()) {
				Thread.sleep(500);
				if (!failedMessageAnswered && (jobsRetryError > 50)) {
					downloadJobDispatcher.pause();
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
						downloadJobDispatcher.resume();
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
			if (tileIndex.size() < tileCount) {
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

			AtlasOutputFormat aof = atlasInterface.getOutputFormat();
			MapCreator mc = null;
			switch (aof) {
			case TaredAtlas:
			case UntaredAtlas:
				TileImageParameters parameters = map.getParameters();
				if (parameters == null)
					mc = new MapCreatorTrekBuddy(map, tileIndex, atlasDir);
				else
					mc = new MapCreatorTrekBuddyCustom(map, tileIndex, atlasDir, parameters);
				break;
			default:
				mc = aof.createMapCreatorInstance(map, tileIndex, atlasDir);
			}
			mc.createMap();
		} catch (Exception e) {
			log.error("Error in createMap: " + e.getMessage(), e);
			throw e;
		} finally {
			if (tileIndex != null)
				tileIndex.closeAndDelete();
			if (tileArchive != null)
				tileArchive.delete();
		}
		return true;
	}

	public void pauseResumeDownload() {
		if (downloadJobDispatcher.isPaused())
			downloadJobDispatcher.resume();
		else
			downloadJobDispatcher.pause();
	}

	private void createTbaFile(String name) {
		File crtba = new File(atlasDir.getAbsolutePath(), name + ".tba");
		try {
			FileWriter fw = new FileWriter(crtba);
			fw.write("Atlas 1.0\r\n");
			fw.close();
		} catch (IOException e) {
			log.error("", e);
		}
	}

	/**
	 * Stop listener from {@link AtlasProgress}
	 */
	public void stopDownload() {
		try {
			DownloadJobProducer djp_ = djp;
			if (djp_ != null)
				djp_.cancel();
			if (downloadJobDispatcher != null)
				downloadJobDispatcher.terminateAllWorkerThreads();
			this.interrupt();
		} catch (Exception ex) {
			// ingored
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
		Toolkit.getDefaultToolkit().beep();
		ap.setErrorCounter(jobsRetryError, jobsPermanentError);
		ap.updateGUI();
	}

	public AtlasProgress getAtlasProgress() {
		return ap;
	}

	/**
	 * 
	 * Creates the jobs for downloading tiles. If the job queue is full it will
	 * block on {@link JobDispatcher#addJob(Job)}
	 * 
	 */
	public class DownloadJobProducer extends Thread {

		private Logger log = Logger.getLogger(DownloadJobProducer.class);

		Enumeration<Job> jobEnumerator;

		public DownloadJobProducer(TarIndexedArchive tileArchive, DownloadableElement de) {
			jobEnumerator = de.getDownloadJobs(tileArchive, AtlasThread.this);
			start();
		}

		@Override
		public void run() {
			try {
				while (jobEnumerator.hasMoreElements()) {
					Job job = jobEnumerator.nextElement();
					downloadJobDispatcher.addJob(job);
					log.trace("Job added: " + job);
					jobsProduced++;
				}
				log.debug("All download jobs has been generated");
			} catch (InterruptedException e) {
				downloadJobDispatcher.cancelOutstandingJobs();
				log.error("Download job generation interrupted");
			}
		}

		public void cancel() {
			try {
				interrupt();
			} catch (Exception e) {
			}
		}
	}

}
