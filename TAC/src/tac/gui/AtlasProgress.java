package tac.gui;

import static tac.utilities.Utilities.fmt;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import tac.program.AtlasThread;
import tac.program.Logging;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.utilities.GBC;
import tac.utilities.OSUtilities;
import tac.utilities.TACExceptionHandler;
import tac.utilities.Utilities;

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
		int retryErrors = 0;
		int permanentErrors = 0;
	}

	private final Data data = new Data();

	private JLabel windowTitle;

	private JLabel title;
	private JLabel mapInfo;
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

	private JButton dismissWindowButton;
	private JButton openProgramFolderButton;
	private JButton abortAtlasDownloadButton;
	private JButton pauseResumeDownloadButton;

	private DownloadControlerListener downloadControlListener = null;

	private UpdateTask updateTask = null;
	private GUIUpdater guiUpdater = null;

	private AtlasThread atlasThread;

	private static String TEXT_MAP_DOWNLOAD = "Downloading tiles for map number ";
	private static String TEXT_PERCENT = "Percent done: %d%%";

	public AtlasProgress(AtlasThread atlasThread) {
		super("Atlas creation in progress");
		this.atlasThread = atlasThread;
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

		title = new JLabel("Downloading maps of atlas:");

		mapInfo = new JLabel("Downloading map: ");

		atlasMapsDone = new JLabel("000 of 000 done");
		atlasPercent = new JLabel(fmt(TEXT_PERCENT, 100));
		atlasTimeLeft = new JLabel("Time remaining: 00000 minutes 00 seconds", JLabel.RIGHT);
		atlasProgressBar = new JProgressBar();

		mapDownloadTitle = new JLabel(TEXT_MAP_DOWNLOAD + "000");
		mapDownloadElementsDone = new JLabel("1000000 of 1000000 tiles done");
		mapDownloadPercent = new JLabel(fmt(TEXT_PERCENT, 100));
		mapDownloadTimeLeft = new JLabel("Time remaining: 00000 minutes 00 seconds", JLabel.RIGHT);
		mapDownloadProgressBar = new JProgressBar();

		mapCreation = new JLabel("Map Creation");
		mapCreationProgressBar = new JProgressBar();

		nrOfDownloadedBytesPerSecond = new JLabel("Average download speed");
		nrOfDownloadedBytesPerSecondValue = new JLabel();
		nrOfDownloadedBytes = new JLabel("Total download size");
		nrOfDownloadedBytesValue = new JLabel();
		activeDownloads = new JLabel("Active Downloads");
		activeDownloadsValue = new JLabel();
		downloadErrors = new JLabel("Download errors");
		downloadErrors
				.setToolTipText("<html>Download errors for the current layer (retryable/permanent):<br>"
						+ "TAC tries to retry failed tile downloads up to three times.<br>"
						+ "For each failed try the retryable counter increases by one.<br>"
						+ "If the tile downloads fails the third time the tile will be counted as "
						+ "permanent error.</html>");
		downloadErrorsValue = new JLabel();
		downloadErrorsValue.setToolTipText(downloadErrors.getToolTipText());
		totalDownloadTime = new JLabel("Total download time");
		totalDownloadTimeValue = new JLabel();

		abortAtlasDownloadButton = new JButton("Abort Download");
		abortAtlasDownloadButton.setToolTipText("Abort current Atlas download");
		dismissWindowButton = new JButton("Close Window");
		dismissWindowButton.setToolTipText("Download in progress...");
		dismissWindowButton.setVisible(false);
		openProgramFolderButton = new JButton("Open Atlas Folder");
		openProgramFolderButton.setToolTipText("Download in progress...");
		openProgramFolderButton.setEnabled(false);
		pauseResumeDownloadButton = new JButton("Pause/Resume");

		GBC gbcStd = GBC.std();
		GBC gbcRIF = GBC.std().insets(0, 0, 20, 0).fill(GBC.HORIZONTAL);
		GBC gbcEol = GBC.eol();
		GBC gbcEolFill = GBC.eol().fill(GBC.HORIZONTAL);
		GBC gbcEolFillI = GBC.eol().fill(GBC.HORIZONTAL).insets(0, 5, 0, 0);

//		background.add(windowTitle, gbcEolFill);
//		background.add(Box.createVerticalStrut(10), gbcEol);

		background.add(mapInfo, gbcEolFill);
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

		bottomPanel.add(infoPanel, gbcStd);

		GBC gbcRight = GBC.std().anchor(GBC.SOUTHEAST).insets(5, 0, 0, 0);
		bottomPanel.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		bottomPanel.add(abortAtlasDownloadButton, gbcRight);
		bottomPanel.add(dismissWindowButton, gbcRight);
		bottomPanel.add(pauseResumeDownloadButton, gbcRight);
		bottomPanel.add(openProgramFolderButton, gbcRight);

		background.add(bottomPanel, gbcEolFillI);

		JPanel borderPanel = new JPanel(new GridBagLayout());
//		borderPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		borderPanel.add(background, GBC.std().insets(10, 10, 10, 10).fill());

		add(borderPanel, GBC.std().fill());
//		setUndecorated(true);

		abortAtlasDownloadButton.addActionListener(this);
		dismissWindowButton.addActionListener(this);
		openProgramFolderButton.addActionListener(this);
		pauseResumeDownloadButton.addActionListener(this);
	}

	public void init(AtlasInterface atlasInterface) {
		data.atlasInterface = atlasInterface;
		data.totalNumberOfTiles = atlasInterface.calculateTilesToDownload();
		int mapCount = 0;
		for (LayerInterface layer : atlasInterface)
			mapCount += layer.getMapCount();
		data.totalNumberOfMaps = mapCount;

		initialTotalTime = System.currentTimeMillis();
		initialMapDownloadTime = System.currentTimeMillis();
		updateGUI();
		setVisible(true);
		TIMER.schedule(updateTask, 0, 500);
	}

	public void initMapDownload(MapInterface map) {
		data.map = map;
		data.mapDownloadNumberOfTiles = map.calculateTilesToDownload();
		initialMapDownloadTime = System.currentTimeMillis();
		data.mapCreationProgress = 0;
		data.mapDownloadProgress = 0;
		data.currentMapNumber++;
		updateGUI();
	}

	public void initMapCreation(int maxTilesToProcess) {
		data.mapCreationProgress = 0;
		data.mapCreationMax = maxTilesToProcess;
		initialMapDownloadTime = System.currentTimeMillis();
		updateGUI();
	}

	public void setErrorCounter(int retryErrors, int permanentErrors) {
		data.retryErrors = retryErrors;
		data.permanentErrors = permanentErrors;
		updateGUI();
	}

	public void incMapDownloadProgress() {
		data.mapDownloadProgress++;
		data.totalProgress++;
	}

	public void incMapCreationProgress() {
		data.mapCreationProgress++;
		updateGUI();
	}

	public void addDownloadedBytes(int bytes) {
		data.numberOfDownloadedBytes += bytes;
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
				abortAtlasDownloadButton.setEnabled(false);

				windowTitle.setText("<html><h2>ATLAS CREATION FINISHED SUCCESSFULLY</h2></html>");
				mapInfo.setText("");
				setTitle("Atlas creation finished successfully");

				abortAtlasDownloadButton.setVisible(false);

				dismissWindowButton.setToolTipText("Close atlas download progress window");
				dismissWindowButton.setVisible(true);

				openProgramFolderButton.setToolTipText("Open folder where Atlas is created");
				openProgramFolderButton.setEnabled(true);
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

	public DownloadControlerListener getDownloadControlListener() {
		return downloadControlListener;
	}

	public void setDownloadControlerListener(DownloadControlerListener threadControlListener) {
		this.downloadControlListener = threadControlListener;
	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		String workingDir = System.getProperty("user.dir");
		File atlasFolder = new File(workingDir, "atlases");
		if (openProgramFolderButton.equals(source)) {
			try {
				OSUtilities.openFolderBrowser(atlasFolder);
			} catch (Exception e) {
				log.error("", e);
			}
		} else if (dismissWindowButton.equals(source)) {
			downloadControlListener = null;
			closeWindow();
		} else if (abortAtlasDownloadButton.equals(source)) {
			updateTask.cancel();
			if (downloadControlListener != null)
				downloadControlListener.stopDownload();
			else
				dispose();
		} else if (pauseResumeDownloadButton.equals(source)) {
			if (downloadControlListener!= null)
				downloadControlListener.pauseResumeDownload();
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
			if (scheduledCounter > 0)
				return;
			synchronized (this) {
				scheduledCounter++;
			}
			SwingUtilities.invokeLater(this);
		}

		public void run() {
			synchronized (this) {
				scheduledCounter--;
			}

			if (data.map != null)
				mapInfo
						.setText("<html>Downloading map \"<b>" + data.map.getName()
								+ "</b>\" of layer <b>\"" + data.map.getLayer().getName()
								+ "\"</b></html>");

			// atlas progress
			atlasProgressBar.setMaximum(data.totalNumberOfTiles);
			atlasProgressBar.setValue(data.totalProgress);

			int newPercent = (int) (atlasProgressBar.getPercentComplete() * 100);
			if (data.totalProgressPercent != newPercent) {
				data.totalProgressPercent = newPercent;
				atlasPercent.setText(fmt(TEXT_PERCENT, data.totalProgressPercent));
				if (data.atlasInterface != null)
					AtlasProgress.this.setTitle(Integer.toString(data.totalProgressPercent)
							+ " % - Downloading atlas \"" + data.atlasInterface.getName() + "\"");
			}

			long seconds = -1;
			if (data.totalProgress != 0) {
				// Avoid for a possible division by zero
				int totalTilesRemaining = data.totalNumberOfTiles - data.totalProgress;
				long totalElapsedTime = System.currentTimeMillis() - initialTotalTime;
				seconds = (totalElapsedTime * totalTilesRemaining / (1000L * data.totalProgress));
			}
			atlasTimeLeft.setText(formatRemainingTime(seconds));

			// layer progress
			mapDownloadProgressBar.setMaximum(data.mapDownloadNumberOfTiles);
			mapDownloadProgressBar.setValue(data.mapDownloadProgress);

			mapDownloadPercent.setText(fmt(TEXT_PERCENT, (int) (mapDownloadProgressBar
					.getPercentComplete() * 100)));

			mapDownloadElementsDone.setText(Integer.toString(data.mapDownloadProgress) + " of "
					+ data.mapDownloadNumberOfTiles + " tiles done");

			seconds = -1;
			if (data.mapDownloadProgress != 0)
				seconds = ((System.currentTimeMillis() - initialMapDownloadTime)
						* (data.mapDownloadNumberOfTiles - data.mapDownloadProgress) / (1000L * data.mapDownloadProgress));
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
			if (data.mapCreationProgress == 0) {
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

			// total download time
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

			downloadErrorsValue.setText(": " + data.retryErrors + " / " + data.permanentErrors);
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
			DownloadControlerListener listener = AtlasProgress.this.downloadControlListener;
			if (listener != null)
				listener.stopDownload();
		}

	}

	public static interface DownloadControlerListener {

		public void stopDownload();

		public void pauseResumeDownload();

	}

	public static void main(String[] args) {
		Logging.configureLogging();
		TACExceptionHandler.installToolkitEventQueueProxy();
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