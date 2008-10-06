package tac.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;

import javax.swing.BorderFactory;
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
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.gui.preview.GoogleTileSource;
import tac.gui.preview.MapSelectionListener;
import tac.gui.preview.OpenStreetMapTileSource;
import tac.gui.preview.PreviewMap;
import tac.program.AtlasThread;
import tac.program.GoogleDownLoad;
import tac.program.GoogleTileDownLoad;
import tac.program.GoogleTileUtils;
import tac.program.MapSelection;
import tac.program.ProcessValues;
import tac.program.Profile;
import tac.program.SelectedZoomLevels;
import tac.program.Settings;
import tac.program.TileStore;
import tac.program.TileXYMinMaxAndZoom;
import tac.utilities.PersistentProfiles;
import tac.utilities.Utilities;

public class GUI extends JFrame implements MapSelectionListener {

	private static final long serialVersionUID = -8444942802691874960L;
	private static final String VERSION = "0.9 alpha";

	// public static final NumberFormat DF = new DecimalFormat("0.000000");

	private JPanel leftPanel;
	private JPanel rightPanel;
	private JPanel coordinatesPanel;
	private JPanel zoomLevelPanel;
	private JPanel tileSizePanel;
	private JPanel atlasNamePanel;
	private JPanel profilesPanel;

	private PreviewMap previewMap;

	private JButton createAtlasButton;
	private JButton deleteProfileButton;
	private JButton saveAsProfileButton;
	private JButton previewSelectionButton;
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
	private JTextField lonMinTextField;
	private JTextField lonMaxTextField;
	private JTextField tileSizeWidthTextField;
	private JTextField tileSizeHeightTextField;
	private JTextField atlasNameTextField;

	private JCheckBox[] cbZoom = new JCheckBox[18];

	private JComboBox tileSizeWidthComboBox;
	private JComboBox tileSizeHeightComboBox;
	private JComboBox mapSource;

	private Vector<Integer> tileSizeValues;
	private Vector<Profile> profilesVector;
	private Vector<String> profileNamesVector;

	private JList profilesJList;
	private Thread downloadThread;
	private Thread atlasThread;
	private Thread getGoogleDownloadStringThread;

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

		this.setTitle("TrekBuddy Atlas Creator v" + VERSION);

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
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		// ... if that is not possible, try to set the look and feel associated
		// to the system
		catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (UnsupportedLookAndFeelException e1) {
				System.out.println("The selection of look and feel was not possible, due to: "
						+ e1.toString());
				e1.printStackTrace();
			} catch (Exception e1) {
			}
		}
	}

	public void createLeftPanel() {

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 5, 2, 2);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setPreferredSize(new Dimension(280, 780));

		coordinatesLabel = new JLabel("COORDINATES");

		// Coordinates Panel

		coordinatesPanel = new JPanel(null);
		coordinatesPanel.setMinimumSize(new Dimension(270, 200));
		coordinatesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		latMaxLabel = new JLabel("Latitude Max");
		latMaxLabel.setBounds(112, 20, 100, 20);

		latMaxTextField = new JTextField();
		latMaxTextField.setBounds(88, 42, 100, 20);
		latMaxTextField.setActionCommand("latMaxTextField");

		longMinLabel = new JLabel("Longitude Min");
		longMinLabel.setBounds(20, 70, 100, 20);

		lonMinTextField = new JTextField();
		lonMinTextField.setBounds(5, 90, 100, 20);
		lonMinTextField.setActionCommand("longMinTextField");

		longMaxLabel = new JLabel("Longitude Max");
		longMaxLabel.setBounds(185, 70, 100, 20);

		lonMaxTextField = new JTextField();
		lonMaxTextField.setBounds(162, 90, 100, 20);
		lonMaxTextField.setActionCommand("longMaxTextField");

		latMinLabel = new JLabel("Latitude Min");
		latMinLabel.setBounds(112, 120, 100, 20);

		latMinTextField = new JTextField();
		latMinTextField.setBounds(88, 140, 100, 20);
		latMinTextField.setActionCommand("latMinTextField");

		previewSelectionButton = new JButton("Display selection");
		previewSelectionButton.setBounds(78, 170, 120, 20);

		coordinatesPanel.add(latMinLabel);
		coordinatesPanel.add(latMaxLabel);
		coordinatesPanel.add(longMinLabel);
		coordinatesPanel.add(longMaxLabel);

		coordinatesPanel.add(latMinTextField);
		coordinatesPanel.add(latMaxTextField);
		coordinatesPanel.add(lonMinTextField);
		coordinatesPanel.add(lonMaxTextField);

		coordinatesPanel.add(previewSelectionButton);

		mapSource =
				new JComboBox(new Object[] { new GoogleTileSource.GoogleMaps(),
						new GoogleTileSource.GoogleEarth(), new OpenStreetMapTileSource.Mapnik(),
						new OpenStreetMapTileSource.TilesAtHome(),
						new OpenStreetMapTileSource.CycleMap() });
		mapSource.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				previewMap.setTileSource((TileSource) mapSource.getSelectedItem());
			}
		});
		// Zoom Panel

		String s = "ZOOM LEVELS (0, 1, 2 ..... ";
		for (int i = cbZoom.length - 3; i < cbZoom.length; i++) {
			s += i;
			if (i != (cbZoom.length - 1))
				s += ", ";
		}
		s += ")";
		zoomLevelLabel = new JLabel(s);
		zoomLevelLabel.setAlignmentX(Component.BOTTOM_ALIGNMENT);

		amountOfTilesLabel = new JLabel();
		amountOfTilesLabel.setBounds(175, 260, 100, 20);
		amountOfTilesLabel.setHorizontalAlignment(JLabel.RIGHT);
		amountOfTilesLabel.setToolTipText("Total amount of tiles to download");

		zoomLevelPanel = new JPanel(new GridLayout(2, cbZoom.length + 2 / 2, 0, 0));
		zoomLevelPanel.setPreferredSize(new Dimension(280, 30));
		zoomLevelPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		CheckBoxListener cbl = new CheckBoxListener();

		for (int i = 0; i < cbZoom.length; i++) {
			JCheckBox cb = new JCheckBox();
			cb.setPreferredSize(new Dimension(17, 17));
			s = "Zoom level " + i;
			if (i == 0)
				s += " (Minimum zoom)";
			if (i == cbZoom.length - 1)
				s += " (Maximum zoom)";
			cb.setToolTipText(s);
			cb.addActionListener(cbl);
			zoomLevelPanel.add(cb);
			cbZoom[i] = cb;
		}

		tileSizeLabel = new JLabel("TILE SIZE (Pixels)");
		// tileSizeLabel.setBounds(5, 316, 100, 20);

		tileSizePanel = new JPanel(null);
		tileSizePanel.setMinimumSize(new Dimension(275, 55));
		tileSizePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

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
		atlasNamePanel.setMinimumSize(new Dimension(275, 30));
		atlasNamePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		atlasNameTextField = new JTextField();
		atlasNameTextField.setBounds(5, 5, 264, 20);
		atlasNameTextField.setActionCommand("atlasNameTextField");

		profilesLabel = new JLabel("SAVED PROFILES");
		profilesLabel.setBounds(5, 460, 100, 20);

		chooseProfileButton = new JToggleButton("UNLOCK/LOCK");

		profilesPanel = new JPanel(null);
		profilesPanel.setMinimumSize(new Dimension(275, 231));
		profilesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		profilesJList = new JList();
		profilesJList.setBounds(1, 1, 264, 180);
		profilesJList.setEnabled(false);

		JScrollPane scrollPane =
				new JScrollPane(profilesJList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(5, 5, 264, 180);

		saveAsProfileButton = new JButton("Save as profile");
		saveAsProfileButton.setBounds(3, 195, 110, 25);

		deleteProfileButton = new JButton("Delete profile");
		deleteProfileButton.setBounds(159, 195, 110, 25);

		settingsGUIButton = new JButton("Settings");

		createAtlasButton = new JButton("Create Atlas");

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
		profilesPanel.add(saveAsProfileButton, gbc);
		profilesPanel.add(deleteProfileButton, gbc);

		leftPanel.add(coordinatesLabel, gbc);
		leftPanel.add(coordinatesPanel, gbc);
		leftPanel.add(new JLabel("Map Source"), gbc);
		leftPanel.add(mapSource, gbc);
		leftPanel.add(zoomLevelLabel, gbc);
		leftPanel.add(amountOfTilesLabel, gbc);
		leftPanel.add(zoomLevelPanel, gbc);
		leftPanel.add(tileSizeLabel, gbc);
		leftPanel.add(tileSizePanel, gbc);
		leftPanel.add(atlasNameLabel, gbc);
		leftPanel.add(atlasNamePanel, gbc);
		leftPanel.add(profilesLabel, gbc);
		leftPanel.add(chooseProfileButton, gbc);
		leftPanel.add(profilesPanel, gbc);
		leftPanel.add(settingsGUIButton, gbc);
		leftPanel.add(createAtlasButton, gbc);

		JPanel leftBorderPanel = new JPanel();
		leftBorderPanel.add(leftPanel);
		leftBorderPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
		add(leftBorderPanel, BorderLayout.WEST);
	}

	public void createRightPanel() {

		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

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
		previewSelectionButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				previewSelection();

			}
		});
		// latMinTextField.getDocument().addDocumentListener(
		// new JTextFieldListener());
		// latMaxTextField.getDocument().addDocumentListener(
		// new JTextFieldListener());
		// longMinTextField.getDocument().addDocumentListener(
		// new JTextFieldListener());
		// longMaxTextField.getDocument().addDocumentListener(
		// new JTextFieldListener());
		tileSizeWidthTextField.addFocusListener(new JTextFieldFocusChangeListener());
		tileSizeWidthTextField.getDocument().addDocumentListener(new JTextFieldListener());
		tileSizeHeightTextField.addFocusListener(new JTextFieldFocusChangeListener());
		tileSizeHeightTextField.getDocument().addDocumentListener(new JTextFieldListener());
		atlasNameTextField.getDocument().addDocumentListener(new JTextFieldListener());

		chooseProfileButton.addActionListener(new JToggleButtonListener());
		profilesJList.addListSelectionListener(new JListListener());

		topLeftCorner.addActionListener(new JPopupMenuListener());
		bottomRightCorner.addActionListener(new JPopupMenuListener());
	}

	/**
	 * Reads the entered coordinates and
	 */
	protected void previewSelection() {
		checkCoordinates();
		MapSelection ms = getMapSelectionCoordinates();
		previewMap.setSelection(ms);
	}

	protected void checkCoordinates() {
		MapSelection ms = getMapSelectionCoordinates();
		latMaxTextField.setText(Utilities.FORMAT_6_DEC.format(ms.lat_max));
		latMinTextField.setText(Utilities.FORMAT_6_DEC.format(ms.lat_min));
		lonMaxTextField.setText(Utilities.FORMAT_6_DEC.format(ms.lon_max));
		lonMinTextField.setText(Utilities.FORMAT_6_DEC.format(ms.lon_min));
	}

	protected MapSelection getMapSelectionCoordinates() {
		MapSelection ms = new MapSelection();
		try {
			ms.lat_max = Utilities.FORMAT_6_DEC.parse(latMaxTextField.getText()).doubleValue();
		} catch (ParseException e) {
			ms.lat_max = 0.0;
		}
		try {
			ms.lat_min = Utilities.FORMAT_6_DEC.parse(latMinTextField.getText()).doubleValue();
		} catch (ParseException e) {
			ms.lat_min = 0.0;
		}
		try {
			ms.lon_max = Utilities.FORMAT_6_DEC.parse(lonMaxTextField.getText()).doubleValue();
		} catch (ParseException e) {
			ms.lon_max = 0.0;
		}
		try {
			ms.lon_min = Utilities.FORMAT_6_DEC.parse(lonMinTextField.getText()).doubleValue();
		} catch (ParseException e) {
			ms.lon_min = 0.0;
		}
		return ms;
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
					"Could not create file settings.xml program will exit.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		fileSeparator = System.getProperty("file.separator");

		// Load all profiles from the profiles file from disk
		profilesVector =
				PersistentProfiles.load(new File(System.getProperty("user.dir") + fileSeparator
						+ "profiles.xml"));

		for (Profile p : profilesVector) {
			profileNamesVector.add(p.getProfileName());
		}
		profilesJList.setListData(profileNamesVector);

		UpdateGUI.updateAllUIs();
	}

	public String validateInput(boolean isCreateAtlasValidate) {

		String errorText = "";

		if (lonMinTextField.getText().length() < 1) {
			errorText = "A value of \"Longitude Min\" must be entered \n";
		} else {
			try {
				Double temp = Utilities.FORMAT_6_DEC.parse(lonMinTextField.getText()).doubleValue();

				if (temp < -179 || temp > 179) {
					errorText += "Value of \"Longitude Min\" must be between -179 and 179 \n";
				}
			} catch (ParseException nfex) {
				errorText += "Value of \"Longitude Min\" is not a valid decimal number \n";
			}
		}

		if (lonMaxTextField.getText().length() < 1) {
			errorText += "A value of \"Longitude Max\" must be entered \n";
		} else {
			try {
				Double temp = Utilities.FORMAT_6_DEC.parse(lonMaxTextField.getText()).doubleValue();

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
				Double temp = Utilities.FORMAT_6_DEC.parse(latMaxTextField.getText()).doubleValue();

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
				Double temp = Utilities.FORMAT_6_DEC.parse(latMinTextField.getText()).doubleValue();

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

				result = Utilities.validateString(atlasNameTextField.getText(), false);

				if (result > -1) {
					errorText += "Atlas name contains illegal characters (" + (char) result + ")\n";
				}
			}
		}

		if (isCreateAtlasValidate) {

			boolean zoomLevelChosen = false;

			for (int i = 0; i < cbZoom.length; i++) {
				if (cbZoom[i].isSelected()) {
					zoomLevelChosen = true;
					break;
				}
			}

			if (zoomLevelChosen == false) {
				errorText += "A zoom level must be selected\n";
			}

			String input = tileSizeWidthTextField.getText();

			if (input.length() > 0) {

				int result = -1;

				result = Utilities.validateTileSizeInput(input);

				if (result > -1) {
					errorText += "\"" + (char) result + "\" is not valid input of Custom size (W)";

				} else {
					if (Integer.parseInt(input) > 1792) {
						errorText += "\"" + input + "\" is not valid input of Custom size (W)";
					}
					if (Integer.parseInt(input) < 50) {
						errorText += "\"" + input + "\" is not valid input of Custom size (W)";
					}
				}
			}

			input = tileSizeHeightTextField.getText();

			if (input.length() > 0) {

				int result = -1;

				result = Utilities.validateTileSizeInput(input);

				if (result > -1) {
					errorText += "\"" + (char) result + "\" is not valid input of Custom size (H)";

				} else {
					if (Integer.parseInt(input) > 1792) {
						errorText += "\"" + input + "\" is not valid input of Custom size (H)";
					}
					if (Integer.parseInt(input) < 50) {
						errorText += "\"" + input + "\" is not valid input of Custom size (H)";
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
			latMax = Utilities.FORMAT_6_DEC.parse(latMaxTextField.getText()).doubleValue();
			latMin = Utilities.FORMAT_6_DEC.parse(latMinTextField.getText()).doubleValue();
			longMax = Utilities.FORMAT_6_DEC.parse(lonMaxTextField.getText()).doubleValue();
			longMin = Utilities.FORMAT_6_DEC.parse(lonMinTextField.getText()).doubleValue();
		} catch (ParseException e) {
			return false;
		}

		boolean maxIsBiggerThanMin = true;

		if (latMax < latMin) {

			JOptionPane.showMessageDialog(null, "Latitude Min is greater than Latitude Max",
					"Errors", JOptionPane.ERROR_MESSAGE);
			maxIsBiggerThanMin = false;
		}

		if (longMax < longMin) {

			JOptionPane.showMessageDialog(null, "Longitude Min is greater than Longitude Max",
					"Errors", JOptionPane.ERROR_MESSAGE);
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

				Point topLeft =
						GoogleTileUtils.toTileXY(Double.parseDouble(latMaxTextField.getText()),
								Double.parseDouble(lonMaxTextField.getText()), zoomLevels[i]);
				Point bottomRight =
						GoogleTileUtils.toTileXY(Double.parseDouble(latMinTextField.getText()),
								Double.parseDouble(lonMinTextField.getText()), zoomLevels[i]);

				totalNrOfTiles =
						totalNrOfTiles
								+ Utilities.calculateNrOfTiles(new TileXYMinMaxAndZoom(topLeft,
										bottomRight, zoomLevels[i]));
			}
			amountOfTilesLabel.setText("( " + Integer.toString(totalNrOfTiles) + " )");
		}
	}

	// WindowDestroyer
	private class WindowDestroyer extends WindowAdapter {
		public void windowClosing(WindowEvent e) {

			Settings s = Settings.getInstance();
			try {
				s.store();
			} catch (IOException iox) {
				JOptionPane.showMessageDialog(null,
						"Could not create file settings.xml program will exit.", "Error",
						JOptionPane.ERROR_MESSAGE);
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
							JOptionPane.showMessageDialog(null, "\"" + input
									+ "\" is not valid input of Custom size (W) \n\n"
									+ "Accepted values are between 50 and 1792", "Error",
									JOptionPane.ERROR_MESSAGE);
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

		if ((((this.getFocusOwner()).getClass()).toString()).equals("class javax.swing.JTextField")) {

			input = ((JTextField) this.getFocusOwner()).getText();

			if (input.length() > 0) {

				String jtfobjAsString = ((JTextField) this.getFocusOwner()).toString();

				if (jtfobjAsString.indexOf("latMaxTextField") > -1
						|| jtfobjAsString.indexOf("latMinTextField") > -1
						|| jtfobjAsString.indexOf("longMaxTextField") > -1
						|| jtfobjAsString.indexOf("longMinTextField") > -1) {

					String result = "";

					result = Utilities.validateCordinateInput(input, jtfobjAsString);

					if (result.length() > 0) {
						JOptionPane.showMessageDialog(null, "\"" + result
								+ "\" is not valid input.\n"
								+ "Please enter a valid number in the form "
								+ Utilities.FORMAT_6_DEC.format(1.12345), "Error",
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}

				if (jtfobjAsString.indexOf("tileSizeWidthTextField") > -1) {

					int result = -1;

					result = Utilities.validateTileSizeInput(input);

					if (result > -1) {
						JOptionPane.showMessageDialog(null, "\"" + (char) result
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
						JOptionPane.showMessageDialog(null, "\"" + (char) result
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
						JOptionPane.showMessageDialog(null, "\"" + (char) result
								+ "\" is not valid input", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean retryDownloadPreviewTile(int xValue, int yValue, int zoomValue,
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

	public SelectedZoomLevels getSelectedZoomlevels() {

		SelectedZoomLevels sZL = new SelectedZoomLevels();

		for (int i = 0; i < cbZoom.length; i++) {
			sZL.setZoomLevelSelected(i, cbZoom[i].isSelected());
		}
		sZL.sort();
		return sZL;
	}

	private class JPopupMenuListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			double[] coordinates =
					Utilities.calculatePreviewCoordinates(ProcessValues.getMouseXCoordinat(),
							ProcessValues.getMouseYCoordinat());

			double selectedLongMin = coordinates[0];
			double latMaxApproximitation = coordinates[1];

			String actionCommand = e.getActionCommand();

			if (actionCommand.equals("Top left corner")) {

				latMaxTextField.setText(Utilities.nrOfDecimals(latMaxApproximitation, 6));
				lonMinTextField.setText(Utilities.nrOfDecimals(selectedLongMin, 6));
				jpm.setVisible(false);
			} else if (actionCommand.equals("Bottom right corner")) {
				latMinTextField.setText(Utilities.nrOfDecimals(latMaxApproximitation, 6));
				lonMaxTextField.setText(Utilities.nrOfDecimals(selectedLongMin, 6));
				jpm.setVisible(false);
			}
		}
	}

	private void createAtlas() {
		{

			String errorText = validateInput(true);
			if (errorText.length() > 0) {
				JOptionPane.showMessageDialog(null, errorText, "Errors", JOptionPane.ERROR_MESSAGE);
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
							JOptionPane.showMessageDialog(null,
									"Custom tile size width value is not a valid integer value",
									"Errors", JOptionPane.ERROR_MESSAGE);
						}
					}
					if (tileSizeHeightComboBox.isEnabled() == false) {

						try {
							Integer.parseInt(tileSizeHeightTextField.getText());
						} catch (NumberFormatException nfex) {

							customTileSizeHeightIsOk = false;
							JOptionPane.showMessageDialog(null,
									"Custom tile size height value is not a valid integer value",
									"Errors", JOptionPane.ERROR_MESSAGE);
						}
					}
					if (customTileSizeWidthIsOk && customTileSizeHeightIsOk) {
						createAtlasButton.setEnabled(false);

						int tileSizeWidth = 0;
						int tileSizeHeight = 0;

						if (tileSizeWidthComboBox.isEnabled()) {
							tileSizeWidth =
									Integer.parseInt(tileSizeWidthComboBox.getSelectedItem()
											.toString());
						} else {
							tileSizeWidth = Integer.parseInt(tileSizeWidthTextField.getText());
						}
						if (tileSizeHeightComboBox.isEnabled()) {
							tileSizeHeight =
									Integer.parseInt(tileSizeHeightComboBox.getSelectedItem()
											.toString());
						} else {
							tileSizeHeight = Integer.parseInt(tileSizeHeightTextField.getText());
						}

						try {
							atlasThread =
									new AtlasThread(this, atlasNameTextField.getText(),
											getMapSelectionCoordinates(), getSelectedZoomlevels(),
											tileSizeWidth, tileSizeHeight);
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
						"Profile name already exists, choose a different name", "Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				String errorDescription = "";

				// try {
				// latMax = Double.parseDouble(latMaxTextField.getText());
				// } catch (NumberFormatException nfex) {
				// errorDescription =
				// "Invalid format of \"Latitude Max\" value\n";
				// }
				// try {
				// latMin = Double.parseDouble(latMinTextField.getText());
				// } catch (NumberFormatException nfex) {
				// errorDescription +=
				// "Invalid format of \"Latitude Min\" value\n";
				// }
				// try {
				// longMax = Double.parseDouble(longMaxTextField.getText());
				// } catch (NumberFormatException nfex) {
				// errorDescription +=
				// "Invalid format of \"Longitude Max\" value\n";
				// }
				// try {
				// longMin = Double.parseDouble(longMinTextField.getText());
				// } catch (NumberFormatException nfex) {
				// errorDescription +=
				// "Invalid format of \"Longitude Min\" value\n";
				// }
				//
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
					theProfile.setLatitudeMax(Double.parseDouble(latMaxTextField.getText()));
					theProfile.setLatitudeMin(Double.parseDouble(latMinTextField.getText()));
					theProfile.setLongitudeMax(Double.parseDouble(lonMaxTextField.getText()));
					theProfile.setLongitudeMin(Double.parseDouble(lonMinTextField.getText()));

					boolean[] zoomLevels = new boolean[cbZoom.length];
					for (int i = 0; i < cbZoom.length; i++) {
						zoomLevels[i] = cbZoom[i].isSelected();
					}

					theProfile.setZoomLevels(zoomLevels);
					theProfile.setTileSizeWidth(tileSizeWidthComboBox.getSelectedIndex());
					theProfile.setTileSizeHeight(tileSizeHeightComboBox.getSelectedIndex());

					if (!tileSizeWidthComboBox.isEnabled()) {
						theProfile.setCustomTileSizeWidth(Integer.parseInt(tileSizeWidthTextField
								.getText()));
					} else {
						theProfile.setCustomTileSizeWidth(0);
					}

					if (!tileSizeHeightComboBox.isEnabled()) {
						theProfile.setCustomTileSizeHeight(Integer.parseInt(tileSizeHeightTextField
								.getText()));
					} else {
						theProfile.setCustomTileSizeHeight(0);
					}

					profilesVector.addElement(theProfile);
					PersistentProfiles.store(profilesVector);
					initiateProgram();
				} else {
					JOptionPane.showMessageDialog(null, errorDescription, "Errors",
							JOptionPane.ERROR_MESSAGE);
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
			}
		}
	}

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
				lonMinTextField.setText(Double.toString(temp.getLongitudeMin()));
				lonMaxTextField.setText(Double.toString(temp.getLongitudeMax()));

				if (temp.getCustomTileSizeWidth() == 0) {
					tileSizeWidthTextField.setText("");
				} else {
					tileSizeWidthTextField.setText(Integer.toString(temp.getCustomTileSizeWidth()));
				}

				if (temp.getCustomTileSizeHeight() == 0) {
					tileSizeHeightTextField.setText("");
				} else {
					tileSizeHeightTextField.setText(Integer
							.toString(temp.getCustomTileSizeHeight()));
				}

				atlasNameTextField.setText(temp.getAtlasName());

				boolean[] zoomValues = new boolean[cbZoom.length];

				zoomValues = temp.getZoomLevels();

				for (int i = 0; i < cbZoom.length; i++) {
					cbZoom[i].setSelected(zoomValues[i]);
				}

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

	private class CheckBoxListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			calculateNrOfTilesToDownload();
		}
	}

	public void selectionChanged(java.awt.geom.Point2D.Double max, java.awt.geom.Point2D.Double min) {
		lonMaxTextField.setText(Utilities.FORMAT_6_DEC.format(max.x));
		lonMinTextField.setText(Utilities.FORMAT_6_DEC.format(min.x));
		latMaxTextField.setText(Utilities.FORMAT_6_DEC.format(max.y));
		latMinTextField.setText(Utilities.FORMAT_6_DEC.format(min.y));
	}

	public void setCreateAtlasButtonEnabled(boolean enabled) {
		createAtlasButton.setEnabled(enabled);
	}
}