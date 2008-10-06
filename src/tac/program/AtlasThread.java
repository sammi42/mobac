package tac.program;

import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import tac.gui.AtlasProgress;
import tac.gui.GUI;
import tac.utilities.Utilities;

public class AtlasThread extends Thread {

	private GUI gui;
	private AtlasProgress ap;
	private MapSelection mapSelection;
	private String atlasName;
	private SelectedZoomLevels sZL;
	private int tileSizeWidth = 0;
	private int tileSizeHeight = 0;

	public AtlasThread(GUI gui, String atlasName, MapSelection mapSelection,
			SelectedZoomLevels sZL, int tileSizeWidth, int tileSizeHeight) {
		super();
		this.gui = gui;
		this.atlasName = atlasName;
		this.mapSelection = mapSelection;
		this.sZL = sZL;
		this.tileSizeWidth = tileSizeWidth;
		this.tileSizeHeight = tileSizeHeight;
	}

	public void run() {

		String workingDir = System.getProperty("user.dir");

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String formattedDateString = sdf.format(date);

		File ozi =
				new File(workingDir + File.separator + "ozi" + File.separator + formattedDateString);

		// Check whether preview folder exists
		// or not...
		if (!ozi.exists()) {
			ozi.mkdir();
		}

		/***
		 * In this section of code below, atlas is created.
		 **/

		File atlas =
				new File(workingDir + File.separator + "atlases" + File.separator
						+ formattedDateString);

		// Check whether preview folder exists
		// or not...
		if (!atlas.exists()) {
			// Create if not already existing
			atlas.mkdir();
		}

		File atlasTar =
				new File(workingDir + File.separator + "atlasestared" + File.separator
						+ formattedDateString);

		// Check whether preview folder exists
		// or not...
		if (!atlasTar.exists()) {
			// Create if not already existing
			atlasTar.mkdir();
		}

		File crtba = new File(atlas.getAbsolutePath() + File.separator + "cr.tba");

		try {
			FileWriter fw = new FileWriter(crtba);
			fw.write("Atlas 1.0\r\n");
			fw.close();
		} catch (IOException iox) {
			System.out.println(iox);
		}

		int nrOfLayers = sZL.getNrOfLayers();
		int[] zoomLevels = sZL.getZoomLevels();

		ProcessValues.resetNrOfDownloadedBytes();

		int totalNrOfTiles = 0;

		for (int i = 0; i < nrOfLayers; i++) {

			Point topLeft =
					GoogleTileUtils.toTileXY(mapSelection.lat_max, mapSelection.lon_max,
							zoomLevels[i]);
			Point bottomRight =
					GoogleTileUtils.toTileXY(mapSelection.lat_min, mapSelection.lon_min,
							zoomLevels[i]);

			totalNrOfTiles =
					totalNrOfTiles
							+ Utilities.calculateNrOfTiles(new TileXYMinMaxAndZoom(topLeft,
									bottomRight, zoomLevels[i]));
		}

		ap = AtlasProgress.getInstance();
		ap.init(totalNrOfTiles, nrOfLayers);
		ap.setVisible(true);

		ProcessValues.setTileSizeErrorNotified(false);

		tileDownloadsLoop: for (int layer = 0; layer < nrOfLayers; layer++) {

			if (ProcessValues.getAbortAtlasDownload()) {
				break tileDownloadsLoop;
			} else {
				/***
				 * In this section of code below, tiles for Atlas is being
				 * downloaded and put into folder "ozi"
				 **/
				int zoom = zoomLevels[layer];

				Point topLeft =
						GoogleTileUtils.toTileXY(mapSelection.lat_max, mapSelection.lon_max, zoom);
				Point bottomRight =
						GoogleTileUtils.toTileXY(mapSelection.lat_min, mapSelection.lon_min, zoom);
				int apMax =
						Utilities.calculateNrOfTiles(new TileXYMinMaxAndZoom(topLeft, bottomRight,
								zoom));

				ap.setMinMaxForCurrentLayer(0, apMax);
				ap.setZoomLevel(zoom);
				ap.setInitiateTimeForLayer();

				int xMax = (int) topLeft.getX();
				int xMin = (int) bottomRight.getX();
				int yMax = (int) bottomRight.getY();
				int yMin = (int) topLeft.getY();

				int serverSwitcher = 0;
				int counter = 0;

				File oziZoom = new File(ozi + File.separator + zoom);
				oziZoom.mkdir();

				for (int i = yMin; i <= yMax; i++) {

					if (ProcessValues.getAbortAtlasDownload()) {
						break tileDownloadsLoop;
					} else {

						for (int j = xMin; j <= xMax; j++) {

							if (ProcessValues.getAbortAtlasDownload()) {
								break tileDownloadsLoop;
							} else {

								if (serverSwitcher == 4) {
									serverSwitcher = 0;
								}

								try {

									GoogleTileDownLoad.getImage(j, i, zoom, oziZoom,
											serverSwitcher, true);
								} catch (IOException e) {

									boolean retryOK;

									retryOK =
											retryDownloadAtlasTile(j, i, zoom, oziZoom,
													serverSwitcher);

									if (retryOK == false) {
										JOptionPane
												.showMessageDialog(
														null,
														"Something is wrong with connection to download server. Please check connection to internet and try again",
														"Error", JOptionPane.ERROR_MESSAGE);
										System.exit(1);
									}
								}
								serverSwitcher++;
								counter++;

								ap.updateAtlasProgressBar(ap.getAtlasProgressValue() + 1);
								ap.updateLayerProgressBar(counter);
								ap.updateViewNrOfDownloadedBytes();
								ap.updateViewNrOfDownloadedBytesPerSecond();
								ap.updateTotalDownloadTime();

							}
						}
					}
				}

				if ((oziZoom.list().length) != ((yMax - yMin + 1) * (xMax - xMin + 1))) {
					JOptionPane
							.showMessageDialog(
									null,
									"Something is wrong with download of atlas tiles. Actual amount of downoladed tiles is not the same as the supposed amount of tiles downoladed. It might be connection problems to internet or something else. Please try again.",
									"Error", JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}

				File atlasFolder =
						new File(atlas.getAbsolutePath() + File.separator + atlasName + zoom);
				atlasFolder.mkdir();

				OziToAtlas ota =
						new OziToAtlas(oziZoom, atlasFolder, tileSizeWidth, tileSizeHeight,
								atlasName, zoom);
				ota.convert(xMax, xMin, yMax, yMin);

				ap.updateAtlasProgressBarLayerText(layer + 1);
			}
		}

		if (ProcessValues.getAbortAtlasDownload()) {
			JOptionPane.showMessageDialog(null, "Atlas download aborted", "Information",
					JOptionPane.INFORMATION_MESSAGE);
			ap.setButtonText();
		} else {
			ap.setButtonText();

			Utilities.createCR_TAR(atlas, atlasTar, new File(workingDir + File.separator
					+ "tarwrkdir"));
			AtlasProgress ap = AtlasProgress.getInstance();
			ap.updateTarPrograssBar();
			Utilities.createTarPackedLayers(atlas, atlasTar);
			ap.updateTarPrograssBar();

			JOptionPane.showMessageDialog(null, "Atlas download completed", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
		gui.setCreateAtlasButtonEnabled(true);
		ProcessValues.setAbortAtlasDownload(false);
	}

	protected boolean retryDownloadAtlasTile(int xValue, int yValue, int zoomValue,
			File destinationFolder, int serverSwitcher) {

		boolean retryOk = false;

		for (int i = 0; i < 10; i++) {

			if (serverSwitcher == 4) {
				serverSwitcher = 0;
			}
			try {
				GoogleTileDownLoad.getImage(xValue, yValue, zoomValue, destinationFolder,
						serverSwitcher, true);
				retryOk = true;
			} catch (IOException e) {
				retryOk = false;

				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException iex) {
				}
			}
			serverSwitcher++;
		}
		return retryOk;
	}

}
