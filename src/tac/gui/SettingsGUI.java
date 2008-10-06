package tac.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import tac.program.Settings;

public class SettingsGUI extends JDialog {	
	private static final long serialVersionUID = -5227934684609357198L;
	private JRadioButton dituGoogleCom; // ditu.google.com
	private JRadioButton mapsGoogleCom; // maps.google.com
	
	private JCheckBox tileStoreEnabled;
	
	private JComboBox mapSize;
	
	private JButton okButton;
	private JButton cancelButton;

	private JTabbedPane tabbedPane;
	private JDialog jdialogGUI;
	private WindowDestroyer windowListener;
	
	public SettingsGUI() {
		this.createJFrame();
		this.createTabbedPane();
		this.createJButtons();
		this.applySettings();
		this.addListeners();

		jdialogGUI 	= this;
	}

	// Skapa huvudfönstret
	private void createJFrame() {
		// Hämta in skärmstorlek för att kunna positionera progressbarfönstret här nedan
		Dimension	dScreen		= Toolkit.getDefaultToolkit().getScreenSize();
		Dimension	dContent 	= new Dimension(500,342);

		// Sätter storlek och position på progressbarfönstret. Fönstret hamnar centrerat på
		// skärmen.
		this.setLocation((dScreen.width-dContent.width)/2,(dScreen.height-dContent.height)/2);
		this.setSize(dContent);
		this.setResizable(false);
		this.getContentPane().setLayout(null);
		this.setTitle("Settings");
	}

	// Create tabbed pane
	public void createTabbedPane() {
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0,0,492,275);
		tabbedPane.add(createGoogleSitePanel(), "Google site");
		tabbedPane.add(createTileStorePanel(), "Tile store");
		tabbedPane.add(createMapSizePanel(), "Map size");
		
		this.getContentPane().add(tabbedPane);
	}

	// Skapa fliken för tumnagelinställningar
	private JPanel createGoogleSitePanel() {
		// Skapa RadioButtons
		dituGoogleCom = new JRadioButton("ditu.google.com" );
		dituGoogleCom.setBounds(7,40,120,15);
		dituGoogleCom.setActionCommand("ditu.google.com");

		mapsGoogleCom = new JRadioButton("maps.google.com");
		mapsGoogleCom.setBounds(7,60,120,15);
		mapsGoogleCom.setActionCommand("maps.google.com");

		// Skapa Buttongroup
		ButtonGroup radioButtonGroup = new ButtonGroup();

		radioButtonGroup.add(dituGoogleCom);
		radioButtonGroup.add(mapsGoogleCom);
		
		JPanel leftPanel = new JPanel(null);
		leftPanel.setBounds(5,5,475,240);
		leftPanel.setBorder(BorderFactory.createTitledBorder("Site to download tiles from"));

		// Lägg till objekt till den vänstra panelen
		leftPanel.add(dituGoogleCom);
		leftPanel.add(mapsGoogleCom);
		
		// Skapa Bakgrundspanel
		JPanel thumbNailBackGround = new JPanel(null);
		thumbNailBackGround.add(leftPanel);
		
		return thumbNailBackGround;
	}
	
	private JPanel createTileStorePanel() {
		
		tileStoreEnabled = new JCheckBox("Enable tile store");
		tileStoreEnabled.setBounds(7,40,120,15);
		
		JPanel leftPanel = new JPanel(null);
		leftPanel.setBounds(5,5,475,240);
		leftPanel.setBorder(BorderFactory.createTitledBorder("Tile store settings"));
		
		leftPanel.add(tileStoreEnabled);
		
		// Skapa Bakgrundspanel
		JPanel thumbNailBackGround = new JPanel(null);
		thumbNailBackGround.add(leftPanel);
		
		return thumbNailBackGround;	
	}
	
	private JPanel createMapSizePanel() {
		
		// Sizes from 512 to 4096
		Vector<Integer>mapSizes = new Vector<Integer>();
		int valueToAdd = 512;
		for (int i = 0; i < 4; i++) {
			mapSizes.addElement(valueToAdd);
			valueToAdd *= 2; 
		}
		
		mapSize = new JComboBox(mapSizes);
		mapSize.setBounds(7,40,120,20);
		
		JPanel leftPanel = new JPanel(null);
		leftPanel.setBounds(5,5,475,240);
		leftPanel.setBorder(BorderFactory.createTitledBorder("Map size settings"));
		
		leftPanel.add(mapSize);
		
		// Skapa Bakgrundspanel
		JPanel thumbNailBackGround = new JPanel(null);
		thumbNailBackGround.add(leftPanel);
		
		return thumbNailBackGround;	
	}
 
	// Skapa knappar
	public void createJButtons(){
		okButton = new JButton("Ok");
		okButton.setBounds(364,280,50,25);
				
		cancelButton = new JButton("Cancel");
		cancelButton.setBounds(418,280,74,25);

		this.getContentPane().add(okButton);
		this.getContentPane().add(cancelButton);
	}
	
	// Läsa in alla inställningar och uppdatera GUI:t med rätt värden.
	private void applySettings() {
		Settings s = Settings.getInstance();
		String googleDownloadSite = s.getSelectedGoogleDownloadSite();
		
		if (googleDownloadSite.equals("maps.google.com")) {
			mapsGoogleCom.setSelected(true);
		} else {
			dituGoogleCom.setSelected(true);
		}
		
		tileStoreEnabled.setSelected(s.getTileStoreEnabled());
		
		int size = s.getMapSize();
		int index = 0;
		
		switch (size) {
		case 512:
			index = 0;
			break;
		case 1024:
			index = 1;
			break;
		case 2048:
			index = 2;
			break;
		case 4096:
			index = 3;
			break;
		default:
			index = 0;
			break;
		}
		mapSize.setSelectedIndex(index);
	}

	// Registrera lyssnare till olika objekt
	private void addListeners()	{
		
		windowListener = new WindowDestroyer();
		
		this.addWindowListener(windowListener);
		okButton.addActionListener(new JButtonListener());
		cancelButton.addActionListener(new JButtonListener());
	}
	
	public void removeWindowListener(){
		this.removeWindowListener(windowListener);
	}
	
	
	public void addWindowListener(){
		this.addWindowListener(windowListener);
	}

	// WindowDestroyer
	private class WindowDestroyer extends WindowAdapter	{
		public void windowClosing (WindowEvent e)	{
			((JDialog)e.getComponent()).dispose();
		}
		public void windowDeactivated(WindowEvent e) {
			// Se till så att progressbaren ligger i focus. Fungerar dock inte ifall huvudfönstret
			// minimeras eller markeras.
			((JDialog)e.getComponent()).requestFocus();
		}
	}

	private class JButtonListener implements ActionListener	{
					
		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();

			// Ifall Ok-knappen har blivit klickad. Då skall alla värden blivit valda i
			// inställningsfönstret läsas av och sparas ner till intställningsfilen.
			if(actionCommand.equals("Ok")) {
				
				Settings s = Settings.getInstance();
				
				if (dituGoogleCom.isSelected()) {
					s.setSelectedGoogleDownloadSite("ditu.google.com");
				}
				else if (mapsGoogleCom.isSelected()) {
					s.setSelectedGoogleDownloadSite("maps.google.com");
				}
				
				s.setTileStoreEnabled(tileStoreEnabled.isSelected());
				
				int index = mapSize.getSelectedIndex();
				int size = 512;
				
				switch (index) {
				case 0:
					size = 512;
					break;
				case 1:
					size = 1024;
					break;
				case 2:
					size = 2048;
					break;
				case 3:
					size = 4096;
					break;
				default:
					size = 512;
					break;
				}				
				s.setMapSize(size);
								
				// Close the dialog window
				jdialogGUI.dispose();
			}
			else if(actionCommand.equals("Cancel"))
				jdialogGUI.dispose();
		}
	}
}