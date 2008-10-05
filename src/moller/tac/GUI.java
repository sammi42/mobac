package moller.tac;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import moller.preview.MapSelectionListener;
import moller.preview.PreviewMap;
import moller.tac.utilities.PersistentProfiles;

public class GUI extends JFrame implements MapSelectionListener {

	private static final long serialVersionUID = -8444942802691874960L;

	// public static final NumberFormat DF = new DecimalFormat("0.000000");

	private JPanel leftPanel;
	private JPanel rightPanel;
	private JPanel coordinatesPanel;
	private JPanel zoomLevelPanel;
	private JPanel tileSizePanel;
	private JPanel atlasNamePanel;
	private JPanel profilesPanel;
	private JPanel previewPanel;

	private PreviewMap previewMap;

	private JButton createAtlasButton;
	private JButton deleteProfileButton;
	private JButton saveAsProfileButton;
	private JButton previewButton;
	private JButton settingsGUIButton;

	private JToggleButton chooseProfileButton;

	private JLabel coordinatesLabel;
	private JLabel latMinLabel;
	private JLabel latMaxLabel;
	private JLabel longMinLabel;
	private JLabel longMaxLabel;
	private JLabel zoomLevelLabel;
	private JLabel tileSizeLabel;
	private JLabel customTileSizeWidthLabel;
	private JLabel customTileSizeHeightLabel;
	private JLabel atlasNameLabel;
	private JLabel profilesLabel;
	private JLabel amountOfTilesLabel;

	private JTextField latMinTextField;
	private JTextField latMaxTextField;
	private JTextField longMinTextField;
	private JTextField longMaxTextField;
	private JTextField tileSizeWidthTextField;
	private JTextField tileSizeHeightTextField;
	private JTextField atlasNameTextField;

	private JCheckBox cbOne;
	private JCheckBox cbTwo;
	private JCheckBox cbThree;
	private JCheckBox cbFour;
	private JCheckBox cbFive;
	private JCheckBox cbSix;
	private JCheckBox cbSeven;
	private JCheckBox cbEight;
	private JCheckBox cbNine;
	private JCheckBox cbTen;

	private JComboBox tileSizeWidthComboBox;
	private JComboBox tileSizeHeightComboBox;

	private GridLayout previewLayout;

	private Vector<Integer> tileSizeValues;
	private Vector<Profile> profilesVector;
	private Vector<String> profileNamesVector;

	private Double latMax;
	private Double latMin;
	private Double longMax;
	private Double longMin;

	private File preview;
	private File overlay;

	private JList profilesJList;
	private Thread downloadThread;
	private Thread atlasThread;
	private Thread getGoogleDownloadStringThread;

	private PreviewProgressBar pb;
	private AtlasProgress ap;
	private TileXYMinMaxAndZoom tXY;
	private String fileSeparator;

	private JPopupMenu jpm;

	private JMenuItem topLeftCorner;
	private JMenuItem bottomRightCorner;

	public GUI() {
		super();
		createMainFrame();
		createLeftPanel();
		createRightPanel();
		createJPopupMenu();
		addListeners();
		initiateProgram();
	}

	private void createMainFrame() {

		this.setTitle("TrekBuddy Atlas Creator v0.8");

		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dContent = new Dimension(1000, 768);

		this.setLocation((dScreen.width - dContent.width) / 2,
				(dScreen.height - dContent.height) / 2);
		this.setSize(dContent);
		this.setMinimumSize(dContent);
		this.setResizable(true);
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		// this.getContentPane().setLayout(null);
		setLayout(new BorderLayout());

		// Try to set the Windows look and feel...
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		// ... if that is not possible, try to set the look and feel associated
		// to the system
		catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (UnsupportedLookAndFeelException e1) {
				System.out
						.println("The selection of look and feel was not possible, due to: "
								+ e1.toString());
				e1.printStackTrace();
			} catch (Exception e1) {
			}
		}
	}

	public void createLeftPanel() {

		leftPanel = new JPanel(null);
		leftPanel.setPreferredSize(new Dimension(280, 730));
		leftPanel.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));

		coordinatesLabel = new JLabel("COORDINATES");
		coordinatesLabel.setBounds(5, 5, 150, 20);

		coordinatesPanel = new JPanel(null);
		coordinatesPanel.setBounds(3, 28, 275, 231);
		coordinatesPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		latMaxLabel = new JLabel("Latitude Max");
		latMaxLabel.setBounds(112, 20, 100, 20);

		latMaxTextField = new JTextField();
		latMaxTextField.setBounds(88, 42, 100, 20);
		latMaxTextField.setActionCommand("latMaxTextField");

		longMinLabel = new JLabel("Longitude Min");
		longMinLabel.setBounds(20, 80, 100, 20);

		longMinTextField = new JTextField();
		longMinTextField.setBounds(5, 102, 100, 20);
		longMinTextField.setActionCommand("longMinTextField");

		longMaxLabel = new JLabel("Longitude Max");
		longMaxLabel.setBounds(190, 80, 100, 20);

		longMaxTextField = new JTextField();
		longMaxTextField.setBounds(169, 102, 100, 20);
		longMaxTextField.setActionCommand("longMaxTextField");

		latMinLabel = new JLabel("Latitude Min");
		latMinLabel.setBounds(112, 150, 100, 20);

		latMinTextField = new JTextField();
		latMinTextField.setBounds(88, 172, 100, 20);
		latMinTextField.setActionCommand("latMinTextField");

		previewButton = new JButton("Preview");
		previewButton.setBounds(88, 200, 100, 20);

		zoomLevelLabel = new JLabel("ZOOM LEVELS (1, 2, 3 ..... 8, 9, 10)");
		zoomLevelLabel.setBounds(5, 260, 200, 20);

		amountOfTilesLabel = new JLabel();
		amountOfTilesLabel.setBounds(175, 260, 100, 20);
		amountOfTilesLabel.setHorizontalAlignment(JLabel.RIGHT);
		amountOfTilesLabel.setToolTipText("Total amount of tiles to download");

		zoomLevelPanel = new JPanel(null);
		zoomLevelPanel.setBounds(3, 283, 275, 30);
		zoomLevelPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		cbOne = new JCheckBox();
		cbOne.setBounds(5, 5, 20, 20);
		cbOne.setToolTipText("Zoom Level 1 (Maximum zoom)");

		cbTwo = new JCheckBox();
		cbTwo.setBounds(30, 5, 20, 20);
		cbTwo.setToolTipText("Zoom Level 2");

		cbThree = new JCheckBox();
		cbThree.setBounds(55, 5, 20, 20);
		cbThree.setToolTipText("Zoom Level 3");

		cbFour = new JCheckBox();
		cbFour.setBounds(80, 5, 20, 20);
		cbFour.setToolTipText("Zoom Level 4");

		cbFive = new JCheckBox();
		cbFive.setBounds(105, 5, 20, 20);
		cbFive.setToolTipText("Zoom Level 5");

		cbSix = new JCheckBox();
		cbSix.setBounds(130, 5, 20, 20);
		cbSix.setToolTipText("Zoom Level 6");

		cbSeven = new JCheckBox();
		cbSeven.setBounds(155, 5, 20, 20);
		cbSeven.setToolTipText("Zoom Level 7");

		cbEight = new JCheckBox();
		cbEight.setBounds(180, 5, 20, 20);
		cbEight.setToolTipText("Zoom Level 8");

		cbNine = new JCheckBox();
		cbNine.setBounds(205, 5, 20, 20);
		cbNine.setToolTipText("Zoom Level 9");

		cbTen = new JCheckBox();
		cbTen.setBounds(230, 5, 20, 20);
		cbTen.setToolTipText("Zoom Level 10 (Minimum zoom)");

		tileSizeLabel = new JLabel("TILE SIZE (Pixels)");
		tileSizeLabel.setBounds(5, 316, 100, 20);

		tileSizePanel = new JPanel(null);
		tileSizePanel.setBounds(3, 340, 275, 55);
		tileSizePanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		tileSizeValues = new Vector<Integer>();
		for (int i = 0; i < 7; i++) {
			tileSizeValues.addElement((i + 1) * 256);
		}

		JLabel tileSizeWidth = new JLabel("Width:");
		tileSizeWidth.setBounds(5, 5, 50, 20);

		tileSizeWidthComboBox = new JComboBox(tileSizeValues);
		tileSizeWidthComboBox.setBounds(45, 5, 70, 20);
		tileSizeWidthComboBox.setToolTipText("Width");

		customTileSizeWidthLabel = new JLabel("Custom size (W):");
		customTileSizeWidthLabel.setBounds(130, 5, 100, 20);

		tileSizeWidthTextField = new JTextField();
		tileSizeWidthTextField.setBounds(218, 5, 50, 20);
		tileSizeWidthTextField.setActionCommand("tileSizeWidthTextField");
		tileSizeWidthTextField.setToolTipText("Width");

		JLabel tileSizeHeight = new JLabel("Height:");
		tileSizeHeight.setBounds(5, 29, 50, 20);

		tileSizeHeightComboBox = new JComboBox(tileSizeValues);
		tileSizeHeightComboBox.setBounds(45, 29, 70, 20);
		tileSizeHeightComboBox.setToolTipText("Height");

		customTileSizeHeightLabel = new JLabel("Custom size (H):");
		customTileSizeHeightLabel.setBounds(130, 29, 100, 20);

		tileSizeHeightTextField = new JTextField();
		tileSizeHeightTextField.setBounds(218, 29, 50, 20);
		tileSizeHeightTextField.setActionCommand("tileSizeHeightTextField");
		tileSizeHeightTextField.setToolTipText("Height");

		atlasNameLabel = new JLabel("ATLAS NAME");
		atlasNameLabel.setBounds(5, 400, 100, 20);

		atlasNamePanel = new JPanel(null);
		atlasNamePanel.setBounds(3, 425, 275, 30);
		atlasNamePanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		atlasNameTextField = new JTextField();
		atlasNameTextField.setBounds(5, 5, 264, 20);
		atlasNameTextField.setActionCommand("atlasNameTextField");

		profilesLabel = new JLabel("SAVED PROFILES");
		profilesLabel.setBounds(5, 460, 100, 20);

		chooseProfileButton = new JToggleButton("UNLOCK/LOCK");
		chooseProfileButton.setBounds(110, 460, 110, 20);

		profilesPanel = new JPanel(null);
		profilesPanel.setBounds(3, 485, 275, 191);
		profilesPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		profilesJList = new JList();
		profilesJList.setBounds(1, 1, 264, 180);
		profilesJList.setEnabled(false);

		JScrollPane scrollPane = new JScrollPane(profilesJList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(5, 5, 264, 180);

		saveAsProfileButton = new JButton("Save as profile");
		saveAsProfileButton.setBounds(3, 708, 110, 20);

		deleteProfileButton = new JButton("Delete profile");
		deleteProfileButton.setBounds(3, 688, 110, 20);

		settingsGUIButton = new JButton("Settings");
		settingsGUIButton.setBounds(179, 688, 100, 20);

		createAtlasButton = new JButton("Create Atlas");
		createAtlasButton.setBounds(179, 708, 100, 20);

		coordinatesPanel.add(latMinLabel);
		coordinatesPanel.add(latMaxLabel);
		coordinatesPanel.add(longMinLabel);
		coordinatesPanel.add(longMaxLabel);

		coordinatesPanel.add(latMinTextField);
		coordinatesPanel.add(latMaxTextField);
		coordinatesPanel.add(longMinTextField);
		coordinatesPanel.add(longMaxTextField);

		coordinatesPanel.add(previewButton);

		zoomLevelPanel.add(cbOne);
		zoomLevelPanel.add(cbTwo);
		zoomLevelPanel.add(cbThree);
		zoomLevelPanel.add(cbFour);
		zoomLevelPanel.add(cbFive);
		zoomLevelPanel.add(cbSix);
		zoomLevelPanel.add(cbSeven);
		zoomLevelPanel.add(cbEight);
		zoomLevelPanel.add(cbNine);
		zoomLevelPanel.add(cbTen);

		tileSizePanel.add(tileSizeWidth);
		tileSizePanel.add(tileSizeWidthComboBox);
		tileSizePanel.add(tileSizeWidthTextField);
		tileSizePanel.add(customTileSizeWidthLabel);
		tileSizePanel.add(tileSizeHeight);
		tileSizePanel.add(tileSizeHeightComboBox);
		tileSizePanel.add(tileSizeHeightTextField);
		tileSizePanel.add(customTileSizeHeightLabel);

		atlasNamePanel.add(atlasNameTextField);

		profilesPanel.add(scrollPane);

		leftPanel.add(coordinatesLabel);
		leftPanel.add(coordinatesPanel);

		leftPanel.add(zoomLevelLabel);
		leftPanel.add(amountOfTilesLabel);
		leftPanel.add(zoomLevelPanel);

		leftPanel.add(tileSizeLabel);
		leftPanel.add(tileSizePanel);

		leftPanel.add(atlasNameLabel);
		leftPanel.add(atlasNamePanel);

		leftPanel.add(profilesLabel);
		leftPanel.add(profilesPanel);
		leftPanel.add(chooseProfileButton);

		leftPanel.add(saveAsProfileButton);
		leftPanel.add(deleteProfileButton);
		leftPanel.add(settingsGUIButton);
		leftPanel.add(createAtlasButton);

		add(leftPanel, BorderLayout.WEST);
	}

	public void createRightPanel() {

		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));

		previewPanel = new JPanel(previewLayout = new GridLayout(1, 1));

		// JScrollPane scrollPane = new JScrollPane(previewPanel,
		// JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		// JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		// scrollPane.setBounds(1, 1, 729, 729);
		// rightPanel.add(scrollPane, BorderLayout.CENTER);
		previewMap = new PreviewMap();
		previewMap.addMapSelectionListener(this);
		rightPanel.add(previewMap, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.CENTER);
	}

	public void createJPopupMenu() {
		jpm = new JPopupMenu();

		topLeftCorner = new JMenuItem("Top left corner");
		bottomRightCorner = new JMenuItem("Bottom right corner");

		jpm.add(topLeftCorner);
		jpm.add(bottomRightCorner);
	}

	public void addListeners() {
		this.addWindowListener(new WindowDestroyer());
		this.addWindowListener(new JFrameListener());
		saveAsProfileButton.addActionListener(new ButtonListener());
		deleteProfileButton.addActionListener(new ButtonListener());
		settingsGUIButton.addActionListener(new ButtonListener());
		createAtlasButton.addActionListener(new ButtonListener());
		previewButton.addActionListener(new ButtonListener());
		latMinTextField.getDocument().addDocumentListener(
				new JTextFieldListener());
		latMaxTextField.getDocument().addDocumentListener(
				new JTextFieldListener());
		longMinTextField.getDocument().addDocumentListener(
				new JTextFieldListener());
		longMaxTextField.getDocument().addDocumentListener(
				new JTextFieldListener());
		tileSizeWidthTextField
				.addFocusListener(new JTextFieldFocusChangeListener());
		tileSizeWidthTextField.getDocument().addDocumentListener(
				new JTextFieldListener());
		tileSizeHeightTextField
				.addFocusListener(new JTextFieldFocusChangeListener());
		tileSizeHeightTextField.getDocument().addDocumentListener(
				new JTextFieldListener());
		atlasNameTextField.getDocument().addDocumentListener(
				new JTextFieldListener());

		chooseProfileButton.addActionListener(new JToggleButtonListener());
		profilesJList.addListSelectionListener(new JListListener());

		topLeftCorner.addActionListener(new JPopupMenuListener());
		bottomRightCorner.addActionListener(new JPopupMenuListener());

		cbOne.addActionListener(new CheckBoxListener());
		cbTwo.addActionListener(new CheckBoxListener());
		cbThree.addActionListener(new CheckBoxListener());
		cbFour.addActionListener(new CheckBoxListener());
		cbFive.addActionListener(new CheckBoxListener());
		cbSix.addActionListener(new CheckBoxListener());
		cbSeven.addActionListener(new CheckBoxListener());
		cbEight.addActionListener(new CheckBoxListener());
		cbNine.addActionListener(new CheckBoxListener());
		cbTen.addActionListener(new CheckBoxListener());
	}

	public void initiateProgram() {

		// Check if all necessary files and folder exists
		Utilities.checkFileSetup();

		// Load the tile store with the persistent
		// objects from disk
		TileStore ts = TileStore.getInstance();
		ts.init();

		profilesVector = new Vector<Profile>();
		profileNamesVector = new Vector<String>();
		Settings settings = Settings.getInstance();
		try {
			settings.load();
		} catch (IOException iox) {
			JOptionPane.showMessageDialog(null,
					"Could not create file settings.xml program will exit.",
					"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		fileSeparator = System.getProperty("file.separator");

		// Load all profiles from the profiles file from disk
		profilesVector = PersistentProfiles.load(new File(System
				.getProperty("user.dir")
				+ fileSeparator + "profiles.xml"));

		for (Profile p : profilesVector) {
			profileNamesVector.add(p.getProfileName());
		}
		profilesJList.setListData(profileNamesVector);

		UpdateGUI.updateAllUIs();
	}

	public String validateInput(boolean isCreateAtlasValidate) {

		String errorText = "";

		if (longMinTextField.getText().length() < 1) {
			errorText = "A value of \"Longitude Min\" must be entered \n";
		} else {
			try {
				Double temp = Utilities.FORMAT_6_DEC.parse(
						longMinTextField.getText()).doubleValue();

				if (temp < -179 || temp > 179) {
					errorText += "Value of \"Longitude Min\" must be between -179 and 179 \n";
				}
			} catch (ParseException nfex) {
				errorText += "Value of \"Longitude Min\" is not a valid decimal number \n";
			}
		}

		if (longMaxTextField.getText().length() < 1) {
			errorText += "A value of \"Longitude Max\" must be entered \n";
		} else {
			try {
				Double temp = Utilities.FORMAT_6_DEC.parse(
						longMaxTextField.getText()).doubleValue();

				if (temp < -179 || temp > 179) {
					errorText += "Value of \"Longitude Max\" must be between -179 and 179 \n";
				}
			} catch (ParseException nfex) {
				errorText += "Value of \"Longitude Max\" is not a valid decimal number \n";
			}
		}

		if (latMaxTextField.getText().length() < 1) {
			errorText += "A value of \"Latitude Max\" must be entered \n";
		} else {
			try {
				Double temp = Utilities.FORMAT_6_DEC.parse(
						latMaxTextField.getText()).doubleValue();

				if (temp < -85 || temp > 85) {
					errorText += "Value of \"Latitude Max\" must be between -85 and 85 \n";
				}
			} catch (ParseException nfex) {
				errorText += "Value of \"Latitude Max\" is not a valid decimal number \n";
			}
		}

		if (latMinTextField.getText().length() < 1) {
			errorText += "A value of \"Latitude Min\" must be entered \n";
		} else {
			try {
				Double temp = Utilities.FORMAT_6_DEC.parse(
						latMinTextField.getText()).doubleValue();

				if (temp < -85 || temp > 85) {
					errorText += "Value of \"Latitude Min\" must be between -85 and 85 \n";
				}
			} catch (ParseException nfex) {
				errorText += "Value of \"Latitude Min\" is not a valid decimal number \n";
			}
		}

		if (isCreateAtlasValidate) {

			if (atlasNameTextField.getText().length() < 1) {
				errorText += "A value of \"Atlas name\" must be entered \n";
			} else {

				int result = -1;

				result = Utilities.validateString(atlasNameTextField.getText(),
						false);

				if (result > -1) {
					errorText += "Atlas name contains illegal characters ("
							+ (char) result + ")\n";
				}
			}
		}

		if (isCreateAtlasValidate) {

			boolean zoomLevelChosen = false;

			if (cbOne.isSelected()) {
				zoomLevelChosen = true;
			}
			if (cbTwo.isSelected()) {
				zoomLevelChosen = true;
			}
			if (cbThree.isSelected()) {
				zoomLevelChosen = true;
			}
			if (cbFour.isSelected()) {
				zoomLevelChosen = true;
			}
			if (cbFive.isSelected()) {
				zoomLevelChosen = true;
			}
			if (cbSix.isSelected()) {
				zoomLevelChosen = true;
			}
			if (cbSeven.isSelected()) {
				zoomLevelChosen = true;
			}
			if (cbEight.isSelected()) {
				zoomLevelChosen = true;
			}
			if (cbNine.isSelected()) {
				zoomLevelChosen = true;
			}
			if (cbTen.isSelected()) {
				zoomLevelChosen = true;
			}

			if (zoomLevelChosen == false) {
				errorText += "A zoom level must be selected\n";
			}

			String input = tileSizeWidthTextField.getText();

			if (input.length() > 0) {

				int result = -1;

				result = Utilities.validateTileSizeInput(input);

				if (result > -1) {
					errorText += "\"" + (char) result
							+ "\" is not valid input of Custom size (W)";

				} else {
					if (Integer.parseInt(input) > 1792) {
						errorText += "\"" + input
								+ "\" is not valid input of Custom size (W)";
					}
					if (Integer.parseInt(input) < 50) {
						errorText += "\"" + input
								+ "\" is not valid input of Custom size (W)";
					}
				}
			}

			input = tileSizeHeightTextField.getText();

			if (input.length() > 0) {

				int result = -1;

				result = Utilities.validateTileSizeInput(input);

				if (result > -1) {
					errorText += "\"" + (char) result
							+ "\" is not valid input of Custom size (H)";

				} else {
					if (Integer.parseInt(input) > 1792) {
						errorText += "\"" + input
								+ "\" is not valid input of Custom size (H)";
					}
					if (Integer.parseInt(input) < 50) {
						errorText += "\"" + input
								+ "\" is not valid input of Custom size (H)";
					}
				}
			}
		}
		return errorText;
	}

	public boolean validateLatLongMinMax() {

		Double latMax;
		Double latMin;
		Double longMax;
		Double longMin;
		try {
			latMax = Utilities.FORMAT_6_DEC.parse(latMaxTextField.getText())
					.doubleValue();
			latMin = Utilities.FORMAT_6_DEC.parse(latMinTextField.getText())
					.doubleValue();
			longMax = Utilities.FORMAT_6_DEC.parse(longMaxTextField.getText())
					.doubleValue();
			longMin = Utilities.FORMAT_6_DEC.parse(longMinTextField.getText())
					.doubleValue();
		} catch (ParseException e) {
			return false;
		}

		boolean maxIsBiggerThanMin = true;

		if (latMax < latMin) {

			JOptionPane.showMessageDialog(null,
					"Latitude Min is greater than Latitude Max", "Errors",
					JOptionPane.ERROR_MESSAGE);
			maxIsBiggerThanMin = false;
		}

		if (longMax < longMin) {

			JOptionPane.showMessageDialog(null,
					"Longitude Min is greater than Longitude Max", "Errors",
					JOptionPane.ERROR_MESSAGE);
			maxIsBiggerThanMin = false;
		}

		return maxIsBiggerThanMin;
	}

	public void calculateNrOfTilesToDownload() {

		if (validateInput(false).length() == 0) {

			SelectedZoomLevels sZL = getSelectedZoomlevels();

			int nrOfLayers = sZL.getNrOfLayers();
			int[] zoomLevels = sZL.getZoomLevels();

			int totalNrOfTiles = 0;

			for (int i = 0; i < nrOfLayers; i++) {

				Point topLeft = GoogleTileUtils
						.toTileXY(
								Double.parseDouble(latMaxTextField.getText()),
								Double.parseDouble(longMaxTextField.getText()),
								zoomLevels[i]);
				Point bottomRight = GoogleTileUtils
						.toTileXY(
								Double.parseDouble(latMinTextField.getText()),
								Double.parseDouble(longMinTextField.getText()),
								zoomLevels[i]);

				totalNrOfTiles = totalNrOfTiles
						+ Utilities.calculateNrOfTiles(new TileXYMinMaxAndZoom(
								topLeft, bottomRight, zoomLevels[i]));
			}
			amountOfTilesLabel.setText("( " + Integer.toString(totalNrOfTiles)
					+ " )");
		}
	}

	// WindowDestroyer
	private class WindowDestroyer extends WindowAdapter {
		public void windowClosing(WindowEvent e) {

			Settings s = Settings.getInstance();
			try {
				s.store();
			} catch (IOException iox) {
				JOptionPane
						.showMessageDialog(
								null,
								"Could not create file settings.xml program will exit.",
								"Error", JOptionPane.ERROR_MESSAGE);
			}
			System.exit(0);
		}
	}

	private class JTextFieldListener implements DocumentListener {

		public void insertUpdate(DocumentEvent e) {

			if (tileSizeWidthTextField.getText().length() > 0) {
				tileSizeWidthComboBox.setEnabled(false);
			}
			if (tileSizeHeightTextField.getText().length() > 0) {
				tileSizeHeightComboBox.setEnabled(false);
			}
			if (handleInputInRealTime()) {
				calculateNrOfTilesToDownload();
			}
		}

		public void removeUpdate(DocumentEvent e) {

			if (tileSizeWidthTextField.getText().length() == 0) {
				tileSizeWidthComboBox.setEnabled(true);
			}
			if (tileSizeHeightTextField.getText().length() == 0) {
				tileSizeHeightComboBox.setEnabled(true);
			}
			if (handleInputInRealTime()) {
				calculateNrOfTilesToDownload();
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}
	}

	private class JTextFieldFocusChangeListener implements FocusListener {

		public void focusGained(FocusEvent arg0) {
		}

		public void focusLost(FocusEvent arg0) {

			String component = ((JTextField) arg0.getComponent()).toString();

			if (component.indexOf("tileSizeWidthTextField") > -1) {
				String input = tileSizeWidthTextField.getText();

				if (input.length() > 0) {

					int result = -1;
					result = Utilities.validateTileSizeInput(input);

					if (result == -1) {
						if (Integer.parseInt(input) < 50) {
							JOptionPane
									.showMessageDialog(
											null,
											"\""
													+ input
													+ "\" is not valid input of Custom size (W) \n\n"
													+ "Accepted values are between 50 and 1792",
											"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			if (component.indexOf("tileSizeHeightTextField") > -1) {
				String input = tileSizeHeightTextField.getText();

				if (input.length() > 0) {

					int result = -1;
					result = Utilities.validateTileSizeInput(input);

					if (result == -1) {
						if (Integer.parseInt(input) < 50) {
							JOptionPane
									.showMessageDialog(
											null,
											"\""
													+ input
													+ "\" is not valid input of Custom size (H) \n\nAccepted values are between 50 and 1792",
											"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		}
	}

	private boolean handleInputInRealTime() {

		String input = "";

		if ((((this.getFocusOwner()).getClass()).toString())
				.equals("class javax.swing.JTextField")) {

			input = ((JTextField) this.getFocusOwner()).getText();

			if (input.length() > 0) {

				String jtfobjAsString = ((JTextField) this.getFocusOwner())
						.toString();

				if (jtfobjAsString.indexOf("latMaxTextField") > -1
						|| jtfobjAsString.indexOf("latMinTextField") > -1
						|| jtfobjAsString.indexOf("longMaxTextField") > -1
						|| jtfobjAsString.indexOf("longMinTextField") > -1) {

					String result = "";

					result = Utilities.validateCordinateInput(input,
							jtfobjAsString);

					if (result.length() > 0) {
						JOptionPane.showMessageDialog(null, "\"" + result
								+ "\" is not valid input.\n"
								+ "Please enter a valid number in the form "
								+ Utilities.FORMAT_6_DEC.format(1.12345),
								"Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}

				if (jtfobjAsString.indexOf("tileSizeWidthTextField") > -1) {

					int result = -1;

					result = Utilities.validateTileSizeInput(input);

					if (result > -1) {
						JOptionPane
								.showMessageDialog(
										null,
										"\""
												+ (char) result
												+ "\" is not valid input \n\nOnly accepted input is \"0-9\"",
										"Error", JOptionPane.ERROR_MESSAGE);
						return false;
					} else if (input.length() > 3) {
						if (Integer.parseInt(input) > 1792) {
							JOptionPane
									.showMessageDialog(
											null,
											"\""
													+ input
													+ "\" is not valid input \n\nOnly accepted input is values between 50 and 1792",
											"Error", JOptionPane.ERROR_MESSAGE);
							return false;
						}
					}
				}

				if (jtfobjAsString.indexOf("tileSizeHeightTextField") > -1) {

					int result = -1;

					result = Utilities.validateTileSizeInput(input);

					if (result > -1) {
						JOptionPane
								.showMessageDialog(
										null,
										"\""
												+ (char) result
												+ "\" is not valid input \n\nOnly accepted input is \"0-9\"",
										"Error", JOptionPane.ERROR_MESSAGE);
						return false;
					} else if (input.length() > 3) {
						if (Integer.parseInt(input) > 1792) {
							JOptionPane
									.showMessageDialog(
											null,
											"\""
													+ input
													+ "\" is not valid input \n\nOnly accepted input are values between 50 and 1792",
											"Error", JOptionPane.ERROR_MESSAGE);
							return false;
						}

					}
				}

				if (jtfobjAsString.indexOf("atlasNameTextField") > -1) {

					int result = -1;

					result = Utilities.validateString(input, false);

					if (result > -1) {
						JOptionPane.showMessageDialog(null, "\""
								+ (char) result + "\" is not valid input",
								"Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean retryDownloadAtlasTile(int xValue, int yValue,
			int zoomValue, File destinationFolder, int serverSwitcher) {

		boolean retryOk = false;

		for (int i = 0; i < 10; i++) {

			if (serverSwitcher == 4) {
				serverSwitcher = 0;
			}
			try {
				GoogleTileDownLoad.getImage(xValue, yValue, zoomValue,
						destinationFolder, serverSwitcher, true);
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

	public boolean retryDownloadPreviewTile(int xValue, int yValue,
			int zoomValue, File destinationFolder, int serverSwitcher) {

		boolean retryOk = false;

		for (int i = 0; i < 10; i++) {

			if (serverSwitcher == 4) {
				serverSwitcher = 0;
			}

			try {
				GoogleTileDownLoad.getImage(xValue, yValue, zoomValue,
						destinationFolder, serverSwitcher, true);
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

	public SelectedZoomLevels getSelectedZoomlevels() {

		SelectedZoomLevels sZL = new SelectedZoomLevels();

		sZL.setZoomLevelSelected(1, cbOne.isSelected());
		sZL.setZoomLevelSelected(2, cbTwo.isSelected());
		sZL.setZoomLevelSelected(3, cbThree.isSelected());
		sZL.setZoomLevelSelected(4, cbFour.isSelected());
		sZL.setZoomLevelSelected(5, cbFive.isSelected());
		sZL.setZoomLevelSelected(6, cbSix.isSelected());
		sZL.setZoomLevelSelected(7, cbSeven.isSelected());
		sZL.setZoomLevelSelected(8, cbEight.isSelected());
		sZL.setZoomLevelSelected(9, cbNine.isSelected());
		sZL.setZoomLevelSelected(10, cbTen.isSelected());

		sZL.sort();
		return sZL;
	}

	private class JPopupMenuListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			double[] coordinates = Utilities.calculatePreviewCoordinates(
					ProcessValues.getMouseXCoordinat(), ProcessValues
							.getMouseYCoordinat());

			double selectedLongMin = coordinates[0];
			double latMaxApproximitation = coordinates[1];

			String actionCommand = e.getActionCommand();

			if (actionCommand.equals("Top left corner")) {

				latMaxTextField.setText(Utilities.nrOfDecimals(
						latMaxApproximitation, 6));
				longMinTextField.setText(Utilities.nrOfDecimals(
						selectedLongMin, 6));
				jpm.setVisible(false);
			} else if (actionCommand.equals("Bottom right corner")) {
				latMinTextField.setText(Utilities.nrOfDecimals(
						latMaxApproximitation, 6));
				longMaxTextField.setText(Utilities.nrOfDecimals(
						selectedLongMin, 6));
				jpm.setVisible(false);
			}
		}
	}

	private void createAtlas() {
		{

			String errorText = validateInput(true);
			if (errorText.length() > 0) {
				JOptionPane.showMessageDialog(null, errorText, "Errors",
						JOptionPane.ERROR_MESSAGE);
			} else {

				boolean maxIsBiggerThanMin = true;

				maxIsBiggerThanMin = validateLatLongMinMax();

				if (maxIsBiggerThanMin) {

					boolean customTileSizeWidthIsOk = true;
					boolean customTileSizeHeightIsOk = true;

					if (tileSizeWidthComboBox.isEnabled() == false) {

						try {
							Integer.parseInt(tileSizeWidthTextField.getText());
						} catch (NumberFormatException nfex) {

							customTileSizeWidthIsOk = false;
							JOptionPane
									.showMessageDialog(
											null,
											"Custom tile size width value is not a valid integer value",
											"Errors", JOptionPane.ERROR_MESSAGE);
						}
					}
					if (tileSizeHeightComboBox.isEnabled() == false) {

						try {
							Integer.parseInt(tileSizeHeightTextField.getText());
						} catch (NumberFormatException nfex) {

							customTileSizeHeightIsOk = false;
							JOptionPane
									.showMessageDialog(
											null,
											"Custom tile size height value is not a valid integer value",
											"Errors", JOptionPane.ERROR_MESSAGE);
						}
					}
					if (customTileSizeWidthIsOk && customTileSizeHeightIsOk) {
						createAtlasButton.setEnabled(false);

						try {
							atlasThread = new AtlasThread();
							atlasThread.start();
						} catch (Exception ex) {
							System.out.println(ex);
						}
					}
				}
			}
			System.gc();
		}
	}

	private class AtlasThread extends Thread {

		public void run() {

			String workingDir = System.getProperty("user.dir");

			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String formattedDateString = sdf.format(date);

			File ozi = new File(workingDir + fileSeparator + "ozi"
					+ fileSeparator + formattedDateString);

			// Check whether preview folder exists
			// or not...
			if (!ozi.exists()) {
				ozi.mkdir();
			}

			/***
			 * In this section of code below, atlas is created.
			 **/

			File atlas = new File(workingDir + fileSeparator + "atlases"
					+ fileSeparator + formattedDateString);

			// Check whether preview folder exists
			// or not...
			if (!atlas.exists()) {
				// Create if not already existing
				atlas.mkdir();
			}

			File atlasTar = new File(workingDir + fileSeparator
					+ "atlasestared" + fileSeparator + formattedDateString);

			// Check whether preview folder exists
			// or not...
			if (!atlasTar.exists()) {
				// Create if not already existing
				atlasTar.mkdir();
			}

			File crtba = new File(atlas.getAbsolutePath() + fileSeparator
					+ "cr.tba");

			try {
				FileWriter fw = new FileWriter(crtba);
				fw.write("Atlas 1.0\r\n");
				fw.close();
			} catch (IOException iox) {
				System.out.println(iox);
			}

			SelectedZoomLevels sZL = getSelectedZoomlevels();

			int nrOfLayers = sZL.getNrOfLayers();
			int[] zoomLevels = sZL.getZoomLevels();

			ProcessValues.resetNrOfDownloadedBytes();

			int totalNrOfTiles = 0;

			for (int i = 0; i < nrOfLayers; i++) {

				Point topLeft = GoogleTileUtils
						.toTileXY(
								Double.parseDouble(latMaxTextField.getText()),
								Double.parseDouble(longMaxTextField.getText()),
								zoomLevels[i]);
				Point bottomRight = GoogleTileUtils
						.toTileXY(
								Double.parseDouble(latMinTextField.getText()),
								Double.parseDouble(longMinTextField.getText()),
								zoomLevels[i]);

				totalNrOfTiles = totalNrOfTiles
						+ Utilities.calculateNrOfTiles(new TileXYMinMaxAndZoom(
								topLeft, bottomRight, zoomLevels[i]));
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

					String mapName = atlasNameTextField.getText().trim();

					Point topLeft = GoogleTileUtils.toTileXY(Double
							.parseDouble(latMaxTextField.getText()), Double
							.parseDouble(longMaxTextField.getText()), zoom);
					Point bottomRight = GoogleTileUtils.toTileXY(Double
							.parseDouble(latMinTextField.getText()), Double
							.parseDouble(longMinTextField.getText()), zoom);

					int apMax = Utilities
							.calculateNrOfTiles(new TileXYMinMaxAndZoom(
									topLeft, bottomRight, zoom));

					ap.setMinMaxForCurrentLayer(0, apMax);
					ap.setZoomLevel(zoom);
					ap.setInitiateTimeForLayer();

					int xMax = (int) topLeft.getX();
					int xMin = (int) bottomRight.getX();
					int yMax = (int) bottomRight.getY();
					int yMin = (int) topLeft.getY();

					int serverSwitcher = 0;
					int counter = 0;

					File oziZoom = new File(ozi + fileSeparator + zoom);
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

										GoogleTileDownLoad.getImage(j, i, zoom,
												oziZoom, serverSwitcher, true);
									} catch (IOException e) {

										boolean retryOK;

										retryOK = retryDownloadAtlasTile(j, i,
												zoom, oziZoom, serverSwitcher);

										if (retryOK == false) {
											JOptionPane
													.showMessageDialog(
															null,
															"Something is wrong with connection to download server. Please check connection to internet and try again",
															"Error",
															JOptionPane.ERROR_MESSAGE);
											System.exit(1);
										}
									}
									serverSwitcher++;
									counter++;

									ap.updateAtlasProgressBar(ap
											.getAtlasProgressValue() + 1);
									ap.updateLayerProgressBar(counter);
									ap.updateViewNrOfDownloadedBytes();
									ap.updateViewNrOfDownloadedBytesPerSecond();
									ap.updateTotalDownloadTime();

								}
							}
						}
					}

					if ((oziZoom.list().length) != ((yMax - yMin + 1) * (xMax
							- xMin + 1))) {
						JOptionPane
								.showMessageDialog(
										null,
										"Something is wrong with download of atlas tiles. Actual amount of downoladed tiles is not the same as the supposed amount of tiles downoladed. It might be connection problems to internet or something else. Please try again.",
										"Error", JOptionPane.ERROR_MESSAGE);
						System.exit(1);
					}

					File atlasFolder = new File(atlas.getAbsolutePath()
							+ fileSeparator + mapName + zoom);
					atlasFolder.mkdir();

					int tileSizeWidth = 0;
					int tileSizeHeight = 0;

					if (tileSizeWidthComboBox.isEnabled()) {
						tileSizeWidth = Integer.parseInt(tileSizeWidthComboBox
								.getSelectedItem().toString());
					} else {
						tileSizeWidth = Integer.parseInt(tileSizeWidthTextField
								.getText());
					}
					if (tileSizeHeightComboBox.isEnabled()) {
						tileSizeHeight = Integer
								.parseInt(tileSizeHeightComboBox
										.getSelectedItem().toString());
					} else {
						tileSizeHeight = Integer
								.parseInt(tileSizeHeightTextField.getText());
					}

					OziToAtlas ota = new OziToAtlas(oziZoom, atlasFolder,
							tileSizeWidth, tileSizeHeight, mapName, zoom);
					ota.convert(xMax, xMin, yMax, yMin);

					ap.updateAtlasProgressBarLayerText(layer + 1);
				}
			}

			if (ProcessValues.getAbortAtlasDownload()) {
				JOptionPane.showMessageDialog(null, "Atlas download aborted",
						"Information", JOptionPane.INFORMATION_MESSAGE);
				ap.setButtonText();
			} else {
				ap.setButtonText();

				Utilities.createCR_TAR(atlas, atlasTar, new File(workingDir
						+ fileSeparator + "tarwrkdir"));
				AtlasProgress ap = AtlasProgress.getInstance();
				ap.updateTarPrograssBar();
				Utilities.createTarPackedLayers(atlas, atlasTar);
				ap.updateTarPrograssBar();

				JOptionPane.showMessageDialog(null, "Atlas download completed",
						"Information", JOptionPane.INFORMATION_MESSAGE);
			}
			createAtlasButton.setEnabled(true);
			ProcessValues.setAbortAtlasDownload(false);
		}
	}

	private void saveAsProfile() {

		String inputValue = JOptionPane.showInputDialog("Profile Name");

		if (inputValue != null) {

			Profile theProfile = new Profile();

			boolean profileNameExists = false;

			for (int i = 0; i < profileNamesVector.size(); i++) {
				if (inputValue.equals(profileNamesVector.elementAt(i))) {
					profileNameExists = true;
					break;
				}
			}

			if (profileNameExists) {
				JOptionPane.showMessageDialog(null,
						"Profile name already exists, choose a different name",
						"Error", JOptionPane.ERROR_MESSAGE);
			} else {
				String errorDescription = "";

				try {
					latMax = Double.parseDouble(latMaxTextField.getText());
				} catch (NumberFormatException nfex) {
					errorDescription = "Invalid format of \"Latitude Max\" value\n";
				}
				try {
					latMin = Double.parseDouble(latMinTextField.getText());
				} catch (NumberFormatException nfex) {
					errorDescription += "Invalid format of \"Latitude Min\" value\n";
				}
				try {
					longMax = Double.parseDouble(longMaxTextField.getText());
				} catch (NumberFormatException nfex) {
					errorDescription += "Invalid format of \"Longitude Max\" value\n";
				}
				try {
					longMin = Double.parseDouble(longMinTextField.getText());
				} catch (NumberFormatException nfex) {
					errorDescription += "Invalid format of \"Longitude Min\" value\n";
				}

				if (!tileSizeWidthComboBox.isEnabled()) {
					try {
						Integer.parseInt(tileSizeWidthTextField.getText());
					} catch (NumberFormatException nfex) {
						errorDescription += "Invalid format of \"Custom size\" (TILE SIZE) value\n";
					}
				}

				if (!tileSizeHeightComboBox.isEnabled()) {
					try {
						Integer.parseInt(tileSizeHeightTextField.getText());
					} catch (NumberFormatException nfex) {
						errorDescription += "Invalid format of \"Custom size\" (TILE SIZE) value\n";
					}
				}

				if (errorDescription.equals("")) {

					theProfile.setProfileName(inputValue);
					theProfile.setAtlasName(atlasNameTextField.getText());
					theProfile.setLatitudeMax(Double
							.parseDouble(latMaxTextField.getText()));
					theProfile.setLatitudeMin(Double
							.parseDouble(latMinTextField.getText()));
					theProfile.setLongitudeMax(Double
							.parseDouble(longMaxTextField.getText()));
					theProfile.setLongitudeMin(Double
							.parseDouble(longMinTextField.getText()));

					boolean[] zoomLevels = new boolean[10];
					zoomLevels[0] = cbOne.isSelected();
					zoomLevels[1] = cbTwo.isSelected();
					zoomLevels[2] = cbThree.isSelected();
					zoomLevels[3] = cbFour.isSelected();
					zoomLevels[4] = cbFive.isSelected();
					zoomLevels[5] = cbSix.isSelected();
					zoomLevels[6] = cbSeven.isSelected();
					zoomLevels[7] = cbEight.isSelected();
					zoomLevels[8] = cbNine.isSelected();
					zoomLevels[9] = cbTen.isSelected();

					theProfile.setZoomLevels(zoomLevels);
					theProfile.setTileSizeWidth(tileSizeWidthComboBox
							.getSelectedIndex());
					theProfile.setTileSizeHeight(tileSizeHeightComboBox
							.getSelectedIndex());

					if (!tileSizeWidthComboBox.isEnabled()) {
						theProfile.setCustomTileSizeWidth(Integer
								.parseInt(tileSizeWidthTextField.getText()));
					} else {
						theProfile.setCustomTileSizeWidth(0);
					}

					if (!tileSizeHeightComboBox.isEnabled()) {
						theProfile.setCustomTileSizeHeight(Integer
								.parseInt(tileSizeHeightTextField.getText()));
					} else {
						theProfile.setCustomTileSizeHeight(0);
					}

					profilesVector.addElement(theProfile);
					PersistentProfiles.store(profilesVector);
					initiateProgram();
				} else {
					JOptionPane.showMessageDialog(null, errorDescription,
							"Errors", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void deleteProfile() {

		if (profilesJList.isEnabled() && profilesJList.getSelectedIndex() > -1) {

			profilesVector.removeElementAt(profilesJList.getSelectedIndex());

			PersistentProfiles.store(profilesVector);
			initiateProgram();
		}
	}

	private void preview() {
		String errorText = validateInput(false);

		if (errorText.length() > 0) {

			JOptionPane.showMessageDialog(null, errorText, "Errors",
					JOptionPane.ERROR_MESSAGE);
		} else {

			boolean maxIsBiggerThanMin = true;

			maxIsBiggerThanMin = validateLatLongMinMax();

			if (maxIsBiggerThanMin) {

				System.gc();

				previewPanel.removeAll();
				previewPanel.updateUI();

				System.gc();

				String workingDir = System.getProperty("user.dir");

				preview = new File(workingDir + fileSeparator + "preview");
				overlay = new File(preview + fileSeparator + "overlay");

				// Check whether preview folder exists or not...
				if (preview.exists()) {

					// ...if it exists check if there is any files in it
					File[] files = preview.listFiles();

					// ...if so, delete all of them
					if (files.length > 0) {

						for (int i = 0; i < files.length; i++) {
							if (!files[i].isDirectory()) {
								files[i].delete();
							}
						}
					}
				} else {
					preview.mkdir();
				}

				// Check whether overlay folder exists or not...
				if (overlay.exists()) {

					// ...if it exists check if there is any files in it
					File[] files = overlay.listFiles();

					// ...if so, delete all of them
					if (files.length > 0) {

						boolean deleted = false;

						for (int i = 0; i < files.length; i++) {
							while (!deleted) {
								deleted = files[i].delete();
							}
							deleted = false;
						}
					}
				} else {
					overlay.mkdir();
				}

				latMax = Double.parseDouble(latMaxTextField.getText());
				latMin = Double.parseDouble(latMinTextField.getText());
				longMax = Double.parseDouble(longMaxTextField.getText());
				longMin = Double.parseDouble(longMinTextField.getText());

				tXY = GoogleTileUtils.getTileXYMinMax(latMax, longMax, latMin,
						longMin);

				ProcessValues.setPreviewLatMaxTile(tXY.getYMax());
				ProcessValues.setPreviewLatMinTile(tXY.getYMin());
				ProcessValues.setPreviewLongMaxTile(tXY.getXMax());
				ProcessValues.setPreviewLongMinTile(tXY.getXMin());

				Rectangle2D.Double rect = null;

				// Get the lat max coord
				rect = GoogleTileUtils.getTileRect(tXY.getXMax(),
						tXY.getYMin(), tXY.getZoom());
				ProcessValues.setPreviewLatMaxCoord(rect.getMaxY());

				// System.out.println("rect.getMaxY() : " +
				// rect.getMaxY());
				// System.out.println("rect.getMinY() : " +
				// rect.getMinY());

				// Get the lat min coord
				rect = GoogleTileUtils.getTileRect(tXY.getXMax(),
						tXY.getYMax(), tXY.getZoom());
				ProcessValues.setPreviewLatMinCoord(rect.getMinY());

				// Get the long max coord
				rect = GoogleTileUtils.getTileRect(tXY.getXMax(),
						tXY.getYMin(), tXY.getZoom());
				ProcessValues.setPreviewLongMaxCoord(rect.getMaxX());

				// Get the long min coord
				rect = GoogleTileUtils.getTileRect(tXY.getXMin(),
						tXY.getYMin(), tXY.getZoom());
				ProcessValues.setPreviewLongMinCoord(rect.getMinX());

				// set the zoomlevel
				ProcessValues.setPreviewZoomValue(tXY.getZoom());

				// System.out.println(
				// "ProcessValues.getPreviewLatMaxCoord : " +
				// ProcessValues.getPreviewLatMaxCoord());
				// System.out.println(
				// "ProcessValues.getPreviewLatMinCoord : " +
				// ProcessValues.getPreviewLatMinCoord());
				// System.out.println(
				// "ProcessValues.getPreviewLongMaxCoord : " +
				// ProcessValues.getPreviewLongMaxCoord());
				// System.out.println(
				// "ProcessValues.getPreviewLongMinCoord : " +
				// ProcessValues.getPreviewLongMinCoord());

				pb = new PreviewProgressBar(0, Utilities
						.calculateNrOfTiles(tXY));
				pb.setVisible(true);

				downloadThread = new Thread() {

					public void run() {

						int serverSwitcher = 0;
						int counter = 0;

						for (int i = tXY.getYMin(); i <= tXY.getYMax(); i++) {
							for (int j = tXY.getXMin(); j <= tXY.getXMax(); j++) {

								if (serverSwitcher == 4) {
									serverSwitcher = 0;
								}
								try {

									GoogleTileDownLoad.getImage(j, i, tXY
											.getZoom(), preview,
											serverSwitcher, false);
								} catch (IOException e) {

									boolean retryOK;

									retryOK = retryDownloadPreviewTile(j, i,
											tXY.getZoom(), preview,
											serverSwitcher);

									if (retryOK == false) {
										JOptionPane
												.showMessageDialog(
														null,
														"Something is wrong with connection to download server. Please check connection to internet and try again",
														"Error",
														JOptionPane.ERROR_MESSAGE);
										System.exit(1);
									}
								}
								serverSwitcher++;

								counter++;

								pb.updateProgressBar(counter);
							}
						}

						pb.dispose();

						// ...list files
						File[] files = preview.listFiles();

						files = preview.listFiles();

						String xValue = "";
						String yValue = "";
						String fileName;

						int maxXValue = -2147483648;
						int minXValue = 2147483647;
						int maxYValue = -2147483648;
						int minYValue = 2147483647;

						int nrOfXValues = 0;
						int nrOfYValues = 0;

						if (files.length > 0) {

							// Hitta min och maxvärden för x och y, för
							// att kunna
							// rendera den totala bilden i rätt ordning
							for (int i = 0; i < files.length; i++) {

								if (!files[i].isDirectory()) {

									int intYValue = Integer.parseInt(files[i]
											.getName().substring(
													1,
													files[i].getName().indexOf(
															"x")));
									int intXValue = Integer
											.parseInt(files[i]
													.getName()
													.substring(
															files[i]
																	.getName()
																	.indexOf(
																			"x") + 1,
															files[i].getName()
																	.length() - 4));

									if (intYValue < minYValue) {
										minYValue = intYValue;
									}

									if (intYValue > maxYValue) {
										maxYValue = intYValue;
									}

									if (intXValue < minXValue) {
										minXValue = intXValue;
									}

									if (intXValue > maxXValue) {
										maxXValue = intXValue;
									}
								}
							}

							fileName = files[1].getName();

							// För att få ett startvärde på första
							// filens x och y värde
							yValue = fileName.substring(1, fileName
									.indexOf("x"));
							xValue = fileName.substring(
									fileName.indexOf("x") + 1, fileName
											.length() - 4);

							nrOfXValues = 0;

							// Iterera igenom alla filer för att
							// kontrollera hur många rader det finns i
							// den
							// hämtade "kartarrayen"
							for (int i = 0; i < files.length; i++) {
								if (!files[i].isDirectory()) {

									if (((files[i].getName()).substring(1,
											fileName.indexOf("x"))
											.equals(yValue))) {
										nrOfXValues = nrOfXValues + 1;
									}
								}
							}
						}

						nrOfYValues = (files.length - 1) / nrOfXValues;

						previewLayout.setRows(nrOfYValues);
						previewLayout.setColumns(nrOfXValues);

						for (int i = minYValue; i <= maxYValue; i++) {

							for (int j = minXValue; j <= maxXValue; j++) {

								try {
									BufferedImage image = ImageIO
											.read(new File(System
													.getProperty("user.dir")
													+ fileSeparator
													+ "preview"
													+ fileSeparator
													+ "y"
													+ i
													+ "x" + j + ".png"));
									ImageIO
											.write(
													image,
													"jpg",
													new FileOutputStream(
															new File(
																	System
																			.getProperty("user.dir")
																			+ fileSeparator
																			+ "preview"
																			+ fileSeparator
																			+ "overlay"
																			+ fileSeparator
																			+ "y"
																			+ i
																			+ "x"
																			+ j
																			+ ".jpg")));
								} catch (IOException iox) {
									System.out.println(iox);
								}

								// 2 Rows
								if (nrOfYValues == 2
										&& (nrOfXValues > 2 || nrOfXValues < 2)) {

									// Left top corner
									if (i == minYValue && j == minXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();
											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY(),
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY(), 1,
													256 - (int) theOffset
															.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect((int) theOffset
													.getX() + 1,
													(int) theOffset.getY() + 1,
													256 - (int) theOffset
															.getX() - 1,
													256 - (int) theOffset
															.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Top row not a corner
									if (i == minYValue
											&& !(j == minXValue || j == maxXValue)) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY(),
													256, 1);

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0,
													(int) theOffset.getY() + 1,
													256, 256 - (int) theOffset
															.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Right top corner
									if (i == minYValue && j == maxXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMax, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY(),
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect(
													256 - (int) theOffset
															.getX() - 1,
													(int) theOffset.getY(), 1,
													256 - (int) theOffset
															.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0,
													(int) theOffset.getY() + 1,
													256 - (int) theOffset
															.getX() - 1,
													256 - (int) theOffset
															.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Left bottom corner
									if (i == maxYValue && j == minXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();
											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY() - 1,
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect((int) theOffset
													.getX(), 0, 1,
													(int) theOffset.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect((int) theOffset
													.getX() + 1, 0,
													256 - (int) theOffset
															.getX() - 1,
													(int) theOffset.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Bottom row not a corner
									if (i == maxYValue
											&& !(j == minXValue || j == maxXValue)) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY() - 1,
													256, 1);

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0, 0, 256,
													(int) theOffset.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Right bottom corner
									if (i == maxYValue && j == maxXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMax, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY() - 1,
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect(
													256 - (int) theOffset
															.getX() - 1, 0, 1,
													(int) theOffset.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0, 0,
													256 - (int) theOffset
															.getX() - 1,
													(int) theOffset.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									JLabel temp = new JLabel(new ImageIcon(
											System.getProperty("user.dir")
													+ fileSeparator + "preview"
													+ fileSeparator + "overlay"
													+ fileSeparator + "y" + i
													+ "x" + j + ".png"));

									// Row 1
									if (i == minYValue) {
										temp
												.setVerticalAlignment(JLabel.BOTTOM);
										previewPanel.add(temp);
										previewPanel.updateUI();
									}
									// Row 2
									else {
										temp.setVerticalAlignment(JLabel.TOP);
										previewPanel.add(temp);
										previewPanel.updateUI();
									}
								}

								// 2 Columns
								if (nrOfXValues == 2
										&& (nrOfYValues > 2 || nrOfYValues < 2)) {

									// Left top corner
									if (i == minYValue && j == minXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();
											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY(),
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY(), 1,
													256 - (int) theOffset
															.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect((int) theOffset
													.getX() + 1,
													(int) theOffset.getY() + 1,
													256 - (int) theOffset
															.getX() - 1,
													256 - (int) theOffset
															.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Left column not a corner
									if (!(i == minYValue || i == maxYValue)
											&& j == minXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX(), 0, 1, 256);

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect((int) theOffset
													.getX() + 1, 0,
													256 - (int) theOffset
															.getX() - 1, 256);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Right top corner
									if (i == minYValue && j == maxXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMax, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY(),
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect(
													256 - (int) theOffset
															.getX() - 1,
													(int) theOffset.getY(), 1,
													256 - (int) theOffset
															.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0,
													(int) theOffset.getY() + 1,
													256 - (int) theOffset
															.getX() - 1,
													256 - (int) theOffset
															.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Left bottom corner
									if (i == maxYValue && j == minXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();
											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY() - 1,
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect((int) theOffset
													.getX(), 0, 1,
													(int) theOffset.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect((int) theOffset
													.getX() + 1, 0,
													256 - (int) theOffset
															.getX() - 1,
													(int) theOffset.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Right column not a corner
									if (!(i == minYValue || i == maxYValue)
											&& j == maxXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMax, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(
													256 - (int) theOffset
															.getX() - 1, 0, 1,
													256);

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0, 0,
													256 - (int) theOffset
															.getX() - 1, 256);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Right bottom corner
									if (i == maxYValue && j == maxXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMax, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY() - 1,
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect(
													256 - (int) theOffset
															.getX() - 1, 0, 1,
													(int) theOffset.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0, 0,
													256 - (int) theOffset
															.getX() - 1,
													(int) theOffset.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									JLabel temp = new JLabel(new ImageIcon(
											System.getProperty("user.dir")
													+ fileSeparator + "preview"
													+ fileSeparator + "overlay"
													+ fileSeparator + "y" + i
													+ "x" + j + ".png"));

									// Column 1
									if (j == minXValue) {
										temp
												.setHorizontalAlignment(JLabel.RIGHT);
										previewPanel.add(temp);
										previewPanel.updateUI();
									}

									// Column 2
									else {
										temp
												.setHorizontalAlignment(JLabel.LEFT);
										previewPanel.add(temp);
										previewPanel.updateUI();
									}
								}

								// 2 Rows & 2 Columns
								if (nrOfXValues == 2 && nrOfYValues == 2) {

									// Left top corner
									if (i == minYValue && j == minXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();
											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY(),
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY(), 1,
													256 - (int) theOffset
															.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect((int) theOffset
													.getX() + 1,
													(int) theOffset.getY() + 1,
													256 - (int) theOffset
															.getX() - 1,
													256 - (int) theOffset
															.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Right top corner
									if (i == minYValue && j == maxXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMax, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY(),
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect(
													256 - (int) theOffset
															.getX() - 1,
													(int) theOffset.getY(), 1,
													256 - (int) theOffset
															.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0,
													(int) theOffset.getY() + 1,
													256 - (int) theOffset
															.getX() - 1,
													256 - (int) theOffset
															.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Left bottom corner
									if (i == maxYValue && j == minXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();
											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY() - 1,
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect((int) theOffset
													.getX(), 0, 1,
													(int) theOffset.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect((int) theOffset
													.getX() + 1, 0,
													256 - (int) theOffset
															.getX() - 1,
													(int) theOffset.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Right bottom corner
									if (i == maxYValue && j == maxXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMax, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY() - 1,
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect(
													256 - (int) theOffset
															.getX() - 1, 0, 1,
													(int) theOffset.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0, 0,
													256 - (int) theOffset
															.getX() - 1,
													(int) theOffset.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									JLabel temp = new JLabel(new ImageIcon(
											System.getProperty("user.dir")
													+ fileSeparator + "preview"
													+ fileSeparator + "overlay"
													+ fileSeparator + "y" + i
													+ "x" + j + ".png"));

									if (i == minYValue && j == minXValue) {
										temp
												.setVerticalAlignment(JLabel.BOTTOM);
										temp
												.setHorizontalAlignment(JLabel.RIGHT);
										previewPanel.add(temp);
										previewPanel.updateUI();
									}
									if (i == minYValue && j == maxXValue) {
										temp
												.setVerticalAlignment(JLabel.BOTTOM);
										temp
												.setHorizontalAlignment(JLabel.LEFT);
										previewPanel.add(temp);
										previewPanel.updateUI();
									}

									if (i == maxYValue && j == minXValue) {
										temp.setVerticalAlignment(JLabel.TOP);
										temp
												.setHorizontalAlignment(JLabel.RIGHT);
										previewPanel.add(temp);
										previewPanel.updateUI();
									}

									if (i == maxYValue && j == maxXValue) {
										temp.setVerticalAlignment(JLabel.TOP);
										temp
												.setHorizontalAlignment(JLabel.LEFT);
										previewPanel.add(temp);
										previewPanel.updateUI();
									}
								}

								if (nrOfYValues != 2 && nrOfXValues != 2) {

									// Left top corner
									if (i == minYValue && j == minXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();
											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY(),
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY(), 1,
													256 - (int) theOffset
															.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect((int) theOffset
													.getX() + 1,
													(int) theOffset.getY() + 1,
													256 - (int) theOffset
															.getX() - 1,
													256 - (int) theOffset
															.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Right top corner
									if (i == minYValue && j == maxXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMax, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY(),
													(int) theOffset.getX(), 1);
											graphics.fillRect((int) theOffset
													.getX() - 1,
													(int) theOffset.getY(), 1,
													256 - (int) theOffset
															.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0,
													(int) theOffset.getY() + 1,
													(int) theOffset.getX() - 1,
													256 - (int) theOffset
															.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Right column not a corner
									if (!(i == minYValue || i == maxYValue)
											&& j == maxXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMax, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX() - 1, 0, 1, 256);

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0, 0,
													(int) theOffset.getX() - 1,
													256);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Left column not a corner
									if (!(i == minYValue || i == maxYValue)
											&& j == minXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX(), 0, 1, 256);

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect((int) theOffset
													.getX() + 1, 0,
													256 - (int) theOffset
															.getX() - 1, 256);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Top row not a corner
									if (i == minYValue
											&& !(j == minXValue || j == maxXValue)) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMax,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY(),
													256, 1);

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0,
													(int) theOffset.getY() + 1,
													256, 256 - (int) theOffset
															.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Bottom row not a corner
									if (i == maxYValue
											&& !(j == minXValue || j == maxXValue)) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY() - 1,
													256, 1);

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0, 0, 256,
													(int) theOffset.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Inner tiles
									if (!(j == minXValue || j == maxXValue
											|| i == minYValue || i == maxYValue)) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0, 0, 256, 256);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Left bottom corner
									if (i == maxYValue && j == minXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMin, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();
											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect((int) theOffset
													.getX(), (int) theOffset
													.getY() - 1,
													256 - (int) theOffset
															.getX(), 1);
											graphics.fillRect((int) theOffset
													.getX(), 0, 1,
													(int) theOffset.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect((int) theOffset
													.getX() + 1, 0,
													256 - (int) theOffset
															.getX() - 1,
													(int) theOffset.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									// Right bottom corner
									if (i == maxYValue && j == maxXValue) {

										Point theOffset = GoogleTileUtils
												.getPixelOffsetInTile(latMin,
														longMax, tXY.getZoom());

										try {

											BufferedImage image = ImageIO
													.read(new File(
															System
																	.getProperty("user.dir")
																	+ fileSeparator
																	+ "preview"
																	+ fileSeparator
																	+ "overlay"
																	+ fileSeparator
																	+ "y"
																	+ i
																	+ "x"
																	+ j
																	+ ".jpg"));

											Graphics graphics = image
													.getGraphics();

											graphics.setColor(new Color(0, 0,
													0, 128));
											graphics.fillRect(0,
													(int) theOffset.getY() - 1,
													(int) theOffset.getX(), 1);
											graphics.fillRect((int) theOffset
													.getX() - 1, 0, 1,
													(int) theOffset.getY());

											graphics.setColor(new Color(0, 255,
													0, 64));
											graphics.fillRect(0, 0,
													(int) theOffset.getX() - 1,
													(int) theOffset.getY() - 1);
											graphics.dispose();

											ImageIO
													.write(
															image,
															"png",
															new FileOutputStream(
																	new File(
																			System
																					.getProperty("user.dir")
																					+ fileSeparator
																					+ "preview"
																					+ fileSeparator
																					+ "overlay"
																					+ fileSeparator
																					+ "y"
																					+ i
																					+ "x"
																					+ j
																					+ ".png")));
										} catch (IOException iox) {
											System.out.println(iox);
										}
									}

									JLabel temp = new JLabel(new ImageIcon(
											System.getProperty("user.dir")
													+ fileSeparator + "preview"
													+ fileSeparator + "overlay"
													+ fileSeparator + "y" + i
													+ "x" + j + ".png"));
									previewPanel.add(temp);
									previewPanel.updateUI();
								}
							}
						}
					}
				};
				downloadThread.start();
			}
		}
	}

	// Knapplyssnarklass
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();

			if (actionCommand.equals("Create Atlas"))
				createAtlas();
			else if (actionCommand.equals("Save as profile"))
				saveAsProfile();
			else if (actionCommand.equals("Delete profile"))
				deleteProfile();
			else if (actionCommand.equals("Settings")) {
				SettingsGUI sgui = new SettingsGUI();
				sgui.setVisible(true);
			} else if (actionCommand.equals("Preview")) {
				preview();
			}
		}
	}

	// Knapplyssnarklass
	private class JToggleButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();

			if (actionCommand.equals("UNLOCK/LOCK")) {

				if (chooseProfileButton.isSelected()) {
					profilesJList.setEnabled(true);
				} else {
					profilesJList.setEnabled(false);
				}
			}
		}
	}

	private class JListListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			int selectedIndex = profilesJList.getSelectedIndex();

			if (selectedIndex != -1) {
				Profile temp = new Profile();

				temp = profilesVector.elementAt(selectedIndex);

				latMinTextField.setText(Double.toString(temp.getLatitudeMin()));
				latMaxTextField.setText(Double.toString(temp.getLatitudeMax()));
				longMinTextField.setText(Double
						.toString(temp.getLongitudeMin()));
				longMaxTextField.setText(Double
						.toString(temp.getLongitudeMax()));

				if (temp.getCustomTileSizeWidth() == 0) {
					tileSizeWidthTextField.setText("");
				} else {
					tileSizeWidthTextField.setText(Integer.toString(temp
							.getCustomTileSizeWidth()));
				}

				if (temp.getCustomTileSizeHeight() == 0) {
					tileSizeHeightTextField.setText("");
				} else {
					tileSizeHeightTextField.setText(Integer.toString(temp
							.getCustomTileSizeHeight()));
				}

				atlasNameTextField.setText(temp.getAtlasName());

				boolean[] zoomValues = new boolean[10];

				zoomValues = temp.getZoomLevels();

				cbOne.setSelected(zoomValues[0]);
				cbTwo.setSelected(zoomValues[1]);
				cbThree.setSelected(zoomValues[2]);
				cbFour.setSelected(zoomValues[3]);
				cbFive.setSelected(zoomValues[4]);
				cbSix.setSelected(zoomValues[5]);
				cbSeven.setSelected(zoomValues[6]);
				cbEight.setSelected(zoomValues[7]);
				cbNine.setSelected(zoomValues[8]);
				cbTen.setSelected(zoomValues[9]);

				int tileSizeWidth = temp.getTileSizeWidth();
				int indexWidth = (tileSizeWidth / 256) - 1;
				tileSizeWidthComboBox.setSelectedIndex(indexWidth);

				int tileSizeHeight = temp.getTileSizeHeight();
				int indexHeight = (tileSizeHeight / 256) - 1;
				tileSizeHeightComboBox.setSelectedIndex(indexHeight);

				/**
				 * Calculate amount of tiles to download
				 */
				calculateNrOfTilesToDownload();
			}
		}
	}

	private class JFrameListener extends WindowAdapter {

		Thread getDefaultMapThread;

		public void windowOpened(WindowEvent e) {

			getGoogleDownloadStringThread = new Thread() {

				public void run() {

					createAtlasButton.setText("Wait...");
					createAtlasButton.setEnabled(false);
					previewButton.setText("Wait...");
					previewButton.setEnabled(false);
					GoogleDownLoad.getDownloadString();
				}
			};
			getGoogleDownloadStringThread.start();

			getDefaultMapThread = new DefaultMapThread();
			getDefaultMapThread.start();
		}
	}

	private class DefaultMapThread extends Thread {

		public void run() {
			while (getGoogleDownloadStringThread.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			downloadThread = new DownloadThread();
			downloadThread.start();
		}
	}

	private class DownloadThread extends Thread {

		public void run() {
			/*
			 * tXY = GoogleTileUtils.getTileXYMinMax(85.0, 179.0, -85.0,
			 * -179.0);
			 * 
			 * ProcessValues.setPreviewLatMaxTile(tXY.getYMax());
			 * ProcessValues.setPreviewLatMinTile(tXY.getYMin());
			 * ProcessValues.setPreviewLongMaxTile(tXY.getXMax());
			 * ProcessValues.setPreviewLongMinTile(tXY.getXMin());
			 * ProcessValues.setPreviewLatMaxCoord(85.0);
			 * ProcessValues.setPreviewLatMinCoord(-85.0);
			 * ProcessValues.setPreviewLongMaxCoord(179.0);
			 * ProcessValues.setPreviewLongMinCoord(-179.0);
			 * ProcessValues.setPreviewZoomValue(tXY.getZoom());
			 * 
			 * String workingDir = System.getProperty("user.dir");
			 * 
			 * preview = new File(workingDir + fileSeparator + "preview");
			 * overlay = new File(preview + fileSeparator + "overlay");
			 * 
			 * // Check whether preview folder exists or not... if
			 * (preview.exists()) {
			 * 
			 * // ...if it exists check if there is any files // in it File[]
			 * files = preview.listFiles();
			 * 
			 * // ...if so, delete all of them if (files.length > 0) {
			 * 
			 * for (int i = 0; i < files.length; i++) { if
			 * (!files[i].isDirectory()) { files[i].delete(); } } } } else {
			 * preview.mkdir(); }
			 * 
			 * int serverSwitcher = 0; int counter = 0;
			 * 
			 * for (int i = tXY.getYMin(); i <= tXY.getYMax(); i++) { for (int j
			 * = tXY.getXMin(); j <= tXY.getXMax(); j++) {
			 * 
			 * if (serverSwitcher == 4) { serverSwitcher = 0; }
			 * 
			 * try {
			 * 
			 * GoogleTileDownLoad.getImage(j, i, tXY.getZoom(), preview,
			 * serverSwitcher, false); } catch (IOException e) {
			 * 
			 * boolean retryOK;
			 * 
			 * retryOK = retryDownloadPreviewTile(j, i, tXY.getZoom(), preview,
			 * serverSwitcher);
			 * 
			 * if (retryOK == false) { JOptionPane .showMessageDialog( null,
			 * "Something is wrong with connection to download server. Please check connection to internet and try again"
			 * , "Error", JOptionPane.ERROR_MESSAGE); System.exit(1); } }
			 * serverSwitcher++;
			 * 
			 * counter++; } }
			 * 
			 * // ...list files File[] files = preview.listFiles();
			 * 
			 * files = preview.listFiles();
			 * 
			 * String xValue = ""; String yValue = ""; String fileName;
			 * 
			 * int maxXValue = -2147483648; int minXValue = 2147483647; int
			 * maxYValue = -2147483648; int minYValue = 2147483647;
			 * 
			 * int nrOfXValues = 0; int nrOfYValues = 0;
			 * 
			 * if (files.length > 0) {
			 * 
			 * for (int i = 0; i < files.length; i++) {
			 * 
			 * if (!files[i].isDirectory()) {
			 * 
			 * int intYValue = Integer.parseInt(files[i].getName() .substring(1,
			 * files[i].getName().indexOf("x"))); int intXValue =
			 * Integer.parseInt(files[i].getName()
			 * .substring(files[i].getName().indexOf("x") + 1,
			 * files[i].getName().length() - 4));
			 * 
			 * if (intYValue < minYValue) { minYValue = intYValue; }
			 * 
			 * if (intYValue > maxYValue) { maxYValue = intYValue; }
			 * 
			 * if (intXValue < minXValue) { minXValue = intXValue; }
			 * 
			 * if (intXValue > maxXValue) { maxXValue = intXValue; } } }
			 * 
			 * fileName = files[1].getName();
			 * 
			 * yValue = fileName.substring(1, fileName.indexOf("x")); xValue =
			 * fileName.substring(fileName.indexOf("x") + 1, fileName .length()
			 * - 4);
			 * 
			 * nrOfXValues = 0;
			 * 
			 * for (int i = 0; i < files.length; i++) { if
			 * (!files[i].isDirectory()) {
			 * 
			 * if (((files[i].getName()).substring(1, fileName
			 * .indexOf("x")).equals(yValue))) { nrOfXValues = nrOfXValues + 1;
			 * } } } }
			 * 
			 * nrOfYValues = (files.length - 1) / nrOfXValues;
			 * 
			 * previewLayout.setRows(nrOfYValues);
			 * previewLayout.setColumns(nrOfXValues);
			 * 
			 * for (int i = minYValue; i <= maxYValue; i++) {
			 * 
			 * for (int j = minXValue; j <= maxXValue; j++) {
			 * 
			 * JLabel temp = new JLabel(new ImageIcon(System
			 * .getProperty("user.dir") + fileSeparator + "preview" +
			 * fileSeparator + "y" + i + "x" + j + ".png"));
			 * previewPanel.add(temp); previewPanel.updateUI(); } }
			 * previewButton.setText("Preview"); previewButton.setEnabled(true);
			 * createAtlasButton.setText("Create Atlas");
			 * createAtlasButton.setEnabled(true); //
			 * previewPanel.addMouseListener(new // PreviewMouseListener());
			 */
		}

	}

	private class PreviewMouseListener extends MouseAdapter {

		public void mouseClicked(MouseEvent me) {

			if (me.getButton() == 3) {

				jpm.setVisible(true);
				jpm.setLocation(me.getX()
						+ previewPanel.getLocationOnScreen().x, me.getY()
						+ previewPanel.getLocationOnScreen().y);

				int columns = previewLayout.getColumns();
				int rows = previewLayout.getRows();

				int width = previewPanel.getWidth();
				int height = previewPanel.getHeight();

				int offsetX = width - (columns * 256);
				int offsetY = height - (rows * 256);

				System.out.println("width: " + width);
				System.out.println("height: " + height);

				System.out.println("offsetX: " + offsetX);
				System.out.println("offsetY: " + offsetY);

				if (offsetX > 0) {
					ProcessValues.setMouseXCoordinat(me.getX() - (offsetX / 2));
				} else {
					ProcessValues.setMouseXCoordinat(me.getX());
				}

				if (offsetY > 0) {
					ProcessValues.setMouseYCoordinat(me.getY() - (offsetY / 2));
				} else {
					ProcessValues.setMouseYCoordinat(me.getY());
				}

				double borderTileLatMax = GoogleTileUtils
						.getTileBorderCordinate(ProcessValues
								.getPreviewLatMaxCoord(), ProcessValues
								.getPreviewLongMaxCoord(), ProcessValues
								.getPreviewZoomValue(), "N");
				double borderTileLongMax = GoogleTileUtils
						.getTileBorderCordinate(ProcessValues
								.getPreviewLatMaxCoord(), ProcessValues
								.getPreviewLongMaxCoord(), ProcessValues
								.getPreviewZoomValue(), "E");

				double borderTileLatMin = GoogleTileUtils
						.getTileBorderCordinate(ProcessValues
								.getPreviewLatMinCoord(), ProcessValues
								.getPreviewLongMinCoord(), ProcessValues
								.getPreviewZoomValue(), "S");
				double borderTileLongMin = GoogleTileUtils
						.getTileBorderCordinate(ProcessValues
								.getPreviewLatMinCoord(), ProcessValues
								.getPreviewLongMinCoord(), ProcessValues
								.getPreviewZoomValue(), "W");

				double longDiff = borderTileLongMax - borderTileLongMin;
				double latDiff = borderTileLatMax - borderTileLatMin;

				System.out.println("longDiff: " + longDiff);
				System.out.println("latDiff: " + latDiff);

				ProcessValues
						.setPreviewXResolution(((((ProcessValues
								.getPreviewLongMaxTile()
								- ProcessValues.getPreviewLongMinTile() + 1)) * 256) / longDiff));
				ProcessValues
						.setPreviewYResolution(((((ProcessValues
								.getPreviewLatMaxTile()
								- ProcessValues.getPreviewLatMinTile() + 1)) * 256) / latDiff));
			}
		}

	}

	private class CheckBoxListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			calculateNrOfTilesToDownload();
		}
	}

	public void selectionChanged(java.awt.geom.Point2D.Double max,
			java.awt.geom.Point2D.Double min) {
		longMaxTextField.setText(Utilities.FORMAT_6_DEC.format(max.x));
		longMinTextField.setText(Utilities.FORMAT_6_DEC.format(min.x));
		latMaxTextField.setText(Utilities.FORMAT_6_DEC.format(max.y));
		latMinTextField.setText(Utilities.FORMAT_6_DEC.format(min.y));
	}
}