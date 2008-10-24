package tac.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
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

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.StartTAC;
import tac.gui.preview.MapSelectionListener;
import tac.gui.preview.MapSources;
import tac.gui.preview.PreviewMap;
import tac.program.AtlasThread;
import tac.program.EastNorthCoordinate;
import tac.program.MapSelection;
import tac.program.Profile;
import tac.program.SelectedZoomLevels;
import tac.program.Settings;
import tac.utilities.GBC;
import tac.utilities.PersistentProfiles;
import tac.utilities.Utilities;

public class GUI extends JFrame implements MapSelectionListener {

	private static final long serialVersionUID = -8444942802691874960L;

	private static Logger log = Logger.getLogger(GUI.class);

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
	private JLabel lonMinLabel;
	private JLabel lonMaxLabel;
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
	private JTileSizeField tileSizeWidthTextField;
	private JTileSizeField tileSizeHeightTextField;
	private JTextField atlasNameTextField;

	private JCheckBox[] cbZoom = new JCheckBox[0];

	private JComboBox tileSizeWidthComboBox;
	private JComboBox tileSizeHeightComboBox;
	private JComboBox mapSource;

	private Vector<Integer> tileSizeValues;
	private Vector<Profile> profilesVector = new Vector<Profile>();
	private Vector<String> profileNamesVector = new Vector<String>();

	private JList profilesJList;
	private String fileSeparator;

	public GUI() {
		super();
		InputStream propIn = StartTAC.class.getResourceAsStream("tac.properties");
		Properties props = new Properties();
		try {
			props.load(propIn);
		} catch (IOException e) {
			log.error("Can not find tac properties file");
			props.setProperty("tac.version", "unknown");
		} finally {
			Utilities.closeStream(propIn);
		}

		setTitle("TrekBuddy Atlas Creator v" + props.getProperty("tac.version"));
		log.trace("Creating main dialog - " + getTitle());
		createMainFrame();
		createLeftPanel();
		createRightPanel();
		addListeners();
		initiateProgram();
	}

	private void createMainFrame() {
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
				log.error("The selection of look and feel was not possible!", e1);
			} catch (Exception e1) {
			}
		}
	}

	public void createLeftPanel() {

		leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());

		leftScrollPane = new JScrollPane(leftPanel);
		leftScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		leftScrollPane.setPreferredSize(new Dimension(280, 200));

		coordinatesLabel = new JLabel("COORDINATES");

		// Coordinates Panel

		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWeights = new double[] { 4, 1, 2, 1, 4 };
		coordinatesPanel = new JPanel(gbl);

		coordinatesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		latMaxLabel = new JLabel("Latitude Max", JLabel.CENTER);

		latMaxTextField = new JCoordinateField(MapSelection.LAT_MIN, MapSelection.LAT_MAX);
		latMaxTextField.setActionCommand("latMaxTextField");

		lonMinLabel = new JLabel("Longitude Min", JLabel.CENTER);

		lonMinTextField = new JCoordinateField(MapSelection.LON_MIN, MapSelection.LON_MAX);
		lonMinTextField.setActionCommand("longMinTextField");

		lonMaxLabel = new JLabel("Longitude Max", JLabel.CENTER);

		lonMaxTextField = new JCoordinateField(MapSelection.LON_MIN, MapSelection.LON_MAX);
		lonMaxTextField.setActionCommand("longMaxTextField");

		latMinLabel = new JLabel("Latitude Min", JLabel.CENTER);

		latMinTextField = new JCoordinateField(MapSelection.LAT_MIN, MapSelection.LAT_MAX);
		latMinTextField.setActionCommand("latMinTextField");

		previewSelectionButton = new JButton("Display selection");
		// previewSelectionButton.setBounds(78, 170, 120, 20);

		GBC gbc_eolcf = GBC.eol().fill(GBC.HORIZONTAL).anchor(GBC.CENTER);
		GBC gbc_eolc = GBC.eol().anchor(GBC.CENTER).insets(2, 2, 2, 2);

		JPanel latMaxPanel = new JPanel(new GridBagLayout());
		latMaxPanel.add(latMaxLabel, gbc_eolc);
		latMaxPanel.add(latMaxTextField, gbc_eolc);
		coordinatesPanel.add(latMaxPanel, gbc_eolcf.insets(5, 5, 5, 1));

		JPanel lonMinPanel = new JPanel(new GridBagLayout());
		lonMinPanel.add(lonMinLabel, gbc_eolc);
		lonMinPanel.add(lonMinTextField, gbc_eolc);

		JPanel lonMaxPanel = new JPanel(new GridBagLayout());
		lonMaxPanel.add(lonMaxLabel, gbc_eolc);
		lonMaxPanel.add(lonMaxTextField, gbc_eolc);

		JPanel lonPanel = new JPanel(new BorderLayout());
		lonPanel.add(lonMinPanel, BorderLayout.WEST);
		lonPanel.add(lonMaxPanel, BorderLayout.EAST);
		coordinatesPanel.add(lonPanel, gbc_eolcf);

		JPanel latMinPanel = new JPanel(new GridBagLayout());
		latMinPanel.add(latMinLabel, gbc_eolc);
		latMinPanel.add(latMinTextField, gbc_eolc);

		coordinatesPanel.add(latMinPanel, gbc_eolcf);

		coordinatesPanel.add(previewSelectionButton, gbc_eolcf.fill(GBC.NONE).insets(20, 5, 20, 5));

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
		zoomLevelPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		tileSizeLabel = new JLabel("TILE SIZE (Pixels)");

		tileSizePanel = new JPanel(new GridBagLayout());
		tileSizePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		tileSizeValues = new Vector<Integer>();
		for (int i = 0; i < 7; i++)
			tileSizeValues.addElement((i + 1) * 256);

		JLabel tileSizeWidth = new JLabel("Width:");

		tileSizeWidthComboBox = new JComboBox(tileSizeValues);
		tileSizeWidthComboBox.setToolTipText("Width");

		customTileSizeWidthLabel = new JLabel("Custom size (W):");

		tileSizeWidthTextField = new JTileSizeField();
		tileSizeWidthTextField.setToolTipText("Width");

		JLabel tileSizeHeight = new JLabel("Height:");

		tileSizeHeightComboBox = new JComboBox(tileSizeValues);
		tileSizeHeightComboBox.setToolTipText("Height");

		customTileSizeHeightLabel = new JLabel("Custom size (H):");

		tileSizeHeightTextField = new JTileSizeField();
		tileSizeHeightTextField.setToolTipText("Height");

		GBC gbc_std = GBC.std().insets(5, 2, 5, 3);
		GBC gbc_eol = GBC.eol().insets(5, 2, 5, 3);
		GBC gbc_hspace = GBC.std().fill(GBC.HORIZONTAL);

		tileSizePanel.add(tileSizeWidth, gbc_std);
		tileSizePanel.add(tileSizeWidthComboBox, gbc_std);
		tileSizePanel.add(Box.createHorizontalGlue(), gbc_hspace);
		tileSizePanel.add(customTileSizeWidthLabel, gbc_std);
		tileSizePanel.add(tileSizeWidthTextField, gbc_eol);
		tileSizePanel.add(tileSizeHeight, gbc_std);
		tileSizePanel.add(tileSizeHeightComboBox, gbc_std);
		tileSizePanel.add(Box.createHorizontalGlue(), gbc_hspace);
		tileSizePanel.add(customTileSizeHeightLabel, gbc_std);
		tileSizePanel.add(tileSizeHeightTextField, gbc_eol);

		atlasNameLabel = new JLabel("ATLAS NAME");

		atlasNamePanel = new JPanel(new GridBagLayout());
		atlasNamePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		atlasNameTextField = new JTextField();
		atlasNameTextField.setActionCommand("atlasNameTextField");

		atlasNamePanel.add(atlasNameTextField, GBC.std().insets(5, 5, 5, 5).fill());

		profilesLabel = new JLabel("SAVED PROFILES");
		profilesLabel.setBounds(5, 460, 100, 20);

		chooseProfileButton = new JToggleButton("UNLOCK/LOCK");

		profilesPanel = new JPanel(new GridBagLayout());
		profilesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		profilesJList = new JList();
		profilesJList.setEnabled(false);

		JScrollPane scrollPane = new JScrollPane(profilesJList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		saveAsProfileButton = new JButton("Save as profile");
		deleteProfileButton = new JButton("Delete profile");

		GBC gbc = GBC.eol().fill().insets(5, 5, 5, 5);
		profilesPanel.add(scrollPane, gbc);
		profilesPanel.add(saveAsProfileButton, gbc.toggleEol());
		profilesPanel.add(deleteProfileButton, gbc.toggleEol());

		settingsGUIButton = new JButton("Settings");
		createAtlasButton = new JButton("Create Atlas");

		gbc_eol = GBC.eol().insets(5, 2, 5, 2).fill(GBC.HORIZONTAL);

		leftPanel.add(coordinatesLabel, gbc_eol);
		leftPanel.add(coordinatesPanel, gbc_eol);
		leftPanel.add(new JLabel("Map Source"), gbc_eol);
		leftPanel.add(mapSource, gbc_eol);
		leftPanel.add(zoomLevelLabel, gbc_std);
		leftPanel.add(amountOfTilesLabel, gbc_eol);
		leftPanel.add(zoomLevelPanel, gbc_eol);
		leftPanel.add(tileSizeLabel, gbc_eol);
		leftPanel.add(tileSizePanel, gbc_eol);
		leftPanel.add(atlasNameLabel, gbc_eol);
		leftPanel.add(atlasNamePanel, gbc_eol);
		leftPanel.add(profilesLabel, gbc_eol);
		leftPanel.add(chooseProfileButton, gbc_eol);
		leftPanel.add(profilesPanel, gbc_eol);
		leftPanel.add(settingsGUIButton, gbc_eol);
		leftPanel.add(createAtlasButton, gbc_eol);
		leftPanel.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));

		add(leftScrollPane, BorderLayout.WEST);

	}

	public void createRightPanel() {

		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		previewMap = new PreviewMap();
		previewMap.addMapSelectionListener(this);

		// Allows to disable map painting and tile loading
		// for debugging purposes
		// TODO Enable map preview
		//previewMap.setEnabled(false);

		rightPanel.add(previewMap, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.CENTER);
		createZoomLevelCheckBoxes();
	}

	protected void createZoomLevelCheckBoxes() {
		int zoomLevels = ((TileSource) mapSource.getSelectedItem()).getMaxZoom();
		JCheckBox[] oldCbZoom = cbZoom;
		cbZoom = new JCheckBox[zoomLevels];
		int x = cbZoom.length - 1;
		Object[] o = new Object[] { x--, x--, x-- };
		zoomLevelLabel.setText(String.format("ZOOM LEVELS (%d, %d, %d ... 2, 1, 0)", o));
		zoomLevelPanel.removeAll();
		zoomLevelPanel.setLayout(new GridLayout(2, cbZoom.length + 2 / 2, 0, 0));
		CheckBoxListener cbl = new CheckBoxListener();

		String s = "";
		for (int i = cbZoom.length - 1; i >= 0; i--) {
			JCheckBox cb = new JCheckBox();
			if (i < oldCbZoom.length)
				cb.setSelected(oldCbZoom[i].isSelected());
			cb.setPreferredSize(new Dimension(17, 20));
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
		addWindowListener(new WindowDestroyer());
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
		tileSizeWidthTextField.getDocument().addDocumentListener(jtfl);
		tileSizeHeightTextField.getDocument().addDocumentListener(jtfl);
		atlasNameTextField.getDocument().addDocumentListener(jtfl);

		chooseProfileButton.addActionListener(new JToggleButtonListener());
		JListListener jll = new JListListener();
		profilesJList.addListSelectionListener(jll);
		profilesJList.addMouseListener(jll);
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

		Settings settings = Settings.getInstance();
		atlasNameTextField.setText(settings.getAtlasName());
		previewMap.settingsLoadPosition();
		latMaxTextField.setCoordinate(settings.getSelectionMax().lat);
		lonMaxTextField.setCoordinate(settings.getSelectionMax().lon);
		latMinTextField.setCoordinate(settings.getSelectionMin().lat);
		lonMinTextField.setCoordinate(settings.getSelectionMin().lon);

		mapSource.setSelectedItem(MapSources.getSourceByName(settings.getDefaultMapSource()));

		fileSeparator = System.getProperty("file.separator");
		updateProfilesList();
		UpdateGUI.updateAllUIs();
	}

	protected void updateProfilesList() {
		// Load all profiles from the profiles file from disk
		profilesVector = PersistentProfiles.load(new File(System.getProperty("user.dir")
				+ fileSeparator + "profiles.xml"));
		profileNamesVector.clear();

		for (Profile p : profilesVector) {
			profileNamesVector.add(p.getProfileName());
		}
		profilesJList.setListData(profileNamesVector);
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

				result = Utilities.validateString(atlasNameTextField.getText());

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
			log.error("", e);
		}
	}

	// WindowDestroyer
	private class WindowDestroyer extends WindowAdapter {
		public void windowClosing(WindowEvent e) {

			Settings s = Settings.getInstance();
			previewMap.settingsSavePosition();
			s.setDefaultMapSource(((TileSource) mapSource.getSelectedItem()).getName());
			s.setAtlasName(atlasNameTextField.getText());
			s.setSelectionMax(new EastNorthCoordinate(latMaxTextField.getCoordinateOrNaN(),
					lonMaxTextField.getCoordinateOrNaN()));
			s.setSelectionMin(new EastNorthCoordinate(latMinTextField.getCoordinateOrNaN(),
					lonMinTextField.getCoordinateOrNaN()));
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

			tileSizeWidthComboBox.setEnabled(!tileSizeWidthTextField.isInputValid());
			tileSizeHeightComboBox.setEnabled(!tileSizeHeightTextField.isInputValid());
			if (handleInputInRealTime()) {
				calculateNrOfTilesToDownload();
			}
		}

		public void removeUpdate(DocumentEvent e) {

			tileSizeWidthComboBox.setEnabled(!tileSizeWidthTextField.isInputValid());
			tileSizeHeightComboBox.setEnabled(!tileSizeHeightTextField.isInputValid());
			if (handleInputInRealTime()) {
				calculateNrOfTilesToDownload();
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}
	}

	private boolean handleInputInRealTime() {

		if (!(getFocusOwner() instanceof JTextField))
			return false;
		String input = "";

		JTextField textField = (JTextField) getFocusOwner();
		input = textField.getText();

		if (input.length() == 0)
			return false;

		if (atlasNameTextField.equals(textField)) {
			int result = -1;
			result = Utilities.validateString(input);
			if (result > -1) {
				JOptionPane.showMessageDialog(null, "\"" + (char) result + "\" is not valid input",
						"Error", JOptionPane.ERROR_MESSAGE);
				return false;
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
			return;
		}

		boolean maxIsBiggerThanMin = true;

		maxIsBiggerThanMin = validateLatLongMinMax();

		if (maxIsBiggerThanMin) {

			int tileSizeWidth = 256;
			int tileSizeHeight = 256;

			try {
				if (tileSizeWidthTextField.isInputValid())
					tileSizeWidth = tileSizeWidthTextField.getTileSize();
				else
					tileSizeWidth = ((Integer) tileSizeWidthComboBox.getSelectedItem()).intValue();

				if (tileSizeHeightTextField.isInputValid())
					tileSizeHeight = tileSizeHeightTextField.getTileSize();
				else
					tileSizeHeight = ((Integer) tileSizeHeightComboBox.getSelectedItem())
							.intValue();

				TileSource tileSource = (TileSource) mapSource.getSelectedItem();
				SelectedZoomLevels sZL = new SelectedZoomLevels(cbZoom);
				Thread atlasThread = new AtlasThread(atlasNameTextField.getText(), tileSource,
						getMapSelectionCoordinates(), sZL, tileSizeWidth, tileSizeHeight);
				atlasThread.start();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		System.gc();
	}

	private void profileSaveAs() {

		String inputValue = JOptionPane.showInputDialog("Profile Name");

		if (inputValue != null) {

			Profile profile = new Profile();

			for (int i = 0; i < profileNamesVector.size(); i++) {
				if (inputValue.equals(profileNamesVector.elementAt(i))) {
					JOptionPane.showMessageDialog(null,
							"Profile name already exists, choose a different name", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			String errorDescription = "";
			MapSelection ms = checkCoordinates();

			if (!ms.coordinatesAreValid())
				errorDescription += "Coordinates are not all valid - please check";

			if (!tileSizeWidthComboBox.isEnabled() && !tileSizeWidthTextField.isInputValid())
				errorDescription += "Invalid format of \"Custom size\" (TILE SIZE) value\n";

			if (!tileSizeHeightComboBox.isEnabled() && !tileSizeHeightTextField.isValid())
				errorDescription += "Invalid format of \"Custom size\" (TILE SIZE) value\n";

			if (errorDescription.length() > 0) {
				JOptionPane.showMessageDialog(null, errorDescription, "Errors",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				profile.setProfileName(inputValue);
				profile.setAtlasName(atlasNameTextField.getText());
				profile.setMapSource(((TileSource) mapSource.getSelectedItem()).getName());
				profile.setLatitudeMax(latMaxTextField.getCoordinate());
				profile.setLatitudeMin(latMinTextField.getCoordinate());
				profile.setLongitudeMax(lonMaxTextField.getCoordinate());
				profile.setLongitudeMin(lonMinTextField.getCoordinate());

				boolean[] zoomLevels = new boolean[cbZoom.length];
				for (int i = 0; i < cbZoom.length; i++) {
					zoomLevels[i] = cbZoom[i].isSelected();
				}

				profile.setZoomLevels(zoomLevels);

				if (tileSizeWidthTextField.isInputValid()) {
					profile.setTileSizeWidth(tileSizeWidthTextField.getTileSize());
				} else {
					profile.setTileSizeWidth(((Integer) tileSizeWidthComboBox.getSelectedItem())
							.intValue());
				}

				if (tileSizeHeightTextField.isInputValid()) {
					profile.setTileSizeHeight(tileSizeHeightTextField.getTileSize());
				} else {
					profile.setTileSizeHeight(((Integer) tileSizeHeightComboBox.getSelectedItem())
							.intValue());
				}

				profilesVector.addElement(profile);
				PersistentProfiles.store(profilesVector);
				updateProfilesList();
			} catch (ParseException e) {
				log.error("", e);
			}
		}
	}

	private void profileLoad(Profile profile) {
		TileSource map = MapSources.getSourceByName(profile.getMapSource());
		mapSource.setSelectedItem(map);
		mapSourceChanged();

		latMinTextField.setCoordinate(profile.getLatitudeMin());
		latMaxTextField.setCoordinate(profile.getLatitudeMax());
		lonMinTextField.setCoordinate(profile.getLongitudeMin());
		lonMaxTextField.setCoordinate(profile.getLongitudeMax());

		int tileSizeWidth = profile.getTileSizeWidth();
		int tileSizeHeight = profile.getTileSizeHeight();
		int index;
		index = tileSizeValues.indexOf(new Integer(tileSizeWidth));
		if (index >= 0) {
			tileSizeWidthComboBox.setSelectedIndex(index);
			tileSizeWidthTextField.setTileSize(0);
		} else {
			tileSizeWidthTextField.setTileSize(tileSizeWidth);
		}
		index = tileSizeValues.indexOf(new Integer(tileSizeHeight));
		if (index >= 0) {
			tileSizeHeightComboBox.setSelectedIndex(index);
			tileSizeHeightTextField.setTileSize(0);
		} else {
			tileSizeHeightTextField.setTileSize(tileSizeHeight);
		}

		atlasNameTextField.setText(profile.getAtlasName());

		boolean[] zoomValues = new boolean[cbZoom.length];

		zoomValues = profile.getZoomLevels();

		int min = Math.min(cbZoom.length, zoomValues.length);
		for (int i = 0; i < min; i++) {
			cbZoom[i].setSelected(zoomValues[i]);
		}

		/**
		 * Calculate amount of tiles to download
		 */
		calculateNrOfTilesToDownload();
		previewSelection();
	}

	private void profileDeleteSelected() {
		if (profilesJList.isEnabled() && profilesJList.getSelectedIndex() > -1) {

			profilesVector.removeElementAt(profilesJList.getSelectedIndex());
			PersistentProfiles.store(profilesVector);
			updateProfilesList();
		}
	}

	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();

			if (actionCommand.equals("Create Atlas"))
				createAtlas();
			else if (actionCommand.equals("Save as profile"))
				profileSaveAs();
			else if (actionCommand.equals("Delete profile"))
				profileDeleteSelected();
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

	private class JListListener extends MouseAdapter implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			loadSelectedProfile();
		}

		protected void loadSelectedProfile() {
			int selectedIndex = profilesJList.getSelectedIndex();

			if (selectedIndex != -1) {
				Profile profile = new Profile();

				profile = profilesVector.elementAt(selectedIndex);
				profileLoad(profile);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2)
				return;
			loadSelectedProfile();
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