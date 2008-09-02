package moller.tac;
// Denna klass skapades	: 2003-10-16 av Fredrik Möller
// Senast ändrad		: 2003-10-21 av Fredrik Möller
//						: 2003-10-24 av Fredrik Möller
//						: 2003-10-25 av Fredrik Möller
//						: 2004-03-04 av Fredrik Möller
//						: 2004-03-08 av Fredrik Möller
//                      : 2007-05-01 av Fredrik Möller
//                      : 2007-07-22 av Fredrik Möller
//                      : 2007-07-28 av Fredrik Möller

// Importeringar

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class AtlasProgress extends JFrame{
	
	private static final long serialVersionUID = 3159146939361532653L;
	private static AtlasProgress ap;
	private JProgressBar atlasProgress;
	private JProgressBar layerProgress;
	private JProgressBar tarProgress;

	private JPanel background;

	private long initiateTime;
	private long initiateLayerTime;
	
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

	public static synchronized AtlasProgress getInstance() {
		if (ap == null) {
			ap = new AtlasProgress();
		}
		return ap;
	}
		
	private AtlasProgress() {
		
		tarProgressValue = 0;

		// Hämta in skärmstorlek för att kunna positionera progressbarfönstret här nedan
		Dimension	dScreen		= Toolkit.getDefaultToolkit().getScreenSize();
		Dimension	dContent 	= new Dimension(700,285);

		// Sätter storlek och position på progressbarfönstret. Fönstret hamnar centrerat på
		// skärmen.
		this.setLocation((dScreen.width-dContent.width)/2,(dScreen.height-dContent.height)/2);
		this.setSize(dContent);

		// Skapar JPanel att lägga alla nedanstående grafikobjekt på.
		background = new JPanel(null);
		background.setBorder(BorderFactory.createRaisedBevelBorder());

		windowTitle = new JLabel("ATLAS DOWNLOAD INFORMATION");
		windowTitle.setBounds(10,5,180,15);

		// Skapar JLabel för titel
		title = new JLabel("Downloading layers for atlas:");
		title.setBounds(10,40,180,15);

		// Skapar JLabel för avklarade objekt
		atlasElementsDone = new JLabel();
		atlasElementsDone.setBounds(230,40,135,15);


		// Skapar JLabel för procent avklarat
		percent = new JLabel();
		percent.setBounds(370,40,120,15);


		// Skapar JLabel för återstående tid
		timeLeft = new JLabel();
		timeLeft.setBounds(500,40,200,15);

		// Skapar progressbar för atlas
		atlasProgress = new JProgressBar();
		atlasProgress.setBounds(10,62,680,20);


		layerTitle = new JLabel("Downloading tiles for ZOOM LEVEL = ");
		layerTitle.setBounds(10,105,180,15);

		layerZoomLevel = new JLabel("");
		layerZoomLevel.setBounds(190,105,20,15);

		layerElementsDone = new JLabel();
		layerElementsDone.setBounds(230,105,135,15);

		// Skapar JLabel för procent avklarat
		layerPercent = new JLabel();
		layerPercent.setBounds(370,105,120,15);

		// Skapar JLabel för återstående tid
		timeLayerLeft = new JLabel();
		timeLayerLeft.setBounds(500,105,200,15);

		// Skapar progressbar för layers
		layerProgress = new JProgressBar();
		layerProgress.setBounds(10,127,680,20);
		
		tarCreation = new JLabel("TAR Creation");
		tarCreation.setBounds(10,170,180,15);
		
		tarProgress = new JProgressBar();
		tarProgress.setBounds(10,192,680,20);

		nrOfDownloadedBytesPerSecond = new JLabel("Average download speed");
		nrOfDownloadedBytesPerSecond.setBounds(10,220,140,15);

		nrOfDownloadedBytesPerSecondValue = new JLabel();
		nrOfDownloadedBytesPerSecondValue.setBounds(150,220,240,15);

		nrOfDownloadedBytes = new JLabel("Total download size");
		nrOfDownloadedBytes.setBounds(10,240,140,15);

		nrOfDownloadedBytesValue = new JLabel();
		nrOfDownloadedBytesValue.setBounds(150,240,200,15);

		totalDownloadTime = new JLabel("Total download time");
		totalDownloadTime.setBounds(10,260,140,15);

		totalDownloadTimeValue = new JLabel();
		totalDownloadTimeValue.setBounds(150,260,200,15);
		

		abortAtlasDownloadButton = new JButton("Abort");
		abortAtlasDownloadButton.setToolTipText("Abort current Atlas download");
		abortAtlasDownloadButton.setBounds(500,255,60,20);
		abortAtlasDownloadButton.setEnabled(true);
		
		dismissWindowButton = new JButton("wait..");
		dismissWindowButton.setToolTipText("Download in progress...");
		dismissWindowButton.setBounds(565,255,60,20);
		dismissWindowButton.setEnabled(false);
		
		openProgramFolderButton = new JButton("wait..");
		openProgramFolderButton.setToolTipText("Download in progress...");
		openProgramFolderButton.setBounds(630,255,60,20);
		openProgramFolderButton.setEnabled(false);
				
		// Lägger alla grafikobjekt på bakgrunden
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

		// Lägger bakgrunden på ContentPane
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
	}

	public void setMinMaxForCurrentLayer(int theMinimumValue, int theMaximumValue){

		layerProgress.setMinimum(theMinimumValue);
		layerProgress.setMaximum(theMaximumValue);
  	}

  	public void setInitiateTimeForLayer() {
		initiateLayerTime = System.currentTimeMillis();
	}

	// Metod för att uppdatera alla fält och värden på progressbaren.
	public void updateAtlasProgressBar(int theElementsDone){

		this.setAtlasCurrent(theElementsDone);
		this.setAtlasTimeLeft();
	}

	public void updateAtlasProgressBarLayerText(int theElementsDone){

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

	// Hjälpmetod för att sätta till hur många procent arbetet är avklarat.
	private void setAtlasCurrent(int theElementsDone){
		atlasProgress.invalidate();
		atlasProgress.setValue(theElementsDone);

		String stringPercent = Integer.toString(((int)(atlasProgress.getPercentComplete() * 100)));

		percent.setText("Percent done: " + stringPercent + " %");
	}

	// Hjälpmetod för att sätta till hur många procent arbetet är avklarat.
	private void setLayerCurrent(int theElementsDone){
		layerProgress.invalidate();
		layerProgress.setValue(theElementsDone);

		String stringPercent = Integer.toString(((int)(layerProgress.getPercentComplete() * 100)));

		layerPercent.setText("Percent done: " + stringPercent + " %");
	}

	// Hjälpmetod för att sätta hur många element som är avklarade
	private void setAtlasElementsDone(int theElementsDone){
		atlasElementsDone.setText(Integer.toString(theElementsDone) + " " + "of" + " " + nrOfLayers + " " + "layers done");
	}

	// Hjälpmetod för att sätta hur många element som är avklarade
	private void setLayerElementsDone(int theElementsDone){
		layerElementsDone.setText(Integer.toString(theElementsDone) + " " + "of" + " " + layerProgress.getMaximum() + " " + "tiles done");
	}


	// Hjälpmetod för att visa hur många sekunder det återstår innan arbetet är utfört
	private void setAtlasTimeLeft(){
		// Räkna ut tiden det, i snitt, tar för att utföra en iteration i det som skall
		// utföras
		double timePerElement = (System.currentTimeMillis() - initiateTime) / this.getAtlasProgressValue();

		int secondsLeft = ((int)timePerElement * (atlasProgress.getMaximum() - this.getAtlasProgressValue()) / 1000);
		int minutesLeft = 0;
		String timeLeftString;

		if(secondsLeft > 60){
			if (secondsLeft > 119) {
				minutesLeft = secondsLeft/60;
				secondsLeft = secondsLeft%60;
				timeLeftString = Integer.toString(minutesLeft) + " " + "minutes" + " " + Integer.toString(secondsLeft) + " " + "seconds";
			}
			else {
				minutesLeft = secondsLeft/60;
				secondsLeft = secondsLeft%60;
				timeLeftString = Integer.toString(minutesLeft) + " " + "minute" + " " + Integer.toString(secondsLeft) + " " + "seconds";	
			}
		}
		else{
			if (secondsLeft > 1) {
				timeLeftString = Integer.toString(secondsLeft) + " " + "seconds";
			}
			else {
				timeLeftString = Integer.toString(secondsLeft) + " " + "second";
			}
		}
		timeLeft.setText("Time remaining: " + timeLeftString);
	}


	// Hjälpmetod för att visa hur många sekunder det återstår innan arbetet är utfört
	private void setLayerTimeLeft(int theElementsDoneInt){

		// Räkna ut tiden det, i snitt, tar för att utföra en iteration i det som skall
		// utföras
		double timePerElement = (System.currentTimeMillis() - initiateLayerTime) / theElementsDoneInt;

		int secondsLeft = ((int)timePerElement * (layerProgress.getMaximum() - theElementsDoneInt) / 1000);
		int minutesLeft = 0;
		String timeLeftString;
		
		if(secondsLeft > 60){
			if (secondsLeft > 119) {
				minutesLeft = secondsLeft/60;
				secondsLeft = secondsLeft%60;
				timeLeftString = Integer.toString(minutesLeft) + " " + "minutes" + " " + Integer.toString(secondsLeft) + " " + "seconds";
			}
			else {
				minutesLeft = secondsLeft/60;
				secondsLeft = secondsLeft%60;
				timeLeftString = Integer.toString(minutesLeft) + " " + "minute" + " " + Integer.toString(secondsLeft) + " " + "seconds";	
			}
		}
		else{
			if (secondsLeft > 1) {
				timeLeftString = Integer.toString(secondsLeft) + " " + "seconds";
			}
			else {
				timeLeftString = Integer.toString(secondsLeft) + " " + "second";
			}
		}
	
		timeLayerLeft.setText("Time remaining: " + timeLeftString);
	}

	public void setZoomLevel(int theZoomLevel) {
		layerZoomLevel.setText(Integer.toString(theZoomLevel));
	}

	public void setButtonText() {

		abortAtlasDownloadButton.setEnabled(false);
		
		dismissWindowButton.setText("Close");
		dismissWindowButton.setToolTipText("Close ATLAS DOWNLOAD INFORMATION window");
		dismissWindowButton.setEnabled(true);
		
		openProgramFolderButton.setText("Open");
		openProgramFolderButton.setToolTipText("Open folder where Atlas is created");
		openProgramFolderButton.setEnabled(true);
	}

	public void closeWindow() {
		this.dispose();
		ap = null;
	}

	public void updateViewNrOfDownloadedBytes() {

		String convertedString = "";

		if (ProcessValues.getNrOfDownloadedBytes() > 0) {
			convertedString = ProcessValues.getNrOfDownloadedBytes() + "";

			if ((convertedString.indexOf(".") < convertedString.length() - 2) && (convertedString.indexOf(".") > -1)) {
				convertedString = convertedString.substring(0, convertedString.indexOf((".")) + 2) + " Bytes";
			}
			else {
				convertedString = convertedString + " Bytes";
			}
		}

		if (ProcessValues.getNrOfDownloadedBytes() > 1000) {
			convertedString = ProcessValues.getNrOfDownloadedBytes() / 1000 + "";

			if ((convertedString.indexOf(".") < convertedString.length() - 2) && (convertedString.indexOf(".") > -1)) {
				convertedString = convertedString.substring(0, convertedString.indexOf((".")) + 2) + " KiloBytes";
			}
			else {
				convertedString = convertedString + " KiloBytes";
			}
		}

		if (ProcessValues.getNrOfDownloadedBytes() > 1000000) {
			convertedString = ProcessValues.getNrOfDownloadedBytes() / 1000000 + "";

			if ((convertedString.indexOf(".") < convertedString.length() - 2) && (convertedString.indexOf(".") > -1)) {
				convertedString = convertedString.substring(0, convertedString.indexOf((".")) + 2) + " MegaBytes";
			}
			else {
				convertedString = convertedString + " MegaBytes";
			}
		}
		nrOfDownloadedBytesValue.setText(": " + convertedString);
	}

	public void updateViewNrOfDownloadedBytesPerSecond() {
		nrOfDownloadedBytesPerSecondValue.setText(": " + Long.toString((long)ProcessValues.getNrOfDownloadedBytes() / (System.currentTimeMillis() - initiateTime)) + " KiloByte / Second");
	}

	public void updateTotalDownloadTime() {

		String timeString = "";

		long seconds = 0;
		long minutes = 0;

		long totalMilliseconds = 0;

		totalMilliseconds = System.currentTimeMillis() - initiateTime;

		if (totalMilliseconds > 60000 ) {

			minutes = totalMilliseconds / 60000;
			seconds = (totalMilliseconds - (minutes * 60000)) / 1000;
			timeString = minutes + " minute(s) and " + seconds + " second(s)";
		}
		else {
			seconds = totalMilliseconds / 1000;
			timeString = seconds + " second(s)";
		}
		totalDownloadTimeValue.setText(": " + timeString);
	}

	public int getAtlasProgressValue() {
		return atlasProgress.getValue();
	}

	// Knapplyssnarklass
	private class JButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			String actionCommand = e.getActionCommand();

			if(actionCommand.equals("Open")) {

				try {
					String strCmd = "rundll32 url.dll,FileProtocolHandler" + " " + System.getProperty("user.dir");
					Runtime.getRuntime().exec(strCmd);
				}

				catch(IOException ioe){
				}
			}

			else if(actionCommand.equals("Close")) {

				closeWindow();
			}
			
			else if (actionCommand.equals("Abort")) {
				ProcessValues.setAbortAtlasDownload(true);
			}
		}
	}
}