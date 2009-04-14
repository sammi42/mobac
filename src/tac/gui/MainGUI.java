package tac.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.StartTAC;
import tac.gui.components.AtlasTree;
import tac.gui.components.JAtlasNameField;
import tac.gui.components.JCoordinateField;
import tac.gui.components.JTileSizeCombo;
import tac.gui.mapview.GridZoom;
import tac.gui.mapview.MapSelectionListener;
import tac.gui.mapview.PreviewMap;
import tac.mapsources.MapSources;
import tac.program.AtlasThread;
import tac.program.MapCreatorCustom;
import tac.program.MapSelection;
import tac.program.SelectedZoomLevels;
import tac.program.Settings;
import tac.program.TACInfo;
import tac.program.MapCreatorCustom.TileImageFormat;
import tac.program.model.Atlas;
import tac.program.model.AtlasOutputFormat;
import tac.program.model.AutoCutMultiMapLayer;
import tac.program.model.EastNorthCoordinate;
import tac.program.model.Profile;
import tac.program.model.TileImageColorDepth;
import tac.utilities.GBC;
import tac.utilities.PersistentProfiles;
import tac.utilities.TACExceptionHandler;
import tac.utilities.Utilities;

public class MainGUI extends JFrame implements MapSelectionListener {

	private static final long serialVersionUID = -8444942802691874960L;

	private static Logger log = Logger.getLogger(MainGUI.class);

	private static Color labelBackgroundColor = new Color(0, 0, 0, 127);
	private static Color labelForegroundColor = Color.white;

	private Vector<Profile> profilesVector = new Vector<Profile>();

	private AtlasTree atlasTree;
	private PreviewMap previewMap;

	private JLabel zoomLevelText;
	private JComboBox gridZoomCombo;
	private JSlider zoomSlider;
	private JComboBox mapSourceCombo;
	private JButton helpButton;
	private JButton fullScreenButton;
	private JButton settingsButton;
	private JComboBox profilesCombo;
	private JButton deleteProfileButton;
	private JButton saveAsProfileButton;
	private JAtlasNameField atlasNameTextField;
	private JComboBox atlasOutputFormatCombo;
	private JButton createAtlasButton;
	private JPanel zoomLevelPanel;
	private JCheckBox[] cbZoom = new JCheckBox[0];
	private JLabel amountOfTilesLabel;

	private JCoordinateField latMinTextField;
	private JCoordinateField latMaxTextField;
	private JCoordinateField lonMinTextField;
	private JCoordinateField lonMaxTextField;
	private JCheckBox enableCustomTileProcessingCheckButton;
	private JLabel tileSizeWidthLabel;
	private JLabel tileSizeHeightLabel;
	private JLabel tileColorDepthLabel;
	private JTileSizeCombo tileSizeWidth;
	private JTileSizeCombo tileSizeHeight;
	private JComboBox tileColorDepth;

	private JPanel mapControlPanel = new JPanel(new BorderLayout());
	private JPanel leftPanel = new JPanel(new GridBagLayout());

	private Settings settings;

	public MainGUI() {
		super();
		TACExceptionHandler.registerForCurrentThread();
		settings = Settings.getInstance();
		setTitle(TACInfo.getCompleteTitle());

		log.trace("Creating main dialog - " + getTitle());
		setResizable(true);
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		setMinimumSize(new Dimension(Math.min(800, dScreen.width), Math.min(590, dScreen.height)));
		setSize(getMinimumSize());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowDestroyer());
		addComponentListener(new MainWindowListener());

		previewMap = new PreviewMap();
		previewMap.addMapSelectionListener(this);

		createControls();
		calculateNrOfTilesToDownload();
		setLayout(new BorderLayout());
		add(leftPanel, BorderLayout.WEST);
		JLayeredPane layeredPane = new FilledLayeredPane();
		layeredPane.add(previewMap, new Integer(0));
		layeredPane.add(mapControlPanel, new Integer(1));
		add(layeredPane, BorderLayout.CENTER);

		Utilities.checkFileSetup();
		loadSettings();
		updatePanels();
		initializeProfilesCombo();
		updateZoomLevelCheckBoxes();
		updateGridSizeCombo();
		updateCustomTileProcessingControlsState();
		zoomChanged(previewMap.getZoom());
	}

	private void createControls() {
		// zoom slider
		zoomSlider = new JSlider(JMapViewer.MIN_ZOOM, previewMap.getTileSource().getMaxZoom());
		zoomSlider.setOrientation(JSlider.HORIZONTAL);
		zoomSlider.setSize(20, zoomSlider.getPreferredSize().height);
		zoomSlider.addChangeListener(new ZoomSliderListener());
		zoomSlider.setOpaque(false);

		// zoom level text
		zoomLevelText = new JLabel(" 00 ");
		zoomLevelText.setOpaque(true);
		zoomLevelText.setBackground(labelBackgroundColor);
		zoomLevelText.setForeground(labelForegroundColor);
		zoomLevelText.setToolTipText("The current zoom level");

		// grid zoom combo
		gridZoomCombo = new JComboBox();
		gridZoomCombo.setEditable(false);
		gridZoomCombo.addActionListener(new GridZoomComboListener());
		gridZoomCombo.setToolTipText("Add a grid of the spefified zoom level to the map");

		// map source combo
		mapSourceCombo = new JComboBox(MapSources.getMapSources());
		mapSourceCombo.setMaximumRowCount(20);
		mapSourceCombo.addActionListener(new MapSourceComboListener());
		mapSourceCombo.setToolTipText("Select map source");

		// help button
		helpButton = new JButton("Help");
		helpButton.addActionListener(new HelpButtonListener());
		helpButton.setToolTipText("Display some help information");

		// settings button
		settingsButton = new JButton("Settings");
		settingsButton.addActionListener(new SettingsButtonListener());
		settingsButton.setToolTipText("Open the preferences dialogue panel.");

		// full screen
		fullScreenButton = new JButton("Full screen off");
		fullScreenButton.addActionListener(new FullScreenButtonListener());
		fullScreenButton.setToolTipText("Toggle full screen.");

		// profiles combo box
		profilesCombo = new JComboBox();
		profilesCombo.setEditable(true);
		profilesCombo.setToolTipText("Select an atlas creation profile\n "
				+ "or enter a name for a new profile");

		// delete profile button
		deleteProfileButton = new JButton("Delete profile");
		deleteProfileButton.addActionListener(new DeleteProfileListener());
		deleteProfileButton.setToolTipText("Delete atlas profile from list");

		// save as profile button
		saveAsProfileButton = new JButton("Save as profile");
		saveAsProfileButton.addActionListener(new SaveAsProfileListener());
		saveAsProfileButton.setToolTipText("Save atlas profile");

		// atlas output format
		atlasOutputFormatCombo = new JComboBox(AtlasOutputFormat.values());

		// atlas name text field
		atlasNameTextField = new JAtlasNameField();
		atlasNameTextField.setColumns(12);
		atlasNameTextField.setActionCommand("atlasNameTextField");
		atlasNameTextField.setToolTipText("Enter a name for the atlas here");

		// create atlas button
		createAtlasButton = new JButton("Create atlas");
		createAtlasButton.addActionListener(new CreateAtlasButtonListener());
		createAtlasButton.setToolTipText("Create the atlas");

		// zoom level check boxes
		zoomLevelPanel = new JPanel();
		zoomLevelPanel.setBorder(BorderFactory.createEmptyBorder());
		zoomLevelPanel.setOpaque(false);

		// amount of tiles to download
		amountOfTilesLabel = new JLabel();
		amountOfTilesLabel.setToolTipText("Total amount of tiles to download");
		amountOfTilesLabel.setOpaque(true);
		amountOfTilesLabel.setBackground(labelBackgroundColor);
		amountOfTilesLabel.setForeground(labelForegroundColor);

		// coordinates panel
		latMaxTextField = new JCoordinateField(MapSelection.LAT_MIN, MapSelection.LAT_MAX, true);
		latMaxTextField.setActionCommand("latMaxTextField");
		lonMinTextField = new JCoordinateField(MapSelection.LON_MIN, MapSelection.LON_MAX, false);
		lonMinTextField.setActionCommand("longMinTextField");
		lonMaxTextField = new JCoordinateField(MapSelection.LON_MIN, MapSelection.LON_MAX, true);
		lonMaxTextField.setActionCommand("longMaxTextField");
		latMinTextField = new JCoordinateField(MapSelection.LAT_MIN, MapSelection.LAT_MAX, false);
		latMinTextField.setActionCommand("latMinTextField");

		// custom tile size
		enableCustomTileProcessingCheckButton = new JCheckBox(
				"Recreate/adjust map tiles (CPU intensive)");
		enableCustomTileProcessingCheckButton
				.addActionListener(new EnableCustomTileSizeCheckButtonListener());
		enableCustomTileProcessingCheckButton
				.setToolTipText("<html>If this option is disabled each "
						+ "map tile (size: 256x256) is used axactly as downloaded "
						+ "from the server (faster).<br>"
						+ "Otherwise each tile is newly created which allows to "
						+ "use custom tile size (slower / CPU intensive).</html>");

		tileSizeWidthLabel = new JLabel("Width:");
		tileSizeWidth = new JTileSizeCombo();
		tileSizeWidth.setToolTipText("Width");

		tileSizeHeightLabel = new JLabel("Height:");
		tileSizeHeight = new JTileSizeCombo();
		tileSizeHeight.setToolTipText("Height");

		tileColorDepthLabel = new JLabel("Color depth:");
		tileColorDepth = new JComboBox(TileImageColorDepth.values());
		if (!Utilities.testJaiColorQuantizerAvailable()) {
			tileColorDepth.setEnabled(false);
			tileColorDepthLabel.setEnabled(false);
			tileColorDepth.setToolTipText("<html>This feature is deactivated because <br>"
					+ "<b>Java Advanced Image library was not found </b>"
					+ "(jai_core.jar & jai_codec.jar)<br>"
					+ "For more details please see the file <b>README.HTM</b> "
					+ "in section <b>Requirements</b>.</html>");
		}
	}

	private void updateLeftPanel() {
		leftPanel.removeAll();

		// Coordinates Panel
		JPanel coordinatesPanel = new JPanel(new GridBagLayout());
		coordinatesPanel.setBorder(BorderFactory
				.createTitledBorder("Selection coordinates (min/max)"));

		JLabel latMaxLabel = new JLabel("N ", JLabel.CENTER);
		JLabel lonMinLabel = new JLabel("W ", JLabel.CENTER);
		JLabel lonMaxLabel = new JLabel("E ", JLabel.CENTER);
		JLabel latMinLabel = new JLabel("S ", JLabel.CENTER);

		JButton displaySelectionButton = new JButton("Display selection");
		displaySelectionButton.addActionListener(new DisplaySelectionButtonListener());

		coordinatesPanel.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		coordinatesPanel.add(latMaxLabel);
		coordinatesPanel.add(latMaxTextField);
		coordinatesPanel.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));

		JPanel eastWestPanel = new JPanel(new GridBagLayout());
		eastWestPanel.add(lonMinLabel);
		eastWestPanel.add(lonMinTextField);
		eastWestPanel.add(lonMaxLabel, GBC.std().insets(10, 0, 0, 0));
		eastWestPanel.add(lonMaxTextField);
		coordinatesPanel.add(eastWestPanel, GBC.eol().fill().insets(0, 5, 0, 5));

		coordinatesPanel.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		coordinatesPanel.add(latMinLabel);
		coordinatesPanel.add(latMinTextField);
		coordinatesPanel.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));

		coordinatesPanel.add(displaySelectionButton, GBC.eol().anchor(GBC.CENTER)
				.insets(0, 5, 0, 0));

		JPanel mapSourcePanel = new JPanel(new GridBagLayout());
		mapSourcePanel.setBorder(BorderFactory.createTitledBorder("Map source"));
		mapSourcePanel.add(mapSourceCombo, GBC.std().insets(2, 2, 2, 2).fill());

		JPanel zoomLevelsPanel = new JPanel(new GridBagLayout());
		zoomLevelsPanel.setBorder(BorderFactory.createTitledBorder("Zoom Levels"));
		zoomLevelsPanel.add(zoomLevelPanel, GBC.eol());
		zoomLevelsPanel.add(amountOfTilesLabel, GBC.std().anchor(GBC.WEST).insets(0, 5, 0, 0));

		JPanel tileProcessingPanel = new JPanel(new GridBagLayout());
		tileProcessingPanel.setBorder(BorderFactory.createTitledBorder("Custom tile processing"));

		GBC gbc_std = GBC.std().insets(5, 2, 5, 3);
		GBC gbc_eol = GBC.eol().insets(5, 2, 5, 3);

		tileProcessingPanel.add(enableCustomTileProcessingCheckButton, gbc_eol);
		JPanel tileSizePanel = new JPanel(new GridBagLayout());
		tileSizePanel.add(tileSizeWidthLabel, gbc_std);
		tileSizePanel.add(tileSizeWidth, gbc_std);
		tileSizePanel.add(tileSizeHeightLabel, gbc_std);
		tileSizePanel.add(tileSizeHeight, gbc_eol);
		tileProcessingPanel.add(tileSizePanel, GBC.eol());
		JPanel tileColorDepthPanel = new JPanel();
		tileColorDepthPanel.add(tileColorDepthLabel);
		tileColorDepthPanel.add(tileColorDepth);
		tileProcessingPanel.add(tileColorDepthPanel, GBC.eol());

		JPanel atlasContentPanel = new JPanel(new GridBagLayout());
		if (settings.isDevModeEnabled()) {
			atlasContentPanel.setBorder(BorderFactory.createTitledBorder("Atlas Content"));
			atlasTree = new AtlasTree(previewMap);
			JScrollPane treeScrollPane = new JScrollPane(atlasTree,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			treeScrollPane.setPreferredSize(new Dimension(100, 100));
			atlasContentPanel.add(treeScrollPane, GBC.eol().fill());
			JButton clearAtlas = new JButton("Clear");
			atlasContentPanel.add(clearAtlas, GBC.std());
			clearAtlas.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					atlasTree.clearAtlas();
				}
			});
			JButton addLayers = new JButton("Add selection");
			atlasContentPanel.add(addLayers, GBC.std());
			addLayers.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					addSelectedAutoCutMultiMapLayers();
				}
			});
		}
		JPanel profilesPanel = new JPanel(new GridBagLayout());
		profilesPanel.setBorder(BorderFactory.createTitledBorder("Saved profiles"));

		GBC gbc = GBC.eol().fill().insets(5, 5, 5, 5);
		profilesPanel.add(profilesCombo, gbc);
		profilesPanel.add(saveAsProfileButton, gbc.toggleEol());
		profilesPanel.add(deleteProfileButton, gbc.toggleEol());

		JPanel atlasNamePanel = new JPanel(new GridBagLayout());
		atlasNamePanel.setBorder(BorderFactory.createTitledBorder("Atlas settings"));
		atlasNamePanel.add(new JLabel("Name: "), gbc_std);
		atlasNamePanel.add(atlasNameTextField, gbc_eol.fill());
		atlasNamePanel.add(new JLabel("Format: "), gbc_std);
		atlasNamePanel.add(atlasOutputFormatCombo, gbc_eol);
		atlasNamePanel.add(createAtlasButton, gbc_eol.fill());

		gbc_eol = GBC.eol().insets(5, 2, 5, 2).fill(GBC.HORIZONTAL);

		JPanel leftPanelContent = new JPanel(new GridBagLayout());
		leftPanelContent.add(coordinatesPanel, gbc_eol);
		leftPanelContent.add(mapSourcePanel, gbc_eol);
		leftPanelContent.add(zoomLevelsPanel, gbc_eol);
		leftPanelContent.add(tileProcessingPanel, gbc_eol);
		if (settings.isDevModeEnabled())
			leftPanelContent.add(atlasContentPanel, gbc_eol);

		leftPanelContent.add(atlasNamePanel, gbc_eol);
		leftPanelContent.add(profilesPanel, gbc_eol);
		leftPanelContent.add(settingsButton, gbc_eol);
		leftPanelContent.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

		JScrollPane scrollPane = new JScrollPane(leftPanelContent);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		// Set the scroll pane width large enough so that the
		// scroll bar has enough space to appear right to it
		// Dimension d = scrollPane.getPreferredSize();
		// d.width += 5 + scrollPane.getVerticalScrollBar().getWidth();
		// scrollPane.setPreferredSize(d);
		// scrollPane.setMinimumSize(d);
		leftPanel.add(scrollPane, GBC.std().fill());
	}

	/**
	 * Updates the panel that holds all controls which are placed
	 * "inside"/"over" the preview map.
	 */
	private JPanel updateMapControlsPanel(boolean fullScreenEnabled) {
		mapControlPanel.removeAll();
		mapControlPanel.setOpaque(false);

		// zoom label
		JLabel zoomLabel = new JLabel(" Zoom: ");
		zoomLabel.setOpaque(true);
		zoomLabel.setBackground(labelBackgroundColor);
		zoomLabel.setForeground(labelForegroundColor);

		// top panel
		JPanel topControls = new JPanel(new GridBagLayout());
		topControls.setOpaque(false);
		topControls.add(zoomLabel, GBC.std().insets(5, 5, 0, 0));
		topControls.add(zoomSlider, GBC.std().insets(0, 5, 0, 0));
		topControls.add(zoomLevelText, GBC.std().insets(0, 5, 0, 0));
		topControls.add(gridZoomCombo, GBC.std().insets(10, 5, 0, 0));
		topControls.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		if (fullScreenEnabled)
			topControls.add(mapSourceCombo, GBC.std().insets(20, 5, 20, 0));
		topControls.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		if (fullScreenEnabled)
			topControls.add(settingsButton, GBC.std().insets(20, 5, 0, 0));
		topControls.add(helpButton, GBC.std().insets(10, 5, 5, 0));
		mapControlPanel.add(topControls, BorderLayout.NORTH);

		// bottom panel
		JPanel bottomControls = new JPanel(new GridBagLayout());
		bottomControls.setOpaque(false);
		bottomControls.add(fullScreenButton, GBC.std().insets(5, 0, 0, 5));
		bottomControls.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		if (fullScreenEnabled) {
			// atlas name label
			JLabel atlasNameLabel = new JLabel(" Atlas name ");
			atlasNameLabel.setOpaque(true);
			atlasNameLabel.setBackground(labelBackgroundColor);
			atlasNameLabel.setForeground(labelForegroundColor);

			bottomControls.add(profilesCombo, GBC.std().insets(5, 0, 0, 5));
			bottomControls.add(deleteProfileButton, GBC.std().insets(10, 0, 0, 5));
			bottomControls.add(saveAsProfileButton, GBC.std().insets(10, 0, 0, 5));
			bottomControls.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
			bottomControls.add(atlasNameLabel, GBC.std().insets(0, 0, 0, 5));
			bottomControls.add(atlasNameTextField, GBC.std().insets(0, 0, 0, 5));
			bottomControls.add(createAtlasButton, GBC.std().insets(10, 0, 5, 5));
		}
		mapControlPanel.add(bottomControls, BorderLayout.SOUTH);

		// left controls panel
		if (fullScreenEnabled) {
			// zoom levels label
			JLabel label = new JLabel(" Zoom levels ");
			label.setOpaque(true);
			label.setBackground(labelBackgroundColor);
			label.setForeground(labelForegroundColor);
			label.setToolTipText("Select the zoom levels to include in the atlas");

			JPanel leftControls = new JPanel(new GridBagLayout());
			leftControls.add(label, GBC.eol().insets(5, 20, 0, 0));
			leftControls.add(zoomLevelPanel, GBC.eol().insets(0, 5, 0, 0));
			leftControls.add(amountOfTilesLabel, GBC.eol().insets(5, 10, 0, 10));
			leftControls.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));
			leftControls.setOpaque(false);
			mapControlPanel.add(leftControls, BorderLayout.WEST);
		}
		return mapControlPanel;
	}

	private void updatePanels() {
		boolean fullScreenEnabled = settings.getFullScreenEnabled();

		updateMapControlsPanel(fullScreenEnabled);

		if (fullScreenEnabled) {
			leftPanel.setVisible(false);
			fullScreenButton.setText("Full screen off");
		} else {
			updateLeftPanel();
			leftPanel.setVisible(true);
			fullScreenButton.setText("Full screen");
		}
		calculateNrOfTilesToDownload();
		updateZoomLevelCheckBoxes();
		previewMap.grabFocus();
	}

	private void loadSettings() {
		Settings settings = Settings.getInstance();
		atlasNameTextField.setText(settings.getAtlasName());
		atlasOutputFormatCombo.setSelectedItem(settings.getAtlasOutputFormat());
		previewMap.settingsLoadPosition();
		latMaxTextField.setCoordinate(settings.getSelectionMax().lat);
		lonMaxTextField.setCoordinate(settings.getSelectionMax().lon);
		latMinTextField.setCoordinate(settings.getSelectionMin().lat);
		lonMinTextField.setCoordinate(settings.getSelectionMin().lon);
		tileColorDepth.setSelectedItem(settings.getTileColorDepth());

		mapSourceCombo.setSelectedItem(MapSources.getSourceByName(settings.getDefaultMapSource()));

		enableCustomTileProcessingCheckButton.setSelected(settings.isCustomTileSize());
		tileSizeHeight.setValue(settings.getTileHeight());
		tileSizeWidth.setValue(settings.getTileWidth());

		setSize(settings.getWindowDimension());
		Point windowLocation = settings.getWindowLocation();
		if (windowLocation.x == -1 && windowLocation.y == -1) {
			setLocationRelativeTo(null);
		} else {
			setLocation(windowLocation);
		}
		if (settings.getWindowMaximized())
			setExtendedState(Frame.MAXIMIZED_BOTH);
	}

	private void saveSettings() {
		try {
			Settings s = Settings.getInstance();
			previewMap.settingsSavePosition();
			s.setDefaultMapSource(((TileSource) mapSourceCombo.getSelectedItem()).getName());
			s.setAtlasName(atlasNameTextField.getText());
			s.setAtlasOutputFormat((AtlasOutputFormat) atlasOutputFormatCombo.getSelectedItem());
			s.setSelectionMax(new EastNorthCoordinate(latMaxTextField.getCoordinateOrNaN(),
					lonMaxTextField.getCoordinateOrNaN()));
			s.setSelectionMin(new EastNorthCoordinate(latMinTextField.getCoordinateOrNaN(),
					lonMinTextField.getCoordinateOrNaN()));

			s.setCustomTileSize(enableCustomTileProcessingCheckButton.isSelected());
			s.setTileWidth(tileSizeWidth.getValue());
			s.setTileHeight(tileSizeHeight.getValue());
			s.setTileColorDepth((TileImageColorDepth) tileColorDepth.getSelectedItem());
			boolean maximized = (getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
			s.setWindowMaximized(maximized);
			if (!maximized) {
				s.setWindowDimension(getSize());
				s.setWindowLocation(getLocation());
			}
			s.store();
		} catch (Exception e) {
			TACExceptionHandler.showExceptionDialog(e);
			JOptionPane.showMessageDialog(null,
					"Error on writing program settings to \"settings.xml\"", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private class ZoomSliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			previewMap.setZoom(zoomSlider.getValue());
		}
	}

	private class GridZoomComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			GridZoom g = (GridZoom) gridZoomCombo.getSelectedItem();
			if (g == null)
				return;
			previewMap.setGridZoom(g.getZoom());
			repaint();
			previewMap.updateMapSelection();
		}
	}

	private void updateGridSizeCombo() {
		int maxZoom = previewMap.getTileSource().getMaxZoom();
		int minZoom = previewMap.getTileSource().getMinZoom();
		GridZoom lastGridZoom = (GridZoom) gridZoomCombo.getSelectedItem();
		gridZoomCombo.removeAllItems();
		gridZoomCombo.setMaximumRowCount(maxZoom + 1);
		gridZoomCombo.addItem(new GridZoom(-1) {

			@Override
			public String toString() {
				return "Grid disabled";
			}

		});
		for (int i = maxZoom; i >= minZoom; i--) {
			gridZoomCombo.addItem(new GridZoom(i));
		}
		if (lastGridZoom != null)
			gridZoomCombo.setSelectedItem(lastGridZoom);
	}

	private class DisplaySelectionButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			previewSelection();
		}
	}

	private class EnableCustomTileSizeCheckButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			updateCustomTileProcessingControlsState();
		}
	}

	private class MapSourceComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			previewMap.setTileSource((TileSource) mapSourceCombo.getSelectedItem());
			zoomSlider.setMinimum(previewMap.getTileSource().getMinZoom());
			zoomSlider.setMaximum(previewMap.getTileSource().getMaxZoom());
			updateGridSizeCombo();
			updateZoomLevelCheckBoxes();
		}
	}

	private class HelpButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			DataInputStream in = new DataInputStream(StartTAC.class
					.getResourceAsStream("resources/text/help_dialog.html"));
			byte[] buf;
			try {
				buf = new byte[in.available()];
				in.readFully(buf);
				in.close();
				String helpMessage = new String(buf, "UTF-8");
				// Strip out all line breaks because JOptionPane shows
				// the raw HTML code otherwise
				helpMessage = helpMessage.replaceAll("\n", "");
				JOptionPane.showMessageDialog(null, helpMessage, "Help",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	private class FullScreenButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Settings settings = Settings.getInstance();
			settings.setFullScreenEnabled(!settings.getFullScreenEnabled());
			updatePanels();
		}
	}

	private Profile getProfile(String profileName) {
		for (Profile profile : profilesVector) {
			if (profile.getProfileName().equals(profileName))
				return profile;
		}
		return null;
	}

	private void initializeProfilesCombo() {
		// Load all profiles from the profiles file from disk
		profilesVector = PersistentProfiles.load(new File(System.getProperty("user.dir"),
				"profiles.xml"));

		for (Profile p : profilesVector) {
			profilesCombo.addItem(p.getProfileName());
		}
		profilesCombo.setSelectedIndex(-1);
		ProfilesComboListener pcl = new ProfilesComboListener();
		profilesCombo.addActionListener(pcl);

		deleteProfileButton.setEnabled(false);
	}

	private class ProfilesComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Profile profile = getProfile((String) profilesCombo.getEditor().getItem());
			if (profile != null) {
				TileSource map = MapSources.getSourceByName(profile.getMapSource());
				mapSourceCombo.setSelectedItem(map);

				latMinTextField.setCoordinate(profile.getLatitudeMin());
				latMaxTextField.setCoordinate(profile.getLatitudeMax());
				lonMinTextField.setCoordinate(profile.getLongitudeMin());
				lonMaxTextField.setCoordinate(profile.getLongitudeMax());

				tileSizeWidth.setValue(profile.getTileSizeWidth());
				tileSizeHeight.setValue(profile.getTileSizeHeight());

				atlasNameTextField.setText(profile.getAtlasName());

				boolean[] zoomValues = new boolean[cbZoom.length];

				zoomValues = profile.getZoomLevels();

				int min = Math.min(cbZoom.length, zoomValues.length);
				for (int i = 0; i < min; i++) {
					cbZoom[i].setSelected(zoomValues[i]);
				}

				calculateNrOfTilesToDownload();
				previewSelection();
				deleteProfileButton.setEnabled(true);
			} else {
				deleteProfileButton.setEnabled(false);
			}
		}
	}

	private class DeleteProfileListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			profilesVector.removeElementAt(profilesCombo.getSelectedIndex());
			PersistentProfiles.store(profilesVector);
			int index = profilesCombo.getSelectedIndex();
			profilesCombo.setSelectedIndex(-1);
			profilesCombo.removeItemAt(index);
		}

	}

	private class SaveAsProfileListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String errorText = validateInput(true);

			if (errorText.length() > 0) {
				JOptionPane.showMessageDialog(null, errorText, "Errors", JOptionPane.ERROR_MESSAGE);
				return;
			}

			String profileName = (String) profilesCombo.getEditor().getItem();

			if (profileName.length() == 0) {
				JOptionPane.showMessageDialog(null, "Please enter a profile name", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			Profile previousProfile = getProfile(profileName);

			if (previousProfile != null) {
				int response = JOptionPane.showConfirmDialog(null, "Profile \"" + profileName
						+ "\" already exists. Overwrite?", "Please confirm",
						JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.NO_OPTION)
					return;
			}

			Profile profile;
			if (previousProfile != null)
				profile = previousProfile;
			else
				profile = new Profile();

			try {
				profile.setProfileName(profileName);
				profile.setAtlasName(atlasNameTextField.getText());
				profile.setMapSource(((TileSource) mapSourceCombo.getSelectedItem()).getName());
				profile.setLatitudeMax(latMaxTextField.getCoordinate());
				profile.setLatitudeMin(latMinTextField.getCoordinate());
				profile.setLongitudeMax(lonMaxTextField.getCoordinate());
				profile.setLongitudeMin(lonMinTextField.getCoordinate());

				boolean[] zoomLevels = new boolean[cbZoom.length];
				for (int i = 0; i < cbZoom.length; i++) {
					zoomLevels[i] = cbZoom[i].isSelected();
				}

				profile.setZoomLevels(zoomLevels);
				profile.setTileSizeWidth(tileSizeWidth.getValue());
				profile.setTileSizeHeight(tileSizeHeight.getValue());

				if (previousProfile == null) {
					profilesVector.addElement(profile);
					profilesCombo.addItem(profileName);
				}
				PersistentProfiles.store(profilesVector);
				deleteProfileButton.setEnabled(true);
				JOptionPane.showMessageDialog(null, "Saved profile " + profileName, "",
						JOptionPane.PLAIN_MESSAGE);

			} catch (ParseException exception) {
				log.error("", exception);
			}
		}
	}

	private class SettingsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			SettingsGUI.showSettingsDialog(MainGUI.this);
		}
	}

	private class CreateAtlasButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String errorText = validateInput(true);
			if (errorText.length() > 0) {
				JOptionPane.showMessageDialog(null, errorText, "Errors", JOptionPane.ERROR_MESSAGE);
				return;
			}

			boolean maxIsBiggerThanMin = true;

			maxIsBiggerThanMin = validateLatLongMinMax();

			if (maxIsBiggerThanMin) {

				boolean customTileSize = enableCustomTileProcessingCheckButton.isSelected();
				MapCreatorCustom.TileImageParameters customTileParameters = null;
				if (customTileSize) {
					customTileParameters = new MapCreatorCustom.TileImageParameters();
					customTileParameters.width = tileSizeWidth.getValue();
					customTileParameters.height = tileSizeHeight.getValue();
					customTileParameters.colorDepth = (TileImageColorDepth) tileColorDepth
							.getSelectedItem();
					customTileParameters.format = TileImageFormat.Unchanged;
				}

				try {
					TileSource tileSource = (TileSource) mapSourceCombo.getSelectedItem();
					SelectedZoomLevels sZL = new SelectedZoomLevels(previewMap.getTileSource()
							.getMinZoom(), cbZoom);
					AtlasOutputFormat atlasOutputFormat = (AtlasOutputFormat) atlasOutputFormatCombo
							.getSelectedItem();
					Thread atlasThread = new AtlasThread(atlasNameTextField.getText(), tileSource,
							getMapSelectionCoordinates(), sZL, atlasOutputFormat,
							customTileParameters);
					atlasThread.start();
				} catch (Exception exception) {
					log.error("", exception);
				}
			}
			System.gc();
		}
	}

	private void updateZoomLevelCheckBoxes() {
		TileSource tileSource = previewMap.getTileSource();
		int zoomLevels = tileSource.getMaxZoom() - tileSource.getMinZoom() + 1;
		JCheckBox oldZoomLevelCheckBoxes[] = cbZoom;
		cbZoom = new JCheckBox[zoomLevels];
		zoomLevelPanel.removeAll();

		boolean fullScreenEnabled = Settings.getInstance().getFullScreenEnabled();
		if (fullScreenEnabled) {
			zoomLevelPanel.setLayout(new GridLayout(0, 2, 5, 3));
		} else {
			zoomLevelPanel.setLayout(new GridLayout(0, 10, 1, 2));
		}
		ZoomLevelCheckBoxListener cbl = new ZoomLevelCheckBoxListener();

		for (int i = cbZoom.length - 1; i >= 0; i--) {
			int cbz = i + tileSource.getMinZoom();
			JCheckBox cb = new JCheckBox();
			cb.setPreferredSize(new Dimension(22, 11));
			cb.setMinimumSize(cb.getPreferredSize());
			cb.setOpaque(false);
			cb.setFocusable(false);
			if (i < oldZoomLevelCheckBoxes.length)
				cb.setSelected(oldZoomLevelCheckBoxes[i].isSelected());
			cb.addActionListener(cbl);
			cb.setToolTipText("Select zoom level " + cbz + " for atlas");
			zoomLevelPanel.add(cb);
			cbZoom[i] = cb;

			JLabel l = new JLabel(Integer.toString(cbz));
			if (fullScreenEnabled) {
				l.setOpaque(true);
				l.setBackground(labelBackgroundColor);
				l.setForeground(labelForegroundColor);
			}
			zoomLevelPanel.add(l);
		}
		if (fullScreenEnabled) {
			amountOfTilesLabel.setOpaque(true);
			amountOfTilesLabel.setBackground(labelBackgroundColor);
			amountOfTilesLabel.setForeground(labelForegroundColor);
		} else {
			amountOfTilesLabel.setOpaque(false);
			amountOfTilesLabel.setForeground(Color.black);
		}

	}

	private class ZoomLevelCheckBoxListener implements ActionListener {
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

	public void zoomChanged(int zoomLevel) {
		zoomLevelText.setText(" " + zoomLevel + " ");
		zoomSlider.setValue(zoomLevel);
	}

	public void selectNextMapSource() {
		if (mapSourceCombo.getSelectedIndex() == mapSourceCombo.getItemCount() - 1) {
			Toolkit.getDefaultToolkit().beep();
		} else {
			mapSourceCombo.setSelectedIndex(mapSourceCombo.getSelectedIndex() + 1);
		}
	}

	public void selectPreviousMapSource() {
		if (mapSourceCombo.getSelectedIndex() == 0) {
			Toolkit.getDefaultToolkit().beep();
		} else {
			mapSourceCombo.setSelectedIndex(mapSourceCombo.getSelectedIndex() - 1);
		}
	}

	private void addSelectedAutoCutMultiMapLayers() {
		Atlas atlas = atlasTree.getAtlas();
		String atlasNameFmt = atlasNameTextField.getText() + "-%02d";
		TileSource tileSource = (TileSource) mapSourceCombo.getSelectedItem();
		SelectedZoomLevels sZL = new SelectedZoomLevels(previewMap.getTileSource().getMinZoom(),
				cbZoom);
		MapSelection ms = getMapSelectionCoordinates();
		Settings settings = Settings.getInstance();
		Dimension tileSize = new Dimension(tileSizeWidth.getValue(), tileSizeHeight.getValue());
		int[] zoomLevels = sZL.getZoomLevels();
		for (int zoom : zoomLevels) {
			String name = String.format(atlasNameFmt, new Object[] { zoom });
			Point tl = ms.getTopLeftTileCoordinate(zoom);
			Point br = ms.getBottomRightTileCoordinate(zoom);
			log.debug(tl + " " + br);
			new AutoCutMultiMapLayer(atlas, name, tileSource, tl, br, zoom, tileSize, settings
					.getMaxMapSize());
		}
		atlasTree.getTreeModel().notifyStructureChanged();
	}

	private void updateCustomTileProcessingControlsState() {
		boolean b = enableCustomTileProcessingCheckButton.isSelected();
		tileSizeWidthLabel.setEnabled(b);
		tileSizeHeightLabel.setEnabled(b);
		tileSizeHeight.setEnabled(b);
		tileSizeWidth.setEnabled(b);
		if (Utilities.testJaiColorQuantizerAvailable()) {
			tileColorDepth.setEnabled(b);
			tileColorDepthLabel.setEnabled(b);
		}
	}

	private void previewSelection() {
		MapSelection ms = getMapSelectionCoordinates();
		if (ms.coordinatesAreValid()) {
			latMaxTextField.setCoordinate(ms.getLat_max());
			latMinTextField.setCoordinate(ms.getLat_min());
			lonMaxTextField.setCoordinate(ms.getLon_max());
			lonMinTextField.setCoordinate(ms.getLon_min());
			previewMap.setSelection(ms, false);
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	private MapSelection getMapSelectionCoordinates() {
		double lat_max, lat_min, lon_max, lon_min;
		lat_max = latMaxTextField.getCoordinateOrNaN();
		lat_min = latMinTextField.getCoordinateOrNaN();
		lon_max = lonMaxTextField.getCoordinateOrNaN();
		lon_min = lonMinTextField.getCoordinateOrNaN();
		return new MapSelection(lat_max, lat_min, lon_max, lon_min);
	}

	private String validateInput(boolean checkCreateAtlas) {

		String errorText = "";

		if (!lonMinTextField.isInputValid())
			errorText += "Value of \"Longitude Min\" must be between -179 and 179. \n";

		if (!lonMaxTextField.isInputValid())
			errorText += "Value of \"Longitude Max\" must be between -179 and 179. \n";

		if (!latMaxTextField.isInputValid())
			errorText += "Value of \"Latitude Max\" must be between -85 and 85. \n";

		if (!latMinTextField.isInputValid())
			errorText += "Value of \"Latitude Min\" must be between -85 and 85. \n";

		if (!tileSizeHeight.isValueValid())
			errorText += "Value of \"Tile Size Height\" must be between " + JTileSizeCombo.MIN
					+ " and " + JTileSizeCombo.MAX + ". \n";

		if (!tileSizeWidth.isValueValid())
			errorText += "Value of \"Tile Size Width\" must be between " + JTileSizeCombo.MIN
					+ " and " + JTileSizeCombo.MAX + ". \n";

		if (checkCreateAtlas) {
			if (atlasNameTextField.getText().length() < 1) {
				errorText += "Please specify an \"Atlas name\". \n";
			}
		}

		if (checkCreateAtlas) {

			boolean zoomLevelChosen = false;

			for (int i = 0; i < cbZoom.length; i++) {
				if (cbZoom[i].isSelected()) {
					zoomLevelChosen = true;
					break;
				}
			}

			if (zoomLevelChosen == false) {
				errorText += "Please select at least one zoom level. \n";
			}

			MapSelection ms = getMapSelectionCoordinates();
			if (ms.getLat_max() == ms.getLat_min() || ms.getLon_max() == ms.getLon_min()) {
				errorText += "Please select a map area for download. \n";
			}

		}
		return errorText;
	}

	private boolean validateLatLongMinMax() {

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
			log.error("Error retrieving coordinates:", e);
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

	private void calculateNrOfTilesToDownload() {
		MapSelection ms = getMapSelectionCoordinates();
		String baseText;
		if (settings.getFullScreenEnabled())
			baseText = " %s tiles ";
		else
			baseText = " Amount of tiles in atlas: %s";
		if (ms.getLat_max() == ms.getLat_min() || ms.getLon_max() == ms.getLon_min()) {
			amountOfTilesLabel.setText(String.format(baseText, new Object[] { "0" }));
			amountOfTilesLabel.setToolTipText("");
		} else {
			try {
				SelectedZoomLevels sZL = new SelectedZoomLevels(previewMap.getTileSource()
						.getMinZoom(), cbZoom);

				int[] zoomLevels = sZL.getZoomLevels();

				long totalNrOfTiles = 0;

				String hint = "Total amount of tiles to download:";
				for (int i = 0; i < zoomLevels.length; i++) {
					int zoom = zoomLevels[i];
					long[] info = ms.calculateNrOfTilesEx(zoom);
					totalNrOfTiles += info[0];
					hint += "<br>Level " + zoomLevels[i] + ": " + info[0] + " (" + info[1] + "*"
							+ info[2] + ")";
				}
				hint = "<html>" + hint + "</html>";
				amountOfTilesLabel.setText(String.format(baseText, new Object[] { Long
						.toString(totalNrOfTiles) }));
				amountOfTilesLabel.setToolTipText(hint);
			} catch (Exception e) {
				amountOfTilesLabel.setText(String.format(baseText, new Object[] { "?" }));
				log.error("", e);
			}
		}
	}

	private class WindowDestroyer extends WindowAdapter {
		public void windowClosing(WindowEvent event) {
			saveSettings();
		}
	}

	/**
	 * Saves the window position and size when window is moved or resized. This
	 * is necessary because of the maximized state. If a window is maximized it
	 * is impossible to retrieve the window size & position of the non-maximized
	 * window - therefore we have to collect the information every time they
	 * change.
	 */
	private class MainWindowListener extends ComponentAdapter {
		public void componentResized(ComponentEvent event) {
			// log.debug(event.paramString());
			updateValues();
		}

		public void componentMoved(ComponentEvent event) {
			// log.debug(event.paramString());
			updateValues();
		}

		private void updateValues() {
			// only update old values while window is in NORMAL state
			// Note(Java bug): Sometimes getExtendedState() says the window is
			// not maximized but maximizing is already in progress and therefore
			// the window bounds are already changed.
			if ((getExtendedState() & MAXIMIZED_BOTH) != 0)
				return;
			Settings s = Settings.getInstance();
			s.setWindowDimension(getSize());
			s.setWindowLocation(getLocation());
		}
	}

	private class FilledLayeredPane extends JLayeredPane {

		private static final long serialVersionUID = -756648362160847296L;

		/**
		 * Layout each of the components in this JLayeredPane so that they all
		 * fill the entire extents of the layered pane -- from (0,0) to
		 * (getWidth(), getHeight())
		 */
		@Override
		public void doLayout() {
			// Synchronizing on getTreeLock, because I see other layouts doing
			// that.
			// see BorderLayout::layoutContainer(Container)
			synchronized (getTreeLock()) {
				int w = getWidth();
				int h = getHeight();
				for (Component c : getComponents()) {
					c.setBounds(0, 0, w, h);
				}
			}
		}
	}
}