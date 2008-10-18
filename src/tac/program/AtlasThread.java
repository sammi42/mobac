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

public class AtlasThread extends Thread implements ActionListener {

	private Logger log = Logger.getLogger(AtlasThread.class);

	private AtlasProgress ap;
	private MapSelection mapSelection;
	private TileSource tileSource;
	private String atlasName;
	private SelectedZoomLevels sZL;
	private int tileSizeWidth = 0;
	private int tileSizeHeight = 0;

	public AtlasThread(String atlasName, TileSource tileSource, MapSelection mapSelection,
			SelectedZoomLevels sZL, int tileSizeWidth, int tileSizeHeight) {
		super();
		this.tileSource = tileSource;
		this.atlasName = atlasName;
		this.mapSelection = mapSelection;
		this.sZL = sZL;
		this.tileSizeWidth = tileSizeWidth;
		this.tileSizeHeight = tileSizeHeight;
	}

	public void run() {
		ap = new AtlasProgress();
		ap.setAbortListener(this);
		try {
			createAtlas();
			ap.atlasCreationFinished();
		} catch (InterruptedException e) {
			JOptionPane.showMessageDialog(null, "Atlas download aborted", "Information",
					JOptionPane.INFORMATION_MESSAGE);
			ap.setAbortListener(null);
			ap.closeWindow();
			ap = null;
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
		for (int layer = 0; layer < nrOfLayers; layer++) {

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

			// System.out.println("Selection to download: \n\t" + topLeft +
			// "\n\t" + bottomRight
			// + "\n\tzoom: " + zoom);
			int apMax = (int) mapSelection.calculateNrOfTiles(zoom);

			int xMin = topLeft.x;
			int xMax = bottomRight.x;
			int yMin = topLeft.y;
			int yMax = bottomRight.y;
			ap.setMinMaxForCurrentLayer(0, apMax);
			ap.setZoomLevel(zoom);
			ap.setInitiateTimeForLayer();

			int counter = 0;

			File oziZoomDir = new File(oziDir, Integer.toString(zoom));
			oziZoomDir.mkdir();
			for (int y = yMin; y <= yMax; y++) {
				for (int x = xMin; x <= xMax; x++) {
					if (t.isInterrupted())
						throw new InterruptedException();
					try {
						int bytes = TileDownLoader.getImage(x, y, zoom, oziZoomDir, tileSource,
								true);
						ap.addDownloadedBytes(bytes);
					} catch (IOException e) {

						boolean retryOK;

						retryOK = retryDownloadAtlasTile(x, y, zoom, oziZoomDir, tileSource);

						if (retryOK == false) {
							JOptionPane
									.showMessageDialog(
											null,
											"Something is wrong with connection to download server. Please check connection to internet and try again",
											"Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					counter++;

					ap.updateAtlasProgressBar(ap.getAtlasProgressValue() + 1);
					ap.updateLayerProgressBar(counter);
					ap.updateViewNrOfDownloadedBytes();
					ap.updateViewNrOfDownloadedBytesPerSecond();
					ap.updateTotalDownloadTime();
				}
			}

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

			OziToAtlas ota = new OziToAtlas(oziZoomDir, atlasFolder, tileSizeWidth, tileSizeHeight,
					atlasName, zoom);
			ota.convert(xMax, xMin, yMax, yMin);

			ap.updateAtlasProgressBarLayerText(layer + 1);

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

		boolean retryOk = false;

		for (int i = 0; i < 10; i++) {

			try {
				int bytes = TileDownLoader.getImage(xValue, yValue, zoomValue, destinationFolder,
						tileSource, true);
				ap.addDownloadedBytes(bytes);
				retryOk = true;
			} catch (IOException e) {
				retryOk = false;

				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException iex) {
				}
			}
		}
		return retryOk;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			this.interrupt();
		} catch (Exception ex) {
		}
	}

}
