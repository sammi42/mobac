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

import tac.gui.AtlasProgress;
import tac.gui.AtlasProgress.DownloadControlerListener;
import tac.program.JobDispatcher.Job;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.DownloadableElement;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.program.model.AtlasOutputFormat;
import tac.program.model.Settings;
import tac.program.model.TileImageParameters;
import tac.tar.TarIndex;
import tac.tar.TarIndexedArchive;
import tac.utilities.TACExceptionHandler;

public class AtlasThread extends Thread implements DownloadJobListener, DownloadControlerListener {

	/**
	 * Allows to skip the download part for debugging reasons
	 */
	private static boolean SKIP_DOWNLOAD = false;

	private static int threadNum = 0;
	private static Logger log = Logger.getLogger(AtlasThread.class);

	private DownloadJobProducer djp = null;
	private JobDispatcher downloadJobDispatcher;
	private AtlasProgress ap;

	private AtlasInterface atlasInterface;

	private TarIndexedArchive tileArchive;

	private int activeDownloads = 0;
	private int jobsProduced = 0;
	private int jobsCompleted = 0;
	private int jobsRetryError = 0;
	private int jobsPermanentError = 0;

	public AtlasThread(AtlasInterface atlasInterface) {
		super("AtlasThread " + getNextThreadNum());
		ap = new AtlasProgress(this);
		this.atlasInterface = atlasInterface;
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

		String workingDir = System.getProperty("user.dir");

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String formattedDateString = sdf.format(date);

		File atlasDir = new File(workingDir + "/atlases/" + atlasInterface.getName() + "_"
				+ formattedDateString);
		atlasDir.mkdirs();

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

		TileStore ts = TileStore.getInstance();
		Settings s = Settings.getInstance();

		File tileArchiveFile = null;
		TarIndex tileIndex = null;

		Thread t = Thread.currentThread();
		downloadJobDispatcher = new JobDispatcher(s.getDownloadThreadCount());
		try {
			for (LayerInterface layer : atlasInterface) {
				int apMax = (int) layer.calculateTilesToDownload();
				for (MapInterface map : layer) {
					ap.initMapDownload(map);
					jobsCompleted = 0;
					jobsRetryError = 0;
					jobsPermanentError = 0;
					if (t.isInterrupted())
						throw new InterruptedException();

					// Prepare the tile store directory
					ts.prepareTileStore(map.getMapSource());

					/***
					 * In this section of code below, tiles for Atlas is being
					 * downloaded and saved in the temporary layer tar file in
					 * the system temp directory.
					 **/
					int zoom = map.getZoom();

					ap.setZoomLevel(zoom);

					jobsProduced = 0;
					tileArchive = null;
					if (!SKIP_DOWNLOAD) {
						String tempSuffix = "TAC_" + atlasInterface.getName() + "_" + zoom + "_";
						tileArchiveFile = File.createTempFile(tempSuffix, ".tar");
						// If something goes wrong the temp file only
						// persists until the VM exits
						tileArchiveFile.deleteOnExit();
						log.debug("Writing downloaded tiles to " + tileArchiveFile.getPath());
						tileArchive = new TarIndexedArchive(tileArchiveFile, apMax);
						djp = new DownloadJobProducer((DownloadableElement) map);

						boolean failedMessageAnswered = false;

						while (djp.isAlive() || (downloadJobDispatcher.getWaitingJobCount() > 0)
								|| downloadJobDispatcher.isAtLeastOneWorkerActive()) {
							Thread.sleep(500);
							if (!failedMessageAnswered && (jobsRetryError > 50)) {
								downloadJobDispatcher.pause();
								int answer = JOptionPane
										.showConfirmDialog(
												ap,
												"Multiple tile downloads have failed. "
														+ "Something may be wrong with your connection to the "
														+ "download server or your selected area.\n"
														+ "Are you sure you want to continue "
														+ "and create the atlas anyway?",
												"Download of more than 100 tiles failed",
												JOptionPane.ERROR_MESSAGE);
								failedMessageAnswered = true;
								if (answer != JOptionPane.YES_OPTION) {
									downloadJobDispatcher.cancelOutstandingJobs();
									downloadJobDispatcher.terminateAllWorkerThreads();
									djp.cancel();
									return;
								}
								downloadJobDispatcher.resume();
							}
						}
						djp = null;
						log.debug("All download jobs has been completed!");
						tileArchive.writeEndofArchive();
						tileArchive.close();
						tileIndex = tileArchive.getTarIndex();
						if (tileIndex.size() != (map.calculateTilesToDownload())) {
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
								return;
						}
					}
					log.debug("Starting to create atlas from downloaded tiles");

					MapCreator mc;
					if (atlasInterface.getOutputFormat() != AtlasOutputFormat.AndNav) {
						TileImageParameters parameters = map.getParameters();
						if (parameters == null)
							mc = new MapCreator(map, tileIndex, atlasDir);
						else
							mc = new MapCreatorCustom(map, tileIndex, atlasDir, parameters);
					} else
						mc = new MapCreatorAndNav(map, tileIndex, atlasDir);
					mc.createMap();
					downloadJobDispatcher.cancelOutstandingJobs();
					tileIndex.closeAndDelete();
				}
			}
		} finally {
			// In case of an abort: Stop create new download jobs
			if (djp != null)
				djp.cancel();
			downloadJobDispatcher.terminateAllWorkerThreads();
			if (tileArchive != null)
				tileArchive.close();
			if (tileIndex != null)
				tileIndex.closeAndDelete();
			if (tileArchiveFile != null)
				tileArchiveFile.delete();
		}

		if (atlasInterface.getOutputFormat() == AtlasOutputFormat.TaredAtlas)
			AtlasTarCreator.createAtlasCrTarArchive(atlasDir);
		else if (atlasInterface.getOutputFormat() == AtlasOutputFormat.UntaredAtlas) {
			File crtba = new File(atlasDir.getAbsolutePath(), "cr.tba");
			try {
				FileWriter fw = new FileWriter(crtba);
				fw.write("Atlas 1.0\r\n");
				fw.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}

		ap.atlasCreationFinished();
	}

	public void pauseResumeDownload() {
		if (downloadJobDispatcher.isPaused())
			downloadJobDispatcher.resume();
		else
			downloadJobDispatcher.pause();
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

		public DownloadJobProducer(DownloadableElement de) {
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
