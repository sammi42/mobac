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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import tac.program.AtlasThread;
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
	private JProgressBar tarProgress;

	private JPanel background;

	private long initiateTime;
	private long initiateLayerTime;

	private long numberOfDownloadedBytes = 0;

	private int tarProgressValue;

	int nrOfLayers = 0;

	private JLabel windowTitle;

	private JLabel title;
	private JLabel layerTitle;
	private JLabel layerZoomLevel;
	private JLabel percent;
	private JLabel layerPercent;
	private JLabel atlasElementsDone;
	private JLabel layerElementsDone;
	private JLabel timeLeft;
	private JLabel timeLayerLeft;
	private JLabel tarCreation;
	private JLabel nrOfDownloadedBytes;
	private JLabel nrOfDownloadedBytesValue;
	private JLabel nrOfDownloadedBytesPerSecond;
	private JLabel nrOfDownloadedBytesPerSecondValue;
	private JLabel totalDownloadTime;
	private JLabel totalDownloadTimeValue;

	private JButton dismissWindowButton;
	private JButton openProgramFolderButton;
	private JButton abortAtlasDownloadButton;

	private ActionListener abortListener = null;

	private UpdateTask updateDisplay = null;

	public AtlasProgress() {

		super("Downloading tiles...");
		updateDisplay = new UpdateTask();
		tarProgressValue = 0;

		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dContent = new Dimension(700, 285);

		this.setLocation((dScreen.width - dContent.width) / 2,
				(dScreen.height - dContent.height) / 2);
		this.setSize(dContent);

		background = new JPanel(null);
		background.setBorder(BorderFactory.createRaisedBevelBorder());

		windowTitle = new JLabel("ATLAS DOWNLOAD INFORMATION");
		windowTitle.setBounds(10, 5, 180, 15);

		title = new JLabel("Downloading layers for atlas:");
		title.setBounds(10, 40, 180, 15);

		atlasElementsDone = new JLabel();
		atlasElementsDone.setBounds(230, 40, 135, 15);

		percent = new JLabel();
		percent.setBounds(370, 40, 120, 15);

		timeLeft = new JLabel();
		timeLeft.setBounds(500, 40, 200, 15);

		atlasProgress = new JProgressBar();
		atlasProgress.setBounds(10, 62, 680, 20);

		layerTitle = new JLabel("Downloading tiles for ZOOM LEVEL = ");
		layerTitle.setBounds(10, 105, 180, 15);

		layerZoomLevel = new JLabel("");
		layerZoomLevel.setBounds(190, 105, 20, 15);

		layerElementsDone = new JLabel();
		layerElementsDone.setBounds(230, 105, 135, 15);

		layerPercent = new JLabel();
		layerPercent.setBounds(370, 105, 120, 15);

		timeLayerLeft = new JLabel();
		timeLayerLeft.setBounds(500, 105, 200, 15);

		layerProgress = new JProgressBar();
		layerProgress.setBounds(10, 127, 680, 20);

		tarCreation = new JLabel("TAR Creation");
		tarCreation.setBounds(10, 170, 180, 15);

		tarProgress = new JProgressBar();
		tarProgress.setBounds(10, 192, 680, 20);

		nrOfDownloadedBytesPerSecond = new JLabel("Average download speed");
		nrOfDownloadedBytesPerSecond.setBounds(10, 220, 140, 15);

		nrOfDownloadedBytesPerSecondValue = new JLabel();
		nrOfDownloadedBytesPerSecondValue.setBounds(150, 220, 240, 15);

		nrOfDownloadedBytes = new JLabel("Total download size");
		nrOfDownloadedBytes.setBounds(10, 240, 140, 15);

		nrOfDownloadedBytesValue = new JLabel();
		nrOfDownloadedBytesValue.setBounds(150, 240, 200, 15);

		totalDownloadTime = new JLabel("Total download time");
		totalDownloadTime.setBounds(10, 260, 140, 15);

		totalDownloadTimeValue = new JLabel();
		totalDownloadTimeValue.setBounds(150, 260, 200, 15);

		abortAtlasDownloadButton = new JButton("Abort");
		abortAtlasDownloadButton.setToolTipText("Abort current Atlas download");
		abortAtlasDownloadButton.setBounds(500, 255, 60, 20);
		abortAtlasDownloadButton.setEnabled(true);

		dismissWindowButton = new JButton("wait..");
		dismissWindowButton.setToolTipText("Download in progress...");
		dismissWindowButton.setBounds(565, 255, 60, 20);
		dismissWindowButton.setEnabled(false);

		openProgramFolderButton = new JButton("wait..");
		openProgramFolderButton.setToolTipText("Download in progress...");
		openProgramFolderButton.setBounds(630, 255, 60, 20);
		openProgramFolderButton.setEnabled(false);

		background.add(windowTitle);
		background.add(atlasProgress);
		background.add(layerTitle);
		background.add(layerZoomLevel);
		background.add(layerElementsDone);
		background.add(layerPercent);
		background.add(timeLayerLeft);
		background.add(layerProgress);
		background.add(title);
		background.add(percent);
		background.add(atlasElementsDone);
		background.add(timeLeft);
		background.add(tarCreation);
		background.add(tarProgress);
		background.add(nrOfDownloadedBytes);
		background.add(nrOfDownloadedBytesValue);
		background.add(nrOfDownloadedBytesPerSecond);
		background.add(nrOfDownloadedBytesPerSecondValue);
		background.add(totalDownloadTime);
		background.add(totalDownloadTimeValue);
		background.add(abortAtlasDownloadButton);
		background.add(dismissWindowButton);
		background.add(openProgramFolderButton);

		getContentPane().add(background);

		this.setUndecorated(true);

		abortAtlasDownloadButton.addActionListener(new JButtonListener());
		dismissWindowButton.addActionListener(new JButtonListener());
		openProgramFolderButton.addActionListener(new JButtonListener());
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

	public void updateTarPrograssBar() {
		tarProgressValue++;
		this.tarProgress.setValue(tarProgressValue);
	}

	private void setAtlasCurrent(int theElementsDone) {
		atlasProgress.invalidate();
		atlasProgress.setValue(theElementsDone);

		String stringPercent = Integer.toString(((int) (atlasProgress.getPercentComplete() * 100)));

		percent.setText("Percent done: " + stringPercent + " %");
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
		timeLeft.setText(formatRemainingTime(seconds));
	}

	private void setLayerTimeLeft(int theElementsDoneInt) {

		long timePerElement = (System.currentTimeMillis() - initiateLayerTime) / theElementsDoneInt;
		long seconds = (timePerElement * (layerProgress.getMaximum() - theElementsDoneInt) / 1000);
		timeLayerLeft.setText(formatRemainingTime(seconds));
	}

	private String formatRemainingTime(long seconds) {
		int minutesLeft = 0;
		String timeLeftString;

		if (seconds > 60) {
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
		layerZoomLevel.setText(Integer.toString(theZoomLevel));
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
		stopUpdateTask();
		abortListener = null;
		setVisible(false);
		dispose();
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
			updateTotalDownloadTime();
			if (!AtlasProgress.this.isVisible())
				stopUpdateTask();
		}
	}
}