package tac.gui;

// Denna klass skapades	: 2003-10-16 av Fredrik M�ller
// Senast �ndrad		: 2003-10-21 av Fredrik M�ller
//						: 2003-10-24 av Fredrik M�ller
//						: 2003-10-25 av Fredrik M�ller
//						: 2004-03-04 av Fredrik M�ller
//						: 2004-03-08 av Fredrik M�ller
//                      : 2007-05-01 av Fredrik M�ller
//                      : 2007-07-22 av Fredrik M�ller
//                      : 2007-07-28 av Fredrik M�ller

// Importeringar

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import tac.program.AtlasThread;
import tac.utilities.GBC;
import tac.utilities.Utilities;

/**
 * A window showing the progress while {@link AtlasThread} downloads and
 * processes the map tiles.
 * 
 */
public class AtlasProgress extends JFrame implements ActionListener {

	private static final long serialVersionUID = 3159146939361532653L;

	private static final Timer TIMER = new Timer(true);

	private JProgressBar atlasProgressBar;
	private JProgressBar layerProgressBar;
	private JProgressBar mapProgressBar;
	private JProgressBar tarProgressBar;

	private JPanel background;

	private long initialTotalTime;
	private long initialLayerTime;

	private long numberOfDownloadedBytes = 0;

	private int totalNumberOfTiles;
	private int numberOfLayers;

	private int atlasProgress = 0;
	private int layerProgress = 0;
	private int layerProgressMax = 0;
	private int mapProgress = 0;
	private int mapProgressMax = 0;
	private int tarProgress = 0;
	private int currentLayer = 0;

	private JLabel windowTitle;

	private JLabel title;
	private JLabel layerTitle;
	private JLabel atlasPercent;
	private JLabel layerPercent;
	private JLabel atlasElementsDone;
	private JLabel layerElementsDone;
	private JLabel atlasTimeLeft;
	private JLabel layerTimeLeft;
	private JLabel mapCreation;
	private JLabel tarCreation;
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

	private ActionListener abortListener = null;

	private UpdateTask updateTask = null;
	private GUIUpdater guiUpdater = null;

	private AtlasThread atlasThread;

	public AtlasProgress(AtlasThread atlasThread) {
		super("Downloading tiles...");
		this.atlasThread = atlasThread;
		setLayout(new GridBagLayout());
		updateTask = new UpdateTask();
		guiUpdater = new GUIUpdater();

		background = new JPanel(new GridBagLayout());

		windowTitle = new JLabel("ATLAS DOWNLOAD INFORMATION");
		windowTitle.setBounds(10, 5, 180, 15);

		title = new JLabel("Downloading layers for atlas:");

		atlasElementsDone = new JLabel("000 of 000 done");
		atlasPercent = new JLabel("Percent done: 100% ");
		atlasTimeLeft = new JLabel("Time remaining: 00000 minutes 00 seconds", JLabel.RIGHT);
		atlasProgressBar = new JProgressBar();

		layerTitle = new JLabel("Downloading tiles for ZOOM LEVEL = 000");
		layerElementsDone = new JLabel("1000000 of 1000000 tiles done");
		layerPercent = new JLabel("Percent done: 100% ");
		layerTimeLeft = new JLabel("Time remaining: 00000 minutes 00 seconds", JLabel.RIGHT);
		layerProgressBar = new JProgressBar();

		mapCreation = new JLabel("Map Creation");
		mapProgressBar = new JProgressBar();

		tarCreation = new JLabel("Tar Creation");
		tarProgressBar = new JProgressBar();

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
		abortAtlasDownloadButton.setEnabled(true);
		dismissWindowButton = new JButton("wait..");
		dismissWindowButton.setToolTipText("Download in progress...");
		dismissWindowButton.setEnabled(false);
		openProgramFolderButton = new JButton("wait..");
		openProgramFolderButton.setToolTipText("Download in progress...");
		openProgramFolderButton.setEnabled(false);

		GBC gbcStd = GBC.std();
		GBC gbcRIF = GBC.std().insets(0, 0, 20, 0).fill(GBC.HORIZONTAL);
		GBC gbcEol = GBC.eol();
		GBC gbcEolFill = GBC.eol().fill(GBC.HORIZONTAL);
		GBC gbcEolFillI = GBC.eol().fill(GBC.HORIZONTAL).insets(0, 5, 0, 0);

		background.add(windowTitle, gbcEol);
		background.add(Box.createVerticalStrut(20), gbcEol);

		background.add(title, gbcRIF);
		background.add(atlasElementsDone, gbcRIF);
		background.add(atlasPercent, gbcRIF);
		background.add(atlasTimeLeft, gbcEolFill);
		background.add(atlasProgressBar, gbcEolFillI);
		background.add(Box.createVerticalStrut(20), gbcEol);

		background.add(layerTitle, gbcRIF);
		background.add(layerElementsDone, gbcRIF);
		background.add(layerPercent, gbcRIF);
		background.add(layerTimeLeft, gbcEolFill);
		background.add(layerProgressBar, gbcEolFillI);
		background.add(Box.createVerticalStrut(20), gbcEol);

		background.add(mapCreation, gbcEol);
		background.add(mapProgressBar, gbcEolFillI);
		background.add(Box.createVerticalStrut(10), gbcEol);

		background.add(tarCreation, gbcEol);
		background.add(tarProgressBar, gbcEolFillI);
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
		bottomPanel.add(openProgramFolderButton, gbcRight);

		background.add(bottomPanel, gbcEolFillI);

		JPanel borderPanel = new JPanel(new GridBagLayout());
		borderPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		borderPanel.add(background, GBC.std().insets(10, 10, 10, 10).fill());

		add(borderPanel, GBC.std().fill());
		setUndecorated(true);

		abortAtlasDownloadButton.addActionListener(this);
		dismissWindowButton.addActionListener(this);
		openProgramFolderButton.addActionListener(this);

		// Initialize the layout in respect to the layout (font size ...)
		pack();

		// The layout is now initialized - we disable it because we don't want
		// want to the labels to jump around if the content changes.
		background.setLayout(null);

		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dContent = getSize();
		setLocation((dScreen.width - dContent.width) / 2, (dScreen.height - dContent.height) / 2);

		tarProgressBar.setMinimum(0);
		tarProgressBar.setMaximum(2);
		tarProgressBar.setValue(0);

		atlasProgressBar.setMinimum(0);
		atlasProgressBar.setValue(0);

		layerProgressBar.setMinimum(0);
		layerProgressBar.setValue(0);

		activeDownloadsValue.setText(": 0");
		downloadErrorsValue.setText(": 0 / 0");
	}

	public void init(int totalNumberOfTiles, int numberOfLayers) {
		this.totalNumberOfTiles = totalNumberOfTiles;
		this.numberOfLayers = numberOfLayers;

		initialTotalTime = System.currentTimeMillis();
		initialLayerTime = System.currentTimeMillis();
		updateGUI();
		TIMER.schedule(updateTask, 0, 500);
	}

	public void initLayer(int numberOfTiles) {
		layerProgressMax = numberOfTiles;
		initialLayerTime = System.currentTimeMillis();
		mapProgress = 0;
		updateGUI();
	}

	public void incAtlasProgress() {
		atlasProgress++;
		updateGUI();
	}

	public void initMap(int numberOfTiles) {
		mapProgressMax = numberOfTiles;
		updateGUI();
	}

	public void setLayer(int layer) {
		this.currentLayer = layer;
		updateGUI();
	}

	public void setLayerProgress(int progress) {
		this.layerProgress = progress;
	}

	public void setErrorCounter(int retryErrors, int permanentErrors) {
		downloadErrorsValue.setText(": " + retryErrors + " / " + permanentErrors);
	}

	public void incMapProgress() {
		mapProgress += 1;
		updateGUI();
	}

	public void incTarProgress() {
		tarProgress += 1;
		updateGUI();
	}

	public void addDownloadedBytes(int bytes) {
		numberOfDownloadedBytes += bytes;
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
		layerTitle.setText("Downloading tiles for ZOOM LEVEL = " + Integer.toString(theZoomLevel));
	}

	public void atlasCreationFinished() {
		stopUpdateTask();
		abortListener = null;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				abortAtlasDownloadButton.setEnabled(false);

				setTitle("Download finished");

				dismissWindowButton.setText("Close Window");
				dismissWindowButton.setToolTipText("Close atlas download progress window");
				dismissWindowButton.setEnabled(true);

				openProgramFolderButton.setText("Open Atlas Folder");
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
			abortListener = null;
			setVisible(false);
		} finally {
			dispose();
		}
	}

	public ActionListener getAbortListener() {
		return abortListener;
	}

	public void setAbortListener(ActionListener abortListener) {
		this.abortListener = abortListener;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (openProgramFolderButton.equals(source)) {
			try {
				String strCmd = "rundll32 url.dll,FileProtocolHandler" + " "
						+ System.getProperty("user.dir");
				Runtime.getRuntime().exec(strCmd);
			} catch (Exception ex) {
			}
		} else if (dismissWindowButton.equals(source)) {
			abortListener = null;
			closeWindow();
		} else if (abortAtlasDownloadButton.equals(source)) {
			if (abortListener != null)
				abortListener.actionPerformed(null);
		}
	}

	private class GUIUpdater implements Runnable {

		public void run() {
			// atlas progress
			atlasProgressBar.setMaximum(totalNumberOfTiles);
			atlasProgressBar.setValue(atlasProgress);

			String stringPercent = Integer
					.toString(((int) (atlasProgressBar.getPercentComplete() * 100)));

			atlasPercent.setText("Percent done: " + stringPercent + " %");

			long timePerElement = 0;
			if (atlasProgress != 0) // Check for a possible division by zero
				timePerElement = (System.currentTimeMillis() - initialTotalTime) / atlasProgress;

			long seconds = (timePerElement * (atlasProgressBar.getMaximum() - atlasProgress) / 1000);
			atlasTimeLeft.setText(formatRemainingTime(seconds));

			// layer progress
			layerProgressBar.setMaximum(layerProgressMax);
			layerProgressBar.setValue(layerProgress);

			stringPercent = Integer.toString(((int) (layerProgressBar.getPercentComplete() * 100)));

			layerPercent.setText("Percent done: " + stringPercent + " %");

			layerElementsDone.setText(Integer.toString(layerProgress) + " of "
					+ layerProgressBar.getMaximum() + " tiles done");

			if (layerProgress == 0) {
				layerTimeLeft.setText(formatRemainingTime(-1));
			} else {
				timePerElement = (System.currentTimeMillis() - initialLayerTime) / layerProgress;
				seconds = (timePerElement * (layerProgressBar.getMaximum() - layerProgress) / 1000);
				layerTimeLeft.setText(formatRemainingTime(seconds));
			}

			// map progress
			mapProgressBar.setValue(mapProgress);
			mapProgressBar.setMaximum(mapProgressMax);
			atlasElementsDone.setText(currentLayer + " of " + numberOfLayers + " done");

			// tar progress
			tarProgressBar.setValue(tarProgress);

			// bytes per second
			long rate = numberOfDownloadedBytes * 1000;
			long time = System.currentTimeMillis() - initialLayerTime;
			if (mapProgress == 0) {
				if (time == 0) {
					nrOfDownloadedBytesPerSecondValue.setText(": ?? KiByte / Second");
				} else {
					rate = rate / time;
					nrOfDownloadedBytesPerSecondValue.setText(": " + Utilities.formatBytes(rate)
							+ " / Second");
				}
			}

			// downloaded bytes
			nrOfDownloadedBytesValue.setText(": " + Utilities.formatBytes(numberOfDownloadedBytes));

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
			activeDownloadsValue.setText(": " + atlasThread.getActiveDownloads());
			activeDownloadsValue.repaint();
		}

	}

	private void updateGUI() {
		SwingUtilities.invokeLater(guiUpdater);
	}

	private class UpdateTask extends TimerTask {

		@Override
		public void run() {
			updateGUI();
		}
	}

}