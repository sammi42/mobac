package mobac.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import mobac.program.AtlasThread;
import mobac.program.Logging;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.Settings;
import mobac.utilities.GBC;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.OSUtilities;
import mobac.utilities.Utilities;

import org.apache.log4j.Logger;

/**
 * A window showing the progress while {@link AtlasThread} downloads and
 * processes the map tiles.
 * 
 */
public class AtlasProgress extends JFrame implements ActionListener {

	private static Logger log = Logger.getLogger(AtlasProgress.class);

	private static final long serialVersionUID = -1L;

	private static final Timer TIMER = new Timer(true);

	private JProgressBar atlasProgressBar;
	private JProgressBar mapDownloadProgressBar;
	private JProgressBar mapCreationProgressBar;

	private Container background;

	private long initialTotalTime;
	private long initialMapDownloadTime;

	private static class Data {
		AtlasInterface atlasInterface;
		MapInterface map;
		MapInfo mapInfo;
		long numberOfDownloadedBytes = 0;
		int totalNumberOfTiles = 0;
		int totalNumberOfMaps = 0;
		int totalProgress = 0;
		int totalProgressPercent = -1;
		int currentMapNumber = 0;
		int mapDownloadProgress = 0;
		int mapDownloadNumberOfTiles = 0;
		int mapCreationProgress = 0;
		int mapCreationMax = 0;
		int mapRetryErrors = 0;
		int mapPermanentErrors = 0;
		int prevMapsRetryErrors = 0;
		int prevMapsPermanentErrors = 0;
		boolean paused = false;
	}

	private final Data data = new Data();

	private boolean aborted = false;

	private JLabel windowTitle;

	private JLabel title;
	private JLabel mapInfoLabel;
	private JLabel mapDownloadTitle;
	private JLabel atlasPercent;
	private JLabel mapDownloadPercent;
	private JLabel atlasMapsDone;
	private JLabel mapDownloadElementsDone;
	private JLabel atlasTimeLeft;
	private JLabel mapDownloadTimeLeft;
	private JLabel mapCreation;
	private JLabel nrOfDownloadedBytes;
	private JLabel nrOfDownloadedBytesValue;
	private JLabel nrOfDownloadedBytesPerSecond;
	private JLabel nrOfDownloadedBytesPerSecondValue;
	private JLabel activeDownloads;
	private JLabel activeDownloadsValue;
	private JLabel downloadErrors;
	private JLabel downloadErrorsValue;
	private JLabel totalDownloadTime;
	private JLabel totalDownloadTimeValue;

	private JCheckBox ignoreDlErrors;
	private JButton dismissWindowButton;
	private JButton openProgramFolderButton;
	private JButton abortAtlasCreationButton;
	private JButton pauseResumeDownloadButton;

	private AtlasCreationController downloadControlListener = null;

	private UpdateTask updateTask = null;
	private GUIUpdater guiUpdater = null;

	private AtlasThread atlasThread;

	private ArrayList<MapInfo> mapInfos = null;

	private static String TEXT_MAP_DOWNLOAD = "Downloading tiles for map number ";
	private static String TEXT_PERCENT = "Percent done: %d%%";

	public AtlasProgress(AtlasThread atlasThread) {
		super("Atlas creation in progress");
		this.atlasThread = atlasThread;
		ToolTipManager.sharedInstance().setDismissDelay(12000);
		setIconImages(MainGUI.MOBAC_ICONS);
		setLayout(new GridBagLayout());
		updateTask = new UpdateTask();
		guiUpdater = new GUIUpdater();

		createComponents();

		// Initialize the layout in respect to the layout (font size ...)
		pack();

		// The layout is now initialized - we disable it because we don't want
		// want to the labels to jump around if the content changes.
		background.setLayout(null);
		setResizable(false);

		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dContent = getSize();
		setLocation((dScreen.width - dContent.width) / 2, (dScreen.height - dContent.height) / 2);

		initialTotalTime = System.currentTimeMillis();
		initialMapDownloadTime = System.currentTimeMillis();

		addWindowListener(new CloseListener());
	}

	private void createComponents() {
		background = new JPanel(new GridBagLayout());

		windowTitle = new JLabel("<html><h3>ATLAS CREATION IN PROGRESS...</h3></html>");

		title = new JLabel("Processing maps of atlas:");

		mapInfoLabel = new JLabel("Processing map: ");

		atlasMapsDone = new JLabel("000 of 000 done");
		atlasPercent = new JLabel(String.format(TEXT_PERCENT, 100));
		atlasTimeLeft = new JLabel("Time remaining: 00000 minutes 00 seconds", JLabel.RIGHT);
		atlasProgressBar = new JProgressBar();

		mapDownloadTitle = new JLabel(TEXT_MAP_DOWNLOAD + "000");
		mapDownloadElementsDone = new JLabel("1000000 of 1000000 tiles done");
		mapDownloadPercent = new JLabel(String.format(TEXT_PERCENT, 100));
		mapDownloadTimeLeft = new JLabel("Time remaining: 00000 minutes 00 seconds", JLabel.RIGHT);
		mapDownloadProgressBar = new JProgressBar();

		mapCreation = new JLabel("Map Creation");
		mapCreationProgressBar = new JProgressBar();

		nrOfDownloadedBytesPerSecond = new JLabel("Average download speed");
		nrOfDownloadedBytesPerSecondValue = new JLabel();
		nrOfDownloadedBytes = new JLabel("Total download size");
		nrOfDownloadedBytesValue = new JLabel();
		activeDownloads = new JLabel("Active tile fetcher threads");
		activeDownloadsValue = new JLabel();
		downloadErrors = new JLabel("Download errors");
		downloadErrors
				.setToolTipText("<html><h4>Download errors for the current map and for the total atlas (retryable/permanent)</h4>"
						+ "<p>Mobile Atlas Creator tries to retry failed tile downloads up to two times.<br>"
						+ "The first time a tile download fails the <b>retryable</b> counter increases by one.<br>"
						+ "If the tile downloads fails the second time the tile will be counted as <br>"
						+ "<b>permanent</b> error and not tried again.<br></p></html>");
		downloadErrorsValue = new JLabel();
		downloadErrorsValue.setToolTipText(downloadErrors.getToolTipText());
		totalDownloadTime = new JLabel("Total creation time");
		totalDownloadTimeValue = new JLabel();

		ignoreDlErrors = new JCheckBox("Ignore download errors and continue automatically");
		abortAtlasCreationButton = new JButton("Abort creation");
		abortAtlasCreationButton.setToolTipText("Abort current Atlas download");
		dismissWindowButton = new JButton("Close Window");
		dismissWindowButton.setToolTipText("Atlas creation in progress...");
		dismissWindowButton.setVisible(false);
		openProgramFolderButton = new JButton("Open Atlas Folder");
		openProgramFolderButton.setToolTipText("Atlas creation in progress...");
		openProgramFolderButton.setEnabled(false);
		pauseResumeDownloadButton = new JButton("Pause/Resume");

		GBC gbcRIF = GBC.std().insets(0, 0, 20, 0).fill(GBC.HORIZONTAL);
		GBC gbcEol = GBC.eol();
		GBC gbcEolFill = GBC.eol().fill(GBC.HORIZONTAL);
		GBC gbcEolFillI = GBC.eol().fill(GBC.HORIZONTAL).insets(0, 5, 0, 0);

		// background.add(windowTitle, gbcEolFill);
		// background.add(Box.createVerticalStrut(10), gbcEol);

		background.add(mapInfoLabel, gbcEolFill);
		background.add(Box.createVerticalStrut(20), gbcEol);

		background.add(title, gbcRIF);
		background.add(atlasMapsDone, gbcRIF);
		background.add(atlasPercent, gbcRIF);
		background.add(atlasTimeLeft, gbcEolFill);
		background.add(atlasProgressBar, gbcEolFillI);
		background.add(Box.createVerticalStrut(20), gbcEol);

		background.add(mapDownloadTitle, gbcRIF);
		background.add(mapDownloadElementsDone, gbcRIF);
		background.add(mapDownloadPercent, gbcRIF);
		background.add(mapDownloadTimeLeft, gbcEolFill);
		background.add(mapDownloadProgressBar, gbcEolFillI);
		background.add(Box.createVerticalStrut(20), gbcEol);

		background.add(mapCreation, gbcEol);
		background.add(mapCreationProgressBar, gbcEolFillI);
		background.add(Box.createVerticalStrut(10), gbcEol);

		JPanel infoPanel = new JPanel(new GridBagLayout());
		GBC gbci = GBC.std().insets(0, 3, 3, 3);
		infoPanel.add(nrOfDownloadedBytes, gbci);
		infoPanel.add(nrOfDownloadedBytesValue, gbci.toggleEol());
		infoPanel.add(nrOfDownloadedBytesPerSecond, gbci.toggleEol());
		infoPanel.add(nrOfDownloadedBytesPerSecondValue, gbci.toggleEol());
		infoPanel.add(activeDownloads, gbci.toggleEol());
		infoPanel.add(activeDownloadsValue, gbci.toggleEol());
		infoPanel.add(downloadErrors, gbci.toggleEol());
		infoPanel.add(downloadErrorsValue, gbci.toggleEol());
		infoPanel.add(totalDownloadTime, gbci.toggleEol());
		infoPanel.add(totalDownloadTimeValue, gbci.toggleEol());

		JPanel bottomPanel = new JPanel(new GridBagLayout());
		bottomPanel.add(infoPanel, GBC.std().gridheight(2));
		bottomPanel.add(ignoreDlErrors, GBC.eol().anchor(GBC.EAST));

		GBC gbcRight = GBC.std().anchor(GBC.SOUTHEAST).insets(5, 0, 0, 0);
		bottomPanel.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		bottomPanel.add(abortAtlasCreationButton, gbcRight);
		bottomPanel.add(dismissWindowButton, gbcRight);
		bottomPanel.add(pauseResumeDownloadButton, gbcRight);
		bottomPanel.add(openProgramFolderButton, gbcRight);

		background.add(bottomPanel, gbcEolFillI);

		JPanel borderPanel = new JPanel(new GridBagLayout());
		borderPanel.add(background, GBC.std().insets(10, 10, 10, 10).fill());

		add(borderPanel, GBC.std().fill());

		abortAtlasCreationButton.addActionListener(this);
		dismissWindowButton.addActionListener(this);
		openProgramFolderButton.addActionListener(this);
		pauseResumeDownloadButton.addActionListener(this);
	}

	public void initAtlas(AtlasInterface atlasInterface) {
		data.atlasInterface = atlasInterface;
		data.totalNumberOfTiles = atlasInterface.calculateTilesToDownload() * 2;
		int mapCount = 0;
		int tileCount = 0;
		mapInfos = new ArrayList<MapInfo>(100);
		for (LayerInterface layer : atlasInterface) {
			mapCount += layer.getMapCount();
			for (MapInterface map : layer) {
				int before = tileCount;
				int mapTiles = map.calculateTilesToDownload();
				tileCount += mapTiles + mapTiles;
				mapInfos.add(new MapInfo(map, before, tileCount));
			}
		}
		mapInfos.trimToSize();
		data.totalNumberOfMaps = mapCount;

		initialTotalTime = System.currentTimeMillis();
		initialMapDownloadTime = -1;
		updateGUI();
		setVisible(true);
		TIMER.schedule(updateTask, 0, 500);
	}

	public void initMapDownload(MapInterface map) {
		int index = mapInfos.indexOf(new MapInfo(map, 0, 0));
		data.mapInfo = mapInfos.get(index);
		data.totalProgress = data.mapInfo.tileCountOnStart;
		data.map = map;
		data.mapDownloadNumberOfTiles = map.calculateTilesToDownload();
		initialMapDownloadTime = System.currentTimeMillis();
		data.prevMapsPermanentErrors += data.mapPermanentErrors;
		data.prevMapsRetryErrors += data.mapRetryErrors;
		data.mapCreationProgress = 0;
		data.mapDownloadProgress = 0;
		data.currentMapNumber = index + 1;
		updateGUI();
	}

	public void initMapCreation(int maxTilesToProcess) {
		data.mapCreationProgress = 0;
		data.mapCreationMax = maxTilesToProcess;
		initialMapDownloadTime = -1;
		updateGUI();
	}

	public void setErrorCounter(int retryErrors, int permanentErrors) {
		data.mapRetryErrors = retryErrors;
		data.mapPermanentErrors = permanentErrors;
		updateGUI();
	}

	public void incMapDownloadProgress() {
		data.mapDownloadProgress++;
		data.totalProgress++;
		updateGUI();
	}

	public void incMapCreationProgress() {
		setMapCreationProgress(data.mapCreationProgress + 1);
	}

	public void incMapCreationProgress(int stepSize) {
		setMapCreationProgress(data.mapCreationProgress + stepSize);
	}

	public void setMapCreationProgress(int progress) {
		data.mapCreationProgress = progress;
		data.totalProgress = data.mapInfo.tileCountOnStart + data.mapInfo.mapTiles
				+ (data.mapInfo.mapTiles * data.mapCreationProgress / data.mapCreationMax);
		updateGUI();
	}

	public void addDownloadedBytes(int bytes) {
		data.numberOfDownloadedBytes += bytes;
	}

	public boolean ignoreDownloadErrors() {
		return ignoreDlErrors.isSelected();
	}

	private String formatRemainingTime(long seconds) {
		int minutesLeft = 0;
		String timeLeftString;

		if (seconds < 0) {
			timeLeftString = "unknown";
		} else if (seconds > 60) {
			minutesLeft = (int) (seconds / 60);
			int secondsLeft = (int) (seconds % 60);
			if (secondsLeft > 119) {
				timeLeftString = Integer.toString(minutesLeft) + " " + "minutes" + " "
						+ Integer.toString(secondsLeft) + " " + "seconds";
			} else {
				timeLeftString = Integer.toString(minutesLeft) + " " + "minute" + " "
						+ Integer.toString(secondsLeft) + " " + "seconds";
			}
		} else {
			if (seconds > 1) {
				timeLeftString = Long.toString(seconds) + " " + "seconds";
			} else {
				timeLeftString = Long.toString(seconds) + " " + "second";
			}
		}
		return "Time remaining: " + timeLeftString;
	}

	public void setZoomLevel(int theZoomLevel) {
		mapDownloadTitle.setText(TEXT_MAP_DOWNLOAD + Integer.toString(theZoomLevel));
	}

	public void atlasCreationFinished() {
		stopUpdateTask();
		downloadControlListener = null;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				abortAtlasCreationButton.setEnabled(false);

				if (aborted) {
					windowTitle.setText("<html><h2>ATLAS CREATION HAS BEEN "
							+ "ABORTED BY USER</h2></html>");
					setTitle("Atlas creation aborted");
				} else {
					windowTitle.setText("<html><h2>ATLAS CREATION FINISHED "
							+ "SUCCESSFULLY</h2></html>");
					setTitle("Atlas creation finished successfully");
				}
				mapInfoLabel.setText("");

				abortAtlasCreationButton.setVisible(false);

				dismissWindowButton.setToolTipText("Close atlas creation progress window");
				dismissWindowButton.setVisible(true);

				if (!aborted) {
					openProgramFolderButton.setToolTipText("Open folder where atlas output folder");
					openProgramFolderButton.setEnabled(true);
				}
			}
		});
	}

	private synchronized void stopUpdateTask() {
		try {
			updateTask.cancel();
			updateTask = null;
		} catch (Exception e) {
		}
	}

	public void closeWindow() {
		try {
			stopUpdateTask();
			downloadControlListener = null;
			setVisible(false);
		} finally {
			dispose();
		}
	}

	public AtlasCreationController getDownloadControlListener() {
		return downloadControlListener;
	}

	public void setDownloadControlerListener(AtlasCreationController threadControlListener) {
		this.downloadControlListener = threadControlListener;
	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		File atlasFolder = Settings.getInstance().getAtlasOutputDirectory();
		if (openProgramFolderButton.equals(source)) {
			try {
				OSUtilities.openFolderBrowser(atlasFolder);
			} catch (Exception e) {
				log.error("", e);
			}
		} else if (dismissWindowButton.equals(source)) {
			downloadControlListener = null;
			closeWindow();
		} else if (abortAtlasCreationButton.equals(source)) {
			aborted = true;
			updateTask.cancel();
			if (downloadControlListener != null) {
				downloadControlListener.abortAtlasCreation();
			} else
				dispose();
		} else if (pauseResumeDownloadButton.equals(source)) {
			if (downloadControlListener != null)
				downloadControlListener.pauseResumeAtlasCreation();
		}
	}

	public void updateGUI() {
		guiUpdater.updateAsynchronously();
	}

	private class GUIUpdater implements Runnable {

		int scheduledCounter = 0;

		public void updateAsynchronously() {
			// If there is still at least one scheduled update request to be
			// executed we don't have add another one as this can result in an
			// to overloaded swing invocation queue.
			synchronized (this) {
				if (scheduledCounter > 0)
					return;
				scheduledCounter++;
			}
			SwingUtilities.invokeLater(this);
		}

		public void run() {
			synchronized (this) {
				scheduledCounter--;
			}

			if (data.map != null) {
				String text = "<html>Processing map \"<b>" + data.map.getName()
						+ "</b>\" of layer <b>\"" + data.map.getLayer().getName()
						+ "\"</b> map source: " + data.map.getMapSource() + "</html>";
				mapInfoLabel.setText(text);
			}

			// atlas progress
			atlasProgressBar.setMaximum(data.totalNumberOfTiles);
			atlasProgressBar.setValue(data.totalProgress);

			int newPercent = (int) (atlasProgressBar.getPercentComplete() * 100);
			boolean pauseState = atlasThread.isPaused();
			if (data.totalProgressPercent != newPercent || pauseState != data.paused) {
				data.totalProgressPercent = newPercent;
				atlasPercent.setText(String.format(TEXT_PERCENT, data.totalProgressPercent));
				if (data.atlasInterface != null) {
					String text = String.format("%d%% - Processing atlas \"%s\"",
							data.totalProgressPercent, data.atlasInterface.getName());
					if (pauseState)
						text += " [PAUSED]";
					AtlasProgress.this.setTitle(text);
				}
			}
			data.paused = pauseState;

			long seconds = -1;
			int totalProgress = data.totalProgress;
			if (totalProgress != 0) {
				// Avoid for a possible division by zero
				int totalTilesRemaining = data.totalNumberOfTiles - totalProgress;
				long totalElapsedTime = System.currentTimeMillis() - initialTotalTime;
				seconds = (totalElapsedTime * totalTilesRemaining / (1000L * totalProgress));
			}
			atlasTimeLeft.setText(formatRemainingTime(seconds));

			// layer progress
			mapDownloadProgressBar.setMaximum(data.mapDownloadNumberOfTiles);
			mapDownloadProgressBar.setValue(data.mapDownloadProgress);

			mapDownloadPercent.setText(String.format(TEXT_PERCENT, (int) (mapDownloadProgressBar
					.getPercentComplete() * 100)));

			mapDownloadElementsDone.setText(Integer.toString(data.mapDownloadProgress) + " of "
					+ data.mapDownloadNumberOfTiles + " tiles done");

			seconds = -1;
			int mapDlProgress = data.mapDownloadProgress;
			if (mapDlProgress != 0 && initialMapDownloadTime > 0)
				seconds = ((System.currentTimeMillis() - initialMapDownloadTime)
						* (data.mapDownloadNumberOfTiles - mapDlProgress) / (1000L * mapDlProgress));
			mapDownloadTimeLeft.setText(formatRemainingTime(seconds));

			// map progress
			mapCreation.setText("Map creation");
			mapCreationProgressBar.setValue(data.mapCreationProgress);
			mapCreationProgressBar.setMaximum(data.mapCreationMax);
			atlasMapsDone
					.setText(data.currentMapNumber + " of " + data.totalNumberOfMaps + " done");

			// bytes per second
			long rate = data.numberOfDownloadedBytes * 1000;
			long time = System.currentTimeMillis() - initialMapDownloadTime;
			if (data.mapCreationProgress == 0 && initialMapDownloadTime > 0) {
				if (time == 0) {
					nrOfDownloadedBytesPerSecondValue.setText(": ?? KiByte / Second");
				} else {
					rate = rate / time;
					nrOfDownloadedBytesPerSecondValue.setText(": " + Utilities.formatBytes(rate)
							+ " / Second");
				}
			}

			// downloaded bytes
			nrOfDownloadedBytesValue.setText(": "
					+ Utilities.formatBytes(data.numberOfDownloadedBytes));

			// total creation time
			String timeString = "";

			seconds = 0;
			long minutes = 0;

			long totalMilliseconds = 0;

			totalMilliseconds = System.currentTimeMillis() - initialTotalTime;

			if (totalMilliseconds > 60000) {
				minutes = totalMilliseconds / 60000;
				seconds = (totalMilliseconds - (minutes * 60000)) / 1000;
				timeString = minutes + " minute(s) and " + seconds + " second(s)";
			} else {
				seconds = totalMilliseconds / 1000;
				timeString = seconds + " second(s)";
			}
			totalDownloadTimeValue.setText(": " + timeString);
			totalDownloadTimeValue.repaint();

			// active downloads
			int activeDownloads = (atlasThread == null) ? 0 : atlasThread.getActiveDownloads();
			activeDownloadsValue.setText(": " + activeDownloads);
			activeDownloadsValue.repaint();

			int totalPermanentErrors = data.prevMapsPermanentErrors + data.mapPermanentErrors;
			int totalRetylableErrors = data.prevMapsRetryErrors + data.mapRetryErrors;
			downloadErrorsValue.setText(": map: " + data.mapRetryErrors + " / "
					+ data.mapPermanentErrors + " total: " + totalRetylableErrors + " / "
					+ totalPermanentErrors);
			downloadErrorsValue.repaint();
		}
	}

	private class UpdateTask extends TimerTask {

		@Override
		public void run() {
			updateGUI();
		}
	}

	private class CloseListener extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			log.debug("Closing event detected for atlas progress window");
			AtlasCreationController listener = AtlasProgress.this.downloadControlListener;
			if (listener != null)
				listener.abortAtlasCreation();
		}

	}

	protected static class MapInfo {

		final MapInterface map;
		final int tileCountOnStart;
		final int tileCountOnEnd;
		final int mapTiles;

		public MapInfo(MapInterface map, int tileCountOnStart, int tileCountOnEnd) {
			super();
			this.map = map;
			this.tileCountOnStart = tileCountOnStart;
			this.tileCountOnEnd = tileCountOnEnd;
			this.mapTiles = map.calculateTilesToDownload();
		}

		@Override
		public int hashCode() {
			return map.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof MapInfo))
				return false;
			return map.equals(((MapInfo) obj).map);
		}
	}

	public static interface AtlasCreationController {

		public void abortAtlasCreation();

		public void pauseResumeAtlasCreation();

		public boolean isPaused();

	}

	public static void main(String[] args) {
		Logging.configureLogging();
		GUIExceptionHandler.installToolkitEventQueueProxy();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log.error("The selection of look and feel failed!", e);
		}
		AtlasProgress ap = new AtlasProgress(null);
		ap.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ap.setVisible(true);
	}
}