package tac.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.gui.preview.MapSelectionListener;
import tac.gui.preview.MapSources;
import tac.gui.preview.PreviewMap;
import tac.program.AtlasThread;
import tac.program.EastNorthCoordinate;
import tac.program.MapSelection;
import tac.program.Profile;
import tac.program.SelectedZoomLevels;
import tac.program.Settings;
import tac.utilities.PersistentProfiles;
import tac.utilities.Utilities;

public class GUI extends JFrame implements MapSelectionListener {

	private static final long serialVersionUID = -8444942802691874960L;

	private static final String VERSION = "0.9 alpha";

	private JPanel leftPanel;
	private JPanel rightPanel;
	private JPanel coordinatesPanel;
	private JPanel zoomLevelPanel;
	private JPanel tileSizePanel;
	private JPanel atlasNamePanel;
	private JPanel profilesPanel;

	private JScrollPane leftScrollPane;

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

	private JCoordinateField latMinTextField;
	private JCoordinateField latMaxTextField;
	private JCoordinateField lonMinTextField;
	private JCoordinateField lonMaxTextField;
	private JTextField tileSizeWidthTextField;
	private JTextField tileSizeHeightTextField;
	private JTextField atlasNameTextField;

	private JCheckBox[] cbZoom;

	private JComboBox tileSizeWidthComboBox;
	private JComboBox tileSizeHeightComboBox;
	private JComboBox mapSource;

	private Vector<Integer> tileSizeValues;
	private Vector<Profile> profilesVector;
	private Vector<String> profileNamesVector;

	private JList profilesJList;
	private String fileSeparator;

	public GUI() {
		super();
		createMainFrame();
		createLeftPanel();
		createRightPanel();
		addListeners();
		initiateProgram();
	}

	private void createMainFrame() {

		setTitle("TrekBuddy Atlas Creator v" + VERSION);

		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		dScreen.width = Math.min(1024, dScreen.width);
		dScreen.height = Math.min(768, dScreen.height);
		setSize(dScreen);
		setMinimumSize(new Dimension(800, 600));
		setResizable(true);
		setExtendedState(JFrame.MAXIMIZED_BOTH);

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

		leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());
		// leftPanel.setMinimumSize(new Dimension(290, 800));
		// leftPanel.setPreferredSize(leftPanel.getMinimumSize());

		leftScrollPane = new JScrollPane(leftPanel);
		leftScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		leftScrollPane.setPreferredSize(new Dimension(315, 200));

		coordinatesLabel = new JLabel("COORDINATES");

		// Coordinates Panel

		coordinatesPanel = new JPanel(null);
		coordinatesPanel.setMinimumSize(new Dimension(270, 200));
		coordinatesPanel.setPreferredSize(coordinatesPanel.getMinimumSize());
		coordinatesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		latMaxLabel = new JLabel("Latitude Max");
		latMaxLabel.setBounds(112, 20, 100, 20);

		latMaxTextField = new JCoordinateField(MapSelection.LAT_MIN, MapSelection.LAT_MAX);
		latMaxTextField.setBounds(88, 42, 100, 20);
		latMaxTextField.setActionCommand("latMaxTextField");

		longMinLabel = new JLabel("Longitude Min");
		longMinLabel.setBounds(20, 70, 100, 20);

		lonMinTextField = new JCoordinateField(MapSelection.LON_MIN, MapSelection.LON_MAX);
		lonMinTextField.setBounds(5, 90, 100, 20);
		lonMinTextField.setActionCommand("longMinTextField");

		longMaxLabel = new JLabel("Longitude Max");
		longMaxLabel.setBounds(185, 70, 100, 20);

		lonMaxTextField = new JCoordinateField(MapSelection.LON_MIN, MapSelection.LON_MAX);
		lonMaxTextField.setBounds(162, 90, 100, 20);
		lonMaxTextField.setActionCommand("longMaxTextField");

		latMinLabel = new JLabel("Latitude Min");
		latMinLabel.setBounds(112, 120, 100, 20);

		latMinTextField = new JCoordinateField(MapSelection.LAT_MIN, MapSelection.LAT_MAX);
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

		mapSource = new JComboBox(MapSources.getMapSources());
		mapSource.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				mapSourceChanged();
			}
		});
		// Zoom Panel

		zoomLevelLabel = new JLabel();
		zoomLevelLabel.setAlignmentX(Component.BOTTOM_ALIGNMENT);

		amountOfTilesLabel = new JLabel();
		amountOfTilesLabel.setHorizontalAlignment(JLabel.RIGHT);
		amountOfTilesLabel.setToolTipText("Total amount of tiles to download");

		zoomLevelPanel = new JPanel();
		zoomLevelPanel.setPreferredSize(new Dimension(280, 44));
		zoomLevelPanel.setMinimumSize(zoomLevelPanel.getPreferredSize());
		zoomLevelPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		tileSizeLabel = new JLabel("TILE SIZE (Pixels)");
		// tileSizeLabel.setBounds(5, 316, 100, 20);

		tileSizePanel = new JPanel(null);
		tileSizePanel.setMinimumSize(new Dimension(275, 55));
		tileSizePanel.setPreferredSize(tileSizePanel.getMinimumSize());
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
		atlasNamePanel.setPreferredSize(atlasNamePanel.getMinimumSize());
		atlasNamePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		atlasNameTextField = new JTextField();
		atlasNameTextField.setBounds(5, 5, 264, 20);
		atlasNameTextField.setActionCommand("atlasNameTextField");

		profilesLabel = new JLabel("SAVED PROFILES");
		profilesLabel.setBounds(5, 460, 100, 20);

		chooseProfileButton = new JToggleButton("UNLOCK/LOCK");

		profilesPanel = new JPanel(null);
		profilesPanel.setMinimumSize(new Dimension(275, 231));
		profilesPanel.setPreferredSize(profilesPanel.getMinimumSize());
		profilesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		profilesJList = new JList();
		profilesJList.setBounds(1, 1, 264, 180);
		profilesJList.setEnabled(false);

		JScrollPane scrollPane = new JScrollPane(profilesJList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
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

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 5, 2, 2);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		profilesPanel.add(scrollPane);
		profilesPanel.add(saveAsProfileButton, gbc);
		profilesPanel.add(deleteProfileButton, gbc);

		leftPanel.add(coordinatesLabel, gbc);
		leftPanel.add(coordinatesPanel, gbc);
		leftPanel.add(new JLabel("Map Source"), gbc);
		leftPanel.add(mapSource, gbc);
		gbc.gridwidth = 1;
		leftPanel.add(zoomLevelLabel, gbc);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
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

		add(leftScrollPane, BorderLayout.WEST);

		atlasNameTextField.setText("Test");
	}

	public void createRightPanel() {

		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		previewMap = new PreviewMap();
		previewMap.addMapSelectionListener(this);

		// Allows to disable map painting and tile loading
		// for debugging purposes
		// TODO Enable map preview
		// previewMap.setEnabled(false);

		rightPanel.add(previewMap, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.CENTER);
		createZoomLevelCheckBoxes();
	}

	protected void createZoomLevelCheckBoxes() {
		int zoomLevels = ((TileSource) mapSource.getSelectedItem()).getMaxZoom();
		cbZoom = new JCheckBox[zoomLevels];
		String s = "ZOOM LEVELS (0, 1, 2 ..... ";
		for (int i = cbZoom.length - 3; i < cbZoom.length; i++) {
			s += i;
			if (i != (cbZoom.length - 1))
				s += ", ";
		}
		s += ")";
		zoomLevelLabel.setText(s);
		zoomLevelPanel.removeAll();
		zoomLevelPanel.setLayout(new GridLayout(2, cbZoom.length + 2 / 2, 0, 0));
		CheckBoxListener cbl = new CheckBoxListener();

		for (int i = 0; i < cbZoom.length; i++) {
			JCheckBox cb = new JCheckBox();
			cb.setPreferredSize(new Dimension(17, 25));
			cb.setMinimumSize(cb.getPreferredSize());
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

	}

	public void addListeners() {
		this.addWindowListener(new WindowDestroyer());
		this.addWindowListener(new JFrameListener());
		ButtonListener bl = new ButtonListener();
		saveAsProfileButton.addActionListener(bl);
		deleteProfileButton.addActionListener(bl);
		settingsGUIButton.addActionListener(bl);
		createAtlasButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createAtlas();
			}
		});
		previewSelectionButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				previewSelection();

			}
		});
		JTextFieldListener jtfl = new JTextFieldListener();
		tileSizeWidthTextField.addFocusListener(new JTextFieldFocusChangeListener());
		tileSizeWidthTextField.getDocument().addDocumentListener(jtfl);
		tileSizeHeightTextField.addFocusListener(new JTextFieldFocusChangeListener());
		tileSizeHeightTextField.getDocument().addDocumentListener(jtfl);
		atlasNameTextField.getDocument().addDocumentListener(jtfl);

		chooseProfileButton.addActionListener(new JToggleButtonListener());
		profilesJList.addListSelectionListener(new JListListener());
	}

	/**
	 * Reads the entered coordinates and marks the selection on the preview map
	 * (zooms automatically to the selected area.
	 */
	protected void previewSelection() {
		MapSelection ms = checkCoordinates();
		previewMap.setSelection(ms);
	}

	protected MapSelection checkCoordinates() {
		MapSelection ms = getMapSelectionCoordinates();
		latMaxTextField.setCoordinate(ms.getLat_max());
		latMinTextField.setCoordinate(ms.getLat_min());
		lonMaxTextField.setCoordinate(ms.getLon_max());
		lonMinTextField.setCoordinate(ms.getLon_min());
		return ms;
	}

	protected MapSelection getMapSelectionCoordinates() {
		double lat_max, lat_min, lon_max, lon_min;
		try {
			lat_max = latMaxTextField.getCoordinate();
		} catch (ParseException e) {
			lat_max = Double.NaN;
		}
		try {
			lat_min = latMinTextField.getCoordinate();
		} catch (ParseException e) {
			lat_min = Double.NaN;
		}
		try {
			lon_max = lonMaxTextField.getCoordinate();
		} catch (ParseException e) {
			lon_max = Double.NaN;
		}
		try {
			lon_min = lonMinTextField.getCoordinate();
		} catch (ParseException e) {
			lon_min = Double.NaN;
		}
		return new MapSelection(lat_max, lat_min, lon_max, lon_min);
	}

	public void initiateProgram() {

		// Check if all necessary files and folder exists
		Utilities.checkFileSetup();

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
		previewMap.settingsLoadPosition();
		mapSource.setSelectedItem(MapSources.getSourceByName(settings.getDefaultMapSource()));

		fileSeparator = System.getProperty("file.separator");

		// Load all profiles from the profiles file from disk
		profilesVector = PersistentProfiles.load(new File(System.getProperty("user.dir")
				+ fileSeparator + "profiles.xml"));

		for (Profile p : profilesVector) {
			profileNamesVector.add(p.getProfileName());
		}
		profilesJList.setListData(profileNamesVector);

		UpdateGUI.updateAllUIs();
	}

	public String validateInput(boolean isCreateAtlasValidate) {

		String errorText = "";

		if (!lonMinTextField.isInputValid())
			errorText += "Value of \"Longitude Min\" must be between -179 and 179 \n";

		if (!lonMaxTextField.isInputValid())
			errorText += "Value of \"Longitude Max\" must be between -179 and 179 \n";

		if (!latMaxTextField.isInputValid())
			errorText += "Value of \"Latitude Max\" must be between -85 and 85 \n";

		if (!latMinTextField.isInputValid())
			errorText += "Value of \"Latitude Min\" must be between -85 and 85 \n";

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
			latMax = latMaxTextField.getCoordinate();
			latMin = latMinTextField.getCoordinate();
			longMax = lonMaxTextField.getCoordinate();
			longMin = lonMinTextField.getCoordinate();
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

		try {
			SelectedZoomLevels sZL = new SelectedZoomLevels(cbZoom);

			int[] zoomLevels = sZL.getZoomLevels();

			long totalNrOfTiles = 0;

			for (int i = 0; i < zoomLevels.length; i++) {
				MapSelection ms = getMapSelectionCoordinates();
				totalNrOfTiles += ms.calculateNrOfTiles(zoomLevels[i]);
			}
			amountOfTilesLabel.setText("( " + Long.toString(totalNrOfTiles) + " )");
		} catch (Exception e) {
			amountOfTilesLabel.setText("( ? )");
			e.printStackTrace();
		}
	}

	// WindowDestroyer
	private class WindowDestroyer extends WindowAdapter {
		public void windowClosing(WindowEvent e) {

			Settings s = Settings.getInstance();
			previewMap.settingsSavePosition();
			s.setDefaultMapSource(((TileSource) mapSource.getSelectedItem()).getName());
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

	public void mapSourceChanged() {
		previewMap.setTileSource((TileSource) mapSource.getSelectedItem());
		createZoomLevelCheckBoxes();
	}

	private void createAtlas() {

		String errorText = validateInput(true);
		if (errorText.length() > 0) {
			JOptionPane.showMessageDialog(null, errorText, "Errors", JOptionPane.ERROR_MESSAGE);
		} else {

			boolean maxIsBiggerThanMin = true;

			maxIsBiggerThanMin = validateLatLongMinMax();

			if (maxIsBiggerThanMin) {

				int customTileSizeWidth = -1;
				int customTileSizeHeight = -1;

				if (tileSizeWidthComboBox.isEnabled() == false) {
					try {
						customTileSizeWidth = Integer.parseInt(tileSizeWidthTextField.getText());
					} catch (NumberFormatException nfex) {
						JOptionPane.showMessageDialog(null,
								"Custom tile size width value is not a valid integer value",
								"Errors", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				if (tileSizeHeightComboBox.isEnabled() == false) {
					try {
						customTileSizeHeight = Integer.parseInt(tileSizeHeightTextField.getText());
					} catch (NumberFormatException nfex) {
						JOptionPane.showMessageDialog(null,
								"Custom tile size height value is not a valid integer value",
								"Errors", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				int tileSizeWidth = 256;
				int tileSizeHeight = 256;

				if (tileSizeWidthComboBox.isEnabled()) {
					tileSizeWidth = ((Integer) tileSizeWidthComboBox.getSelectedItem()).intValue();
				} else {
					tileSizeWidth = customTileSizeWidth;
				}
				if (tileSizeHeightComboBox.isEnabled()) {
					tileSizeHeight = ((Integer) tileSizeHeightComboBox.getSelectedItem())
							.intValue();
				} else {
					tileSizeHeight = customTileSizeHeight;
				}

				TileSource tileSource = (TileSource) mapSource.getSelectedItem();
				SelectedZoomLevels sZL = new SelectedZoomLevels(cbZoom);
				try {
					Thread atlasThread = new AtlasThread(atlasNameTextField.getText(), tileSource,
							getMapSelectionCoordinates(), sZL, tileSizeWidth, tileSizeHeight);
					atlasThread.start();
				} catch (Exception ex) {
					System.out.println(ex);
				}

			}
		}
		System.gc();
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

				MapSelection ms = checkCoordinates();

				if (!ms.coordinatesAreValid()) {
					errorDescription += "Coordinates are not all valid - please check";
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

				if (errorDescription.length() > 0) {
					JOptionPane.showMessageDialog(null, errorDescription, "Errors",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					theProfile.setProfileName(inputValue);
					theProfile.setAtlasName(atlasNameTextField.getText());
					theProfile.setLatitudeMax(latMaxTextField.getCoordinate());
					theProfile.setLatitudeMin(latMinTextField.getCoordinate());
					theProfile.setLongitudeMax(lonMaxTextField.getCoordinate());
					theProfile.setLongitudeMin(lonMinTextField.getCoordinate());

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
				} catch (ParseException e) {
					e.printStackTrace();
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
				SettingsGUI sgui = new SettingsGUI(GUI.this);
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

				latMinTextField.setCoordinate(temp.getLatitudeMin());
				latMaxTextField.setCoordinate(temp.getLatitudeMax());
				lonMinTextField.setCoordinate(temp.getLongitudeMin());
				lonMaxTextField.setCoordinate(temp.getLongitudeMax());

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

		public void windowOpened(WindowEvent e) {
			// createAtlasButton.setText("Wait...");
			// createAtlasButton.setEnabled(false);
		}

	}

	private class CheckBoxListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			calculateNrOfTilesToDownload();
		}
	}

	public void selectionChanged(EastNorthCoordinate max, EastNorthCoordinate min) {
		lonMaxTextField.setCoordinate(max.lon);
		lonMinTextField.setCoordinate(min.lon);
		latMaxTextField.setCoordinate(max.lat);
		latMinTextField.setCoordinate(min.lat);
		calculateNrOfTilesToDownload();
	}

	public void setCreateAtlasButtonEnabled(boolean enabled) {
		createAtlasButton.setEnabled(enabled);
	}
}