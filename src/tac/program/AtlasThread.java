package tac.program;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.gui.AtlasProgress;
import tac.program.JobDispatcher.Job;
import tac.program.interfaces.DownloadJobListener;
import tac.program.model.AtlasOutputFormat;
import tac.program.model.MapSlice;
import tac.tar.TarIndex;
import tac.tar.TarIndexedArchive;
import tac.utilities.TACExceptionHandler;

public class AtlasThread extends Thread implements DownloadJobListener, ActionListener {

	/**
	 * Allows to skip the download part for debugging reasons
	 */
	private static boolean SKIP_DOWNLOAD = false;

	private static int threadNum = 0;
	private static Logger log = Logger.getLogger(AtlasThread.class);

	private DownloadJobProducer djp = null;
	private JobDispatcher downloadJobDispatcher;
	private AtlasProgress ap;

	private MapSelection mapSelection;
	private TileSource tileSource;
	private String atlasName;
	private SelectedZoomLevels sZL;
	private AtlasOutputFormat atlasOutputFormat;
	private MapCreatorCustom.TileImageParameters customTileParameters;

	private TarIndexedArchive tileArchive;

	private int activeDownloads = 0;
	private int jobsProduced = 0;
	private int jobsCompleted = 0;
	private int jobsRetryError = 0;
	private int jobsPermanentError = 0;

	public AtlasThread(String atlasName, TileSource tileSource, MapSelection mapSelection,
			SelectedZoomLevels sZL, AtlasOutputFormat atlasOutputFormat,
			MapCreatorCustom.TileImageParameters customTileParameters) {
		super("AtlasThread " + getNextThreadNum());
		this.tileSource = tileSource;
		this.atlasName = atlasName;
		this.mapSelection = mapSelection;
		this.sZL = sZL;
		this.atlasOutputFormat = atlasOutputFormat;
		this.customTileParameters = customTileParameters;
		ap = new AtlasProgress(this);
	}

	private static synchronized int getNextThreadNum() {
		threadNum++;
		return threadNum;
	}

	public void run() {
		TACExceptionHandler.registerForCurrentThread();
		log.info("Starting altas creation");
		log.trace("Atlas to download:\n\t" + "MapSource: " + tileSource + "\n\tAtlas name: "
				+ atlasName + "\n\tMap selection: " + mapSelection + "\n\tSelectedZoomLevels: "
				+ sZL);
		ap.setAbortListener(this);
		try {
			createAtlas();
			ap.atlasCreationFinished();
			log.info("Altas creation finished");
		} catch (OutOfMemoryError e) {
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
	}

	protected void createAtlas() throws InterruptedException, IOException {

		String workingDir = System.getProperty("user.dir");

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String formattedDateString = sdf.format(date);

		File atlasDir = new File(workingDir + "/atlases/" + formattedDateString);
		atlasDir.mkdirs();

		/***
		 * In this section of code below, atlas is created.
		 **/

		int nrOfLayers = sZL.getNrOfLayers();
		int[] zoomLevels = sZL.getZoomLevels();

		long totalNrOfTiles = 0;

		for (int i = 0; i < nrOfLayers; i++) {
			totalNrOfTiles += mapSelection.calculateNrOfTiles(zoomLevels[i]);
		}
		if (totalNrOfTiles > Integer.MAX_VALUE) {
			JOptionPane.showMessageDialog(null, "The number of tiles to download is too high!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		ap.init((int) totalNrOfTiles, nrOfLayers);
		updateGUI();
		ap.setVisible(true);

		TileStore ts = TileStore.getInstance();
		Settings s = Settings.getInstance();

		File tileArchiveFile = null;
		TarIndex tileIndex = null;

		Thread t = Thread.currentThread();
		downloadJobDispatcher = new JobDispatcher(s.getThreadCount());
		try {
			for (int layer = 0; layer < nrOfLayers; layer++) {
				jobsCompleted = 0;
				jobsRetryError = 0;
				jobsPermanentError = 0;
				if (t.isInterrupted())
					throw new InterruptedException();

				// Prepare the tile store directory
				if (s.isTileStoreEnabled())
					ts.getTileFile(0, 0, 0, tileSource).getParentFile().mkdirs();

				/***
				 * In this section of code below, tiles for Atlas is being
				 * downloaded and saved in the temporary layer tar file in the
				 * system temp directory.
				 **/
				int zoom = zoomLevels[layer];

				Point topLeft = mapSelection.getTopLeftTileNumber(zoom);
				Point bottomRight = mapSelection.getBottomRightTileNumber(zoom);

				int apMax = (int) mapSelection.calculateNrOfTiles(zoom);

				int xMin = topLeft.x;
				int xMax = bottomRight.x;
				int yMin = topLeft.y;
				int yMax = bottomRight.y;
				ap.initLayer(apMax);
				ap.initMap(0);
				ap.setZoomLevel(zoom);

				jobsProduced = 0;
				tileArchive = null;
				if (!SKIP_DOWNLOAD) {
					String tempSuffix = "TAC_" + atlasName + "_" + zoom + "_";
					tileArchiveFile = File.createTempFile(tempSuffix, ".tar");
					// If something goes wrong the temp file only
					// persists until the VM exits
					tileArchiveFile.deleteOnExit();
					log.debug("Writing downloaded tiles to " + tileArchiveFile.getPath());
					tileArchive = new TarIndexedArchive(tileArchiveFile, apMax);
					djp = new DownloadJobProducer(topLeft, bottomRight, zoom);

					boolean failedMessageAnswered = false;

					while (djp.isAlive() || (downloadJobDispatcher.getWaitingJobCount() > 0)
							|| downloadJobDispatcher.isAtLeastOneWorkerActive()) {
						Thread.sleep(500);
						if (!failedMessageAnswered && (jobsRetryError > 100)) {
							int answer = JOptionPane.showConfirmDialog(ap,
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
						}
					}
					djp = null;
					log.debug("All download jobs has been completed!");
					tileArchive.writeEndofArchive();
					tileArchive.close();
					tileIndex = tileArchive.getTarIndex();
					if (tileIndex.size() != (mapSelection.calculateNrOfTiles(zoom))) {
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
				ap.setLayerProgress(apMax);

				File atlasFolder = new File(atlasDir, String.format("%s-%02d", new Object[] {
						atlasName, zoom }));
				atlasFolder.mkdir();

				log.debug("Starting to create atlas from downloaded tiles");

				int mapSize = s.getMaxMapSize();

				List<MapSlice> subMaps = MapSlicer.calculateMapSlices(mapSize, xMin, xMax, yMin,
						yMax);

				log.trace("Map will been splitted into " + subMaps.size()
						+ " sections because of TrekBuddy maximum map limitation");

				ap.initMap(subMaps.size());
				int mapNumber = 1;
				for (MapSlice smp : subMaps) {
					MapCreator mc;
					if (customTileParameters == null)
						mc = new MapCreator(smp, tileIndex, atlasFolder, atlasName, tileSource,
								zoom, atlasOutputFormat, mapNumber);
					else
						mc = new MapCreatorCustom(smp, tileIndex, atlasFolder, atlasName,
								tileSource, zoom, atlasOutputFormat, mapNumber,
								customTileParameters);
					mc.createMap();
					ap.setMap(mapNumber);
					mapNumber++;
				}
				ap.setLayer(layer + 1);
				downloadJobDispatcher.cancelOutstandingJobs();
				tileIndex.closeAndDelete();
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

		if (atlasOutputFormat == AtlasOutputFormat.TaredAtlas)
			AtlasTarCreator.createAtlasCrTarArchive(atlasDir);
		else if (atlasOutputFormat == AtlasOutputFormat.UntaredAtlas) {
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

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, "Atlas download completed", "Information",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	/**
	 * Abort listener from {@link AtlasProgress}
	 */
	public void actionPerformed(ActionEvent e) {
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

	private void updateGUI() {
		ap.incAtlasProgress();
		ap.setLayerProgress(jobsCompleted);
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
			activeDownloads--;
			jobsCompleted++;
		}
		updateGUI();
	}

	public void jobFinishedWithError(boolean retry) {
		synchronized (this) {
			activeDownloads--;
			if (retry)
				jobsRetryError++;
			else
				jobsPermanentError++;
		}
		Toolkit.getDefaultToolkit().beep();
		ap.setErrorCounter(jobsRetryError, jobsPermanentError);
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

		Point topLeft;
		Point bottomRight;
		int zoom;

		public DownloadJobProducer(Point topLeft, Point bottomRight, int zoom) {
			super("JobProducerThread_" + atlasName);
			this.bottomRight = bottomRight;
			this.topLeft = topLeft;
			this.zoom = zoom;
			start();
		}

		@Override
		public void run() {
			DownloadJobEnumerator djEnum = new DownloadJobEnumerator(topLeft.x, bottomRight.x,
					topLeft.y, bottomRight.y, zoom, tileSource, tileArchive, AtlasThread.this);
			try {
				while (djEnum.hasMoreElements()) {
					Job job = djEnum.nextElement();
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
