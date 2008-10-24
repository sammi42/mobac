package tac.program;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.gui.AtlasProgress;
import tac.program.JobDispatcher.Job;

public class AtlasThread extends Thread implements ActionListener {

	private static int threadNum = 0;
	private static Logger log = Logger.getLogger(AtlasThread.class);

	private JobDispatcher downloadJobDispatcher;
	private AtlasProgress ap;
	private MapSelection mapSelection;
	private TileSource tileSource;
	private String atlasName;
	private SelectedZoomLevels sZL;
	private int tileSizeWidth = 0;
	private int tileSizeHeight = 0;

	private int jobsProduced = 0;
	private int jobsCompleted = 0;
	private int jobsError = 0;

	public AtlasThread(String atlasName, TileSource tileSource, MapSelection mapSelection,
			SelectedZoomLevels sZL, int tileSizeWidth, int tileSizeHeight) {
		super("AtlasThread " + getNextThreadNum());
		this.tileSource = tileSource;
		this.atlasName = atlasName;
		this.mapSelection = mapSelection;
		this.sZL = sZL;
		this.tileSizeWidth = tileSizeWidth;
		this.tileSizeHeight = tileSizeHeight;
	}

	private static synchronized int getNextThreadNum() {
		threadNum++;
		return threadNum;
	}

	public void run() {
		log.info("Starting altas creation");
		log.trace("Atlas to doanload:\n\t" + "MapSource: " + tileSource + "\n\tAtlas name: "
				+ atlasName + "\n\tMap selection: " + mapSelection + "\n\tSelectedZoomLevels: "
				+ sZL + "\n\tTile size width: " + tileSizeWidth + "\n\tTile size height: "
				+ tileSizeHeight);
		ap = new AtlasProgress();
		ap.setAbortListener(this);
		try {
			createAtlas();
			ap.atlasCreationFinished();
			log.info("Altas creation finished");
		} catch (InterruptedException e) {
			JOptionPane.showMessageDialog(null, "Atlas download aborted", "Information",
					JOptionPane.INFORMATION_MESSAGE);
			ap.setAbortListener(null);
			ap.closeWindow();
			ap = null;
			log.info("Altas creation was interrupted by user");
		} catch (Exception e) {
			log.error("Altas creation aborted because of an error: ", e);
		}
	}

	protected void createAtlas() throws InterruptedException {

		String workingDir = System.getProperty("user.dir");

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String formattedDateString = sdf.format(date);

		File oziDir = new File(workingDir + "/ozi/" + formattedDateString);

		oziDir.mkdirs();

		/***
		 * In this section of code below, atlas is created.
		 **/

		File atlasDir = new File(workingDir + "/atlases/" + formattedDateString);
		atlasDir.mkdirs();

		File atlasTarDir = new File(workingDir + "/atlasestared/" + formattedDateString);
		atlasTarDir.mkdirs();

		File crtba = new File(atlasDir.getAbsolutePath(), "cr.tba");

		try {
			FileWriter fw = new FileWriter(crtba);
			fw.write("Atlas 1.0\r\n");
			fw.close();
		} catch (IOException e) {
			log.error("", e);
		}

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
		ap.setVisible(true);

		TileStore ts = TileStore.getInstance();
		Settings s = Settings.getInstance();

		Thread t = Thread.currentThread();
		downloadJobDispatcher = new JobDispatcher(6);
		try {
			for (int layer = 0; layer < nrOfLayers; layer++) {
				jobsCompleted = 0;
				jobsError = 0;
				if (t.isInterrupted())
					throw new InterruptedException();

				// Prepare the tile store directory
				if (s.isTileStoreEnabled())
					ts.getTileFile(0, 0, 0, tileSource).getParentFile().mkdirs();

				/***
				 * In this section of code below, tiles for Atlas is being
				 * downloaded and put into folder "ozi"
				 **/
				int zoom = zoomLevels[layer];

				Point topLeft = mapSelection.getTopLeftTileNumber(zoom);
				Point bottomRight = mapSelection.getBottomRightTileNumber(zoom);

				int apMax = (int) mapSelection.calculateNrOfTiles(zoom);

				int xMin = topLeft.x;
				int xMax = bottomRight.x;
				int yMin = topLeft.y;
				int yMax = bottomRight.y;
				ap.setMinMaxForCurrentLayer(0, apMax);
				ap.setZoomLevel(zoom);
				ap.setInitiateTimeForLayer();

				File oziZoomDir = new File(oziDir, Integer.toString(zoom));
				oziZoomDir.mkdir();
				jobsProduced = 0;
				DownloadJobProducer djp = new DownloadJobProducer(topLeft, bottomRight, zoom,
						oziZoomDir);

				while (djp.isAlive() || jobsCompleted < jobsProduced) {
					Thread.sleep(500);
					if (jobsError > 10) {
						downloadJobDispatcher.cancelOutstandingJobs();
						downloadJobDispatcher.terminateAllWorkerThreads();
						djp.cancel();
						JOptionPane
								.showMessageDialog(
										null,
										"Something is wrong with connection to download server. Please check connection to internet and try again",
										"Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				log.debug("All download jobs has been completed!");

				if ((oziZoomDir.list().length) != (mapSelection.calculateNrOfTiles(zoom))) {
					JOptionPane.showMessageDialog(null,
							"Something is wrong with download of atlas tiles. "
									+ "Actual amount of downoladed tiles is not the same as "
									+ "the supposed amount of tiles downloaded.\n"
									+ "It might be connection problems to internet "
									+ "or something else. Please try again.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				File atlasFolder = new File(atlasDir, String.format("%s-%02d", new Object[] {
						atlasName, zoom }));
				atlasFolder.mkdir();

				OziToAtlas ota = new OziToAtlas(oziZoomDir, atlasFolder, tileSizeWidth,
						tileSizeHeight, atlasName, zoom);
				ota.convert(xMax, xMin, yMax, yMin);

				ap.updateAtlasProgressBarLayerText(layer + 1);
				downloadJobDispatcher.cancelOutstandingJobs();
			}
		} finally {
			downloadJobDispatcher.terminateAllWorkerThreads();
		}

		ap.atlasCreationFinished();

		AtlasTarCreator atc = new AtlasTarCreator(atlasDir, atlasTarDir);
		atc.createAtlasCrTarArchive();
		ap.updateTarPrograssBar();

		atc.createMapTars();
		ap.updateTarPrograssBar();

		JOptionPane.showMessageDialog(null, "Atlas download completed", "Information",
				JOptionPane.INFORMATION_MESSAGE);
	}

	protected boolean retryDownloadAtlasTile(int xValue, int yValue, int zoomValue,
			File destinationFolder, TileSource tileSource) throws InterruptedException {

		for (int i = 0; i < 10; i++) {
			try {
				int bytes = TileDownLoader.getImage(xValue, yValue, zoomValue, destinationFolder,
						tileSource, true);
				ap.addDownloadedBytes(bytes);
				return true;
			} catch (IOException e) {
				Thread.sleep(1000);
			}
		}
		return false;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			this.interrupt();
			if (downloadJobDispatcher != null)
				downloadJobDispatcher.terminateAllWorkerThreads();
		} catch (Exception ex) {
		}
	}

	private void updateGUI() {
		ap.updateAtlasProgressBar(ap.getAtlasProgressValue() + 1);
		ap.updateLayerProgressBar(jobsCompleted);
		ap.updateViewNrOfDownloadedBytes();
		ap.updateViewNrOfDownloadedBytesPerSecond();
		ap.updateTotalDownloadTime();
	}

	protected synchronized void jobFinishedSuccessfully() {
		jobsCompleted++;
		updateGUI();
	}

	protected synchronized void jobFinishedWithError() {
		jobsError++;
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
		File oziZoomDir;

		public DownloadJobProducer(Point topLeft, Point bottomRight, int zoom, File oziZoomDir) {
			super();
			this.bottomRight = bottomRight;
			this.topLeft = topLeft;
			this.zoom = zoom;
			this.oziZoomDir = oziZoomDir;
			start();
		}

		@Override
		public void run() {

			int xMin = topLeft.x;
			int xMax = bottomRight.x;
			int yMin = topLeft.y;
			int yMax = bottomRight.y;
			try {
				for (int y = yMin; y <= yMax; y++) {
					for (int x = xMin; x <= xMax; x++) {
						DownloadJob job = new DownloadJob(oziZoomDir, tileSource, x, y, zoom);
						downloadJobDispatcher.addJob(job);
						log.trace("Job added: " + x + " " + y + " " + zoom);
						jobsProduced++;
					}
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

	public class DownloadJob implements Job {

		int errorCounter = 0;

		TileSource tileSource;
		int xValue;
		int yValue;
		int zoomValue;
		File destinationFolder;

		public DownloadJob(File destinationFolder, TileSource tileSource, int xValue, int yValue,
				int zoomValue) {
			this.destinationFolder = destinationFolder;
			this.tileSource = tileSource;
			this.xValue = xValue;
			this.yValue = yValue;
			this.zoomValue = zoomValue;
		}

		public void run() throws Exception {
			try {
				//Thread.sleep(500);
				int bytes = TileDownLoader.getImage(xValue, yValue, zoomValue, destinationFolder,
						tileSource, true);
				ap.addDownloadedBytes(bytes);
				jobFinishedSuccessfully();
			} catch (Exception e) {
				errorCounter++;
				jobFinishedWithError();
				// Reschedule job to try it later again
				if (errorCounter < 3)
					downloadJobDispatcher.addErrorJob(this);
				throw e;
			}
		}
	}
}
