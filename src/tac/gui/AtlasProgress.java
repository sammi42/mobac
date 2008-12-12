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
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import tac.program.AtlasThread;
import tac.utilities.GBC;
import tac.utilities.Utilities;

/**
 * A window showing the progress while {@link AtlasThread} downloads and
 * processes the map tiles.
 * 
 */
public class AtlasProgress extends JFrame {

	private static final long serialVersionUID = 3159146939361532653L;

	private static final Timer TIMER = new Timer(true);

	private JProgressBar atlasProgress;
	private JProgressBar layerProgress;
	private JProgressBar mapProgress;
	private JProgressBar tarProgress;

	private JPanel background;

	private long initiateTime;
	private long initiateLayerTime;

	private long numberOfDownloadedBytes = 0;

	int nrOfLayers = 0;

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
	private JLabel totalDownloadTime;
	private JLabel totalDownloadTimeValue;

	private JButton dismissWindowButton;
	private JButton openProgramFolderButton;
	private JButton abortAtlasDownloadButton;

	private ActionListener abortListener = null;

	private UpdateTask updateDisplay = null;

	private AtlasThread atlasThread;

	public AtlasProgress(AtlasThread atlasThread) {
		super("Downloading tiles...");
		this.atlasThread = atlasThread;
		setLayout(new GridBagLayout());
		updateDisplay = new UpdateTask();

		background = new JPanel(new GridBagLayout());

		windowTitle = new JLabel("ATLAS DOWNLOAD INFORMATION");
		windowTitle.setBounds(10, 5, 180, 15);

		title = new JLabel("Downloading layers for atlas:");

		atlasElementsDone = new JLabel("000 of 000 done");
		atlasPercent = new JLabel("Percent done: 100% ");
		atlasTimeLeft = new JLabel("Time remaining: 00000 minutes 00 seconds", JLabel.RIGHT);
		atlasProgress = new JProgressBar();

		layerTitle = new JLabel("Downloading tiles for ZOOM LEVEL = 000");
		layerElementsDone = new JLabel("1000000 of 1000000 tiles done");
		layerPercent = new JLabel("Percent done: 100% ");
		layerTimeLeft = new JLabel("Time remaining: 00000 minutes 00 seconds", JLabel.RIGHT);
		layerProgress = new JProgressBar();

		mapCreation = new JLabel("Map Creation");
		mapProgress = new JProgressBar();

		tarCreation = new JLabel("Tar Creation");
		tarProgress = new JProgressBar();

		nrOfDownloadedBytesPerSecond = new JLabel("Average download speed");
		nrOfDownloadedBytesPerSecondValue = new JLabel();
		nrOfDownloadedBytes = new JLabel("Total download size");
		nrOfDownloadedBytesValue = new JLabel();
		activeDownloads = new JLabel("Active Downloads");
		activeDownloadsValue = new JLabel();
		totalDownloadTime = new JLabel("Total download time");
		totalDownloadTimeValue = new JLabel();

		abortAtlasDownloadButton = new JButton("Abort");
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
		background.add(atlasProgress, gbcEolFillI);
		background.add(Box.createVerticalStrut(20), gbcEol);

		background.add(layerTitle, gbcRIF);
		background.add(layerElementsDone, gbcRIF);
		background.add(layerPercent, gbcRIF);
		background.add(layerTimeLeft, gbcEolFill);
		background.add(layerProgress, gbcEolFillI);
		background.add(Box.createVerticalStrut(20), gbcEol);

		background.add(mapCreation, gbcEol);
		background.add(mapProgress, gbcEolFillI);
		background.add(Box.createVerticalStrut(10), gbcEol);

		background.add(tarCreation, gbcEol);
		background.add(tarProgress, gbcEolFillI);
		background.add(Box.createVerticalStrut(10), gbcEol);

		JPanel infoPanel = new JPanel(new GridBagLayout());
		GBC gbci = GBC.std().insets(0, 3, 3, 3);
		infoPanel.add(nrOfDownloadedBytes, gbci);
		infoPanel.add(nrOfDownloadedBytesValue, gbci.toggleEol());
		infoPanel.add(nrOfDownloadedBytesPerSecond, gbci.toggleEol());
		infoPanel.add(nrOfDownloadedBytesPerSecondValue, gbci.toggleEol());
		infoPanel.add(activeDownloads, gbci.toggleEol());
		infoPanel.add(activeDownloadsValue, gbci.toggleEol());
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

		abortAtlasDownloadButton.addActionListener(new JButtonListener());
		dismissWindowButton.addActionListener(new JButtonListener());
		openProgramFolderButton.addActionListener(new JButtonListener());

		// Initialize the layout in respect to the layout (font size ...)
		pack();

		// The layout is now initialized - we disable it because we don't want
		// want to the labels to jump around if the content changes.
		background.setLayout(null);

		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dContent = getSize();
		setLocation((dScreen.width - dContent.width) / 2, (dScreen.height - dContent.height) / 2);
	}

	public void init(int totalNrOfTiles, int theNrOfLayers) {
		tarProgress.setMinimum(0);
		tarProgress.setMaximum(2);
		tarProgress.setValue(0);

		atlasProgress.setMinimum(0);
		atlasProgress.setMaximum(totalNrOfTiles);
		atlasProgress.setValue(0);

		layerProgress.setMinimum(0);
		layerProgress.setMaximum(theNrOfLayers);
		layerProgress.setValue(0);

		nrOfLayers = theNrOfLayers;
		atlasElementsDone.setText("0 of " + nrOfLayers + " done");

		initiateTime = System.currentTimeMillis();
		initiateLayerTime = System.currentTimeMillis();
		TIMER.schedule(updateDisplay, 0, 500);
	}

	public void setMinMaxForCurrentLayer(int theMinimumValue, int theMaximumValue) {
		layerProgress.setMinimum(theMinimumValue);
		layerProgress.setMaximum(theMaximumValue);
	}

	public void setInitiateTimeForLayer() {
		initiateLayerTime = System.currentTimeMillis();
	}

	public void updateAtlasProgressBar(int theElementsDone) {
		this.setAtlasCurrent(theElementsDone);
		this.setAtlasTimeLeft();
	}

	public void updateAtlasProgressBarLayerText(int theElementsDone) {
		this.setAtlasElementsDone(theElementsDone);
	}

	public void updateLayerProgressBar(int theElementsDone) {
		this.setLayerCurrent(theElementsDone);
		this.setLayerElementsDone(theElementsDone);
		this.setLayerTimeLeft(theElementsDone);
	}

	public void initMapProgressBar(int maxElements) {
		mapProgress.setMaximum(maxElements);
	}

	public void incMapProgressBar() {
		mapProgress.setValue(mapProgress.getValue() + 1);
	}

	public void incTarPrograssBar() {
		tarProgress.setValue(tarProgress.getValue() + 1);
	}

	private void setAtlasCurrent(int theElementsDone) {
		atlasProgress.invalidate();
		atlasProgress.setValue(theElementsDone);

		String stringPercent = Integer.toString(((int) (atlasProgress.getPercentComplete() * 100)));

		atlasPercent.setText("Percent done: " + stringPercent + " %");
	}

	private void setLayerCurrent(int theElementsDone) {
		layerProgress.invalidate();
		layerProgress.setValue(theElementsDone);

		String stringPercent = Integer.toString(((int) (layerProgress.getPercentComplete() * 100)));

		layerPercent.setText("Percent done: " + stringPercent + " %");
	}

	private void setAtlasElementsDone(int theElementsDone) {
		atlasElementsDone.setText(Integer.toString(theElementsDone) + " of " + nrOfLayers
				+ " layers done");
	}

	private void setLayerElementsDone(int theElementsDone) {
		layerElementsDone.setText(Integer.toString(theElementsDone) + " of "
				+ layerProgress.getMaximum() + " tiles done");
	}

	private void setAtlasTimeLeft() {
		int progress = getAtlasProgressValue();
		long timePerElement = (System.currentTimeMillis() - initiateTime) / progress;

		long seconds = (timePerElement * (atlasProgress.getMaximum() - progress) / 1000);
		atlasTimeLeft.setText(formatRemainingTime(seconds));
	}

	private void setLayerTimeLeft(int theElementsDoneInt) {
		if (theElementsDoneInt == 0) {
			layerTimeLeft.setText(formatRemainingTime(-1));
		} else {
			long timePerElement = (System.currentTimeMillis() - initiateLayerTime)
					/ theElementsDoneInt;
			long seconds = (timePerElement * (layerProgress.getMaximum() - theElementsDoneInt) / 1000);
			layerTimeLeft.setText(formatRemainingTime(seconds));
		}
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
		abortAtlasDownloadButton.setEnabled(false);

		setTitle("Download finished");

		dismissWindowButton.setText("Close");
		dismissWindowButton.setToolTipText("Close ATLAS DOWNLOAD INFORMATION window");
		dismissWindowButton.setEnabled(true);

		openProgramFolderButton.setText("Open");
		openProgramFolderButton.setToolTipText("Open folder where Atlas is created");
		openProgramFolderButton.setEnabled(true);
	}

	private synchronized void stopUpdateTask() {
		try {
			updateDisplay.cancel();
			updateDisplay = null;
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

	public void updateViewNrOfDownloadedBytes() {
		nrOfDownloadedBytesValue.setText(": " + Utilities.formatBytes(numberOfDownloadedBytes));
	}

	public void updateViewNrOfDownloadedBytesPerSecond() {
		long rate = numberOfDownloadedBytes * 1000;
		long time = System.currentTimeMillis() - initiateTime;
		if (time == 0) {
			nrOfDownloadedBytesPerSecondValue.setText(": ?? KiByte / Second");
		} else {
			rate = rate / time;
			nrOfDownloadedBytesPerSecondValue.setText(": " + Utilities.formatBytes(rate)
					+ " / Second");
		}
	}

	public void updateTotalDownloadTime() {

		String timeString = "";

		long seconds = 0;
		long minutes = 0;

		long totalMilliseconds = 0;

		totalMilliseconds = System.currentTimeMillis() - initiateTime;

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
	}

	public void updateActiveDownloads() {
		activeDownloadsValue.setText(": " + atlasThread.getActiveDownloads());
		activeDownloadsValue.repaint();
	}

	public int getAtlasProgressValue() {
		return atlasProgress.getValue();
	}

	public ActionListener getAbortListener() {
		return abortListener;
	}

	public void setAbortListener(ActionListener abortListener) {
		this.abortListener = abortListener;
	}

	public void addDownloadedBytes(int bytes) {
		numberOfDownloadedBytes += bytes;
	}

	private class JButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();

			if (actionCommand.equals("Open")) {

				try {
					String strCmd = "rundll32 url.dll,FileProtocolHandler" + " "
							+ System.getProperty("user.dir");
					Runtime.getRuntime().exec(strCmd);
				}

				catch (IOException ioe) {
				}
			}

			else if (actionCommand.equals("Close")) {
				abortListener = null;
				closeWindow();
			}

			else if (actionCommand.equals("Abort")) {
				if (abortListener != null)
					abortListener.actionPerformed(null);
			}
		}
	}

	private class UpdateTask extends TimerTask {

		@Override
		public void run() {
			try {
				updateTotalDownloadTime();
				updateActiveDownloads();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * For debugging purposes: Shows the atlas download/progress dialog without
	 * having to start TAC or download anything
	 */
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		AtlasProgress ap = new AtlasProgress(null);
		ap.init(100, 3);
		ap.setVisible(true);
		ap.setDefaultCloseOperation(EXIT_ON_CLOSE);
		ap.setMinMaxForCurrentLayer(0, 100);
		ap.setZoomLevel(1);
		ap.setInitiateTimeForLayer();
		ap.updateAtlasProgressBar(ap.getAtlasProgressValue() + 1);
		ap.updateLayerProgressBar(10);
		ap.updateViewNrOfDownloadedBytes();
		ap.updateViewNrOfDownloadedBytesPerSecond();
		ap.updateTotalDownloadTime();

	}
}