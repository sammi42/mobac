package moller.tac;


// Importeringar
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JFrame;

// Klassdefinition
public class PreviewProgressBar extends JFrame{
		
	private static final long serialVersionUID = 4769464581926237949L;
	
	// Instansvariabler
	private JProgressBar progress;
	private JPanel background;

	private long initiateTime;
	private int elementsDoneInt;

	private JLabel title;
	private JLabel percent;
	private JLabel elementsDone;
	private JLabel timeLeft;

	// Konstruktor
	public PreviewProgressBar(int theMinimumValue, int theMaximumValue){
	//public ProgressBar(){

		// Hämta in skärmstorlek för att kunna positionera progressbarfönstret här nedan
		Dimension	dScreen		= Toolkit.getDefaultToolkit().getScreenSize();
		Dimension	dContent 	= new Dimension(500,52);

		// Sätter storlek och position på progressbarfönstret. Fönstret hamnar centrerat på
		// skärmen.
		this.setLocation((dScreen.width-dContent.width)/2,(dScreen.height-dContent.height)/2);
		this.setSize(dContent);

		// Skapar JPanel att lägga alla nedanstående grafikobjekt på.
		background = new JPanel(null);
		background.setBorder(BorderFactory.createRaisedBevelBorder());

		// Skapar JLabel för titel
		title = new JLabel("Downloading preview images:");
		title.setBounds(10,5,170,15);

		// Skapar JLabel för procent avklarat
		percent = new JLabel();
		percent.setBounds(180,5,120,15);

		// Skapar JLabel för avklarade objekt
		elementsDone = new JLabel();
		elementsDone.setBounds(225,5,185,15);

		// Skapar JLabel för återstående tid
		timeLeft = new JLabel();
		timeLeft.setBounds(310,5,190,15);

		// Skapar progressbar
		progress = new JProgressBar(theMinimumValue, theMaximumValue);
		progress.setBounds(10,27,480,20);

		// Lägger alla grafikobjekt på bakgrunden
		background.add(progress);
		background.add(title);
		background.add(percent);
		background.add(elementsDone);
		background.add(timeLeft);

		// Lägger bakgrunden på ContentPane
		getContentPane().add(background);

		initiateTime = System.currentTimeMillis();

		this.setUndecorated(true);
  	}


	// Metod för att uppdatera alla fält och värden på progressbaren.
	public void updateProgressBar(int theElementsDone){
		elementsDoneInt = theElementsDone;

		this.setCurrent();
		this.setTimeLeft();
	}


	// Hjälpmetod för att sätta till hur många procent arbetet är avklarat.
	private void setCurrent(){
		progress.invalidate();
		progress.setValue(elementsDoneInt);

		String stringPercent = Integer.toString(((int)(progress.getPercentComplete() * 100)));

		percent.setText("Percent done: " + stringPercent + " %");
	}

	// Hjälpmetod för att visa hur många sekunder det återstår innan arbetet är utfört
	private void setTimeLeft(){
		// Räkna ut tiden det, i snitt, tar för att utföra en iteration i det som skall
		// utföras
		double timePerElement = (System.currentTimeMillis() - initiateTime) / elementsDoneInt;

		int secondsLeft = ((int)timePerElement * (progress.getMaximum() - elementsDoneInt) / 1000);
		String timeLeftString;
		
		timeLeftString = Integer.toString(secondsLeft) + " " + "seconds";
		
		timeLeft.setText("Time remaining: " + timeLeftString);
	}
}