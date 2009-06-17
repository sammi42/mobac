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
import java.io.IOException;

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
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.Main;
import tac.exceptions.InvalidNameException;
import tac.gui.components.JAtlasNameField;
import tac.gui.components.JAtlasTree;
import tac.gui.mapview.GridZoom;
import tac.gui.mapview.MapEventListener;
import tac.gui.mapview.PreviewMap;
import tac.gui.panels.JCoordinatesPanel;
import tac.gui.panels.JProfilesPanel;
import tac.gui.panels.JTileImageParametersPanel;
import tac.mapsources.MapSources;
import tac.program.AtlasThread;
import tac.program.MapSelection;
import tac.program.SelectedZoomLevels;
import tac.program.TACInfo;
import tac.program.interfaces.AtlasInterface;
import tac.program.model.AtlasOutputFormat;
import tac.program.model.EastNorthCoordinate;
import tac.program.model.Layer;
import tac.program.model.Profile;
import tac.program.model.Settings;
import tac.program.model.TileImageParameters;
import tac.utilities.GBC;
import tac.utilities.TACExceptionHandler;

public class MainGUI extends JFrame implements MapEventListener {

	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(MainGUI.class);

	private static Color labelBackgroundColor = new Color(0, 0, 0, 127);
	private static Color labelForegroundColor = Color.white;

	private static MainGUI mainGUI = null;

	private JAtlasTree jAtlasTree;
	private PreviewMap previewMap;

	private JLabel zoomLevelText;
	private JComboBox gridZoomCombo;
	private JSlider zoomSlider;
	private JComboBox mapSourceCombo;
	private JButton helpButton;
	private JButton fullScreenButton;
	private JButton settingsButton;
	private JAtlasNameField atlasNameTextField;
	private JComboBox atlasOutputFormatCombo;
	private JButton createAtlasButton;
	private JPanel zoomLevelPanel;
	private JCheckBox[] cbZoom = new JCheckBox[0];
	private JLabel amountOfTilesLabel;

	private JCoordinatesPanel coordinatesPanel;
	private JProfilesPanel profilesPanel;
	private JTileImageParametersPanel tileImageParametersPanel;

	private JPanel mapControlPanel = new JPanel(new BorderLayout());
	private JPanel leftPanel = new JPanel(new GridBagLayout());

	public static void createMainGui() {
		if (mainGUI != null)
			return;
		mainGUI = new MainGUI();
		mainGUI.setVisible(true);
	}

	public static MainGUI getMainGUI() {
		return mainGUI;
	}

	private MainGUI() {
		super();
		TACExceptionHandler.registerForCurrentThread();
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
		previewMap.addMapEventListener(this);

		createControls();
		calculateNrOfTilesToDownload();
		setLayout(new BorderLayout());
		add(leftPanel, BorderLayout.WEST);
		JLayeredPane layeredPane = new FilledLayeredPane();
		layeredPane.add(previewMap, new Integer(0));
		layeredPane.add(mapControlPanel, new Integer(1));
		add(layeredPane, BorderLayout.CENTER);

		loadSettings();
		updatePanels();
		profilesPanel.initialize();
		updateZoomLevelCheckBoxes();
		updateGridSizeCombo();
		tileImageParametersPanel.updateControlsState();
		zoomChanged(previewMap.getZoom());
	}

	private void createControls() {

		// zoom slider
		zoomSlider = new JSlider(JMapViewer.MIN_ZOOM, previewMap.getMapSource().getMaxZoom());
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
		fullScreenButton.setEnabled(false); // TODO: reenable

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

		jAtlasTree = new JAtlasTree(previewMap);

		coordinatesPanel = new JCoordinatesPanel();
		tileImageParametersPanel = new JTileImageParametersPanel();
		profilesPanel = new JProfilesPanel(jAtlasTree);
		profilesPanel.getProfilesCombo().addActionListener(new ProfilesComboListener());
	}

	private void updateLeftPanel() {
		leftPanel.removeAll();

		coordinatesPanel.addButtonActionListener(new DisplaySelectionButtonListener());

		JPanel mapSourcePanel = new JPanel(new GridBagLayout());
		mapSourcePanel.setBorder(BorderFactory.createTitledBorder("Map source"));
		mapSourcePanel.add(mapSourceCombo, GBC.std().insets(2, 2, 2, 2).fill());

		JPanel zoomLevelsPanel = new JPanel(new GridBagLayout());
		zoomLevelsPanel.setBorder(BorderFactory.createTitledBorder("Zoom Levels"));
		zoomLevelsPanel.add(zoomLevelPanel, GBC.eol());
		zoomLevelsPanel.add(amountOfTilesLabel, GBC.std().anchor(GBC.WEST).insets(0, 5, 0, 0));

		GBC gbc_std = GBC.std().insets(5, 2, 5, 3);
		GBC gbc_eol = GBC.eol().insets(5, 2, 5, 3);

		JPanel atlasContentPanel = new JPanel(new GridBagLayout());
		atlasContentPanel.setBorder(BorderFactory.createTitledBorder("Atlas Content"));
		JScrollPane treeScrollPane = new JScrollPane(jAtlasTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jAtlasTree.getTreeModel().addTreeModelListener(new AtlasListener());

		treeScrollPane.setPreferredSize(new Dimension(100, 100));
		atlasContentPanel.add(treeScrollPane, GBC.eol().fill());
		JButton clearAtlas = new JButton("Clear");
		atlasContentPanel.add(clearAtlas, GBC.std());
		clearAtlas.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				jAtlasTree.clearAtlas();
			}
		});
		JButton addLayers = new JButton("Add selection");
		atlasContentPanel.add(addLayers, GBC.eol());
		addLayers.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				addSelectedAutoCutMultiMapLayers();
			}
		});
		atlasContentPanel.add(new JLabel("Name: "), gbc_std);
		atlasContentPanel.add(atlasNameTextField, gbc_eol.fill());

		JPanel atlasNamePanel = new JPanel(new GridBagLayout());
		atlasNamePanel.setBorder(BorderFactory.createTitledBorder("Atlas settings"));
		atlasNamePanel.add(new JLabel("Format: "), gbc_std);
		atlasNamePanel.add(atlasOutputFormatCombo, gbc_eol);
		atlasNamePanel.add(createAtlasButton, gbc_eol.fill());

		gbc_eol = GBC.eol().insets(5, 2, 5, 2).fill(GBC.HORIZONTAL);

		JPanel leftPanelContent = new JPanel(new GridBagLayout());
		leftPanelContent.add(coordinatesPanel, gbc_eol);
		leftPanelContent.add(mapSourcePanel, gbc_eol);
		leftPanelContent.add(zoomLevelsPanel, gbc_eol);
		leftPanelContent.add(tileImageParametersPanel, gbc_eol);
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

			// bottomControls.add(profilesCombo, GBC.std().insets(5, 0, 0, 5));
			// bottomControls.add(deleteProfileButton, GBC.std().insets(10, 0,
			// 0, 5));
			// bottomControls.add(saveAsProfileButton, GBC.std().insets(10, 0,
			// 0, 5));
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
		boolean fullScreenEnabled = false;

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
		atlasNameTextField.setText(settings.getElemntName());
		atlasOutputFormatCombo.setSelectedItem(settings.getAtlasOutputFormat());
		previewMap.settingsLoadPosition();
		coordinatesPanel.setMaxCoordinate(settings.getSelectionMax());
		coordinatesPanel.setMinCoordinate(settings.getSelectionMin());

		tileImageParametersPanel.loadSettings();
		mapSourceCombo.setSelectedItem(MapSources.getSourceByName(settings.getMapviewMapSource()
				.getName()));

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
			s.setMapviewMapSource(previewMap.getMapSource());
			s.setElementName(atlasNameTextField.getText());
			s.setAtlasOutputFormat((AtlasOutputFormat) atlasOutputFormatCombo.getSelectedItem());
			s.setSelectionMax(coordinatesPanel.getMaxCoordinate());
			s.setSelectionMin(coordinatesPanel.getMinCoordinate());

			tileImageParametersPanel.saveSettings();
			boolean maximized = (getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
			s.setWindowMaximized(maximized);
			if (!maximized) {
				s.setWindowDimension(getSize());
				s.setWindowLocation(getLocation());
			}
			Settings.save();
		} catch (Exception e) {
			TACExceptionHandler.showExceptionDialog(e);
			JOptionPane.showMessageDialog(null,
					"Error on writing program settings to \"settings.xml\"", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public String getUserText() {
		return atlasNameTextField.getText();
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
		int maxZoom = previewMap.getMapSource().getMaxZoom();
		int minZoom = previewMap.getMapSource().getMinZoom();
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

	private class MapSourceComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			previewMap.setMapSource((MapSource) mapSourceCombo.getSelectedItem());
			zoomSlider.setMinimum(previewMap.getMapSource().getMinZoom());
			zoomSlider.setMaximum(previewMap.getMapSource().getMaxZoom());
			updateGridSizeCombo();
			updateZoomLevelCheckBoxes();
		}
	}

	private class HelpButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			DataInputStream in = new DataInputStream(Main.class
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
			// Settings settings = Settings.getInstance();
			// settings.setFullScreenEnabled(!settings.getFullScreenEnabled());
			updatePanels();
		}
	}

	private class ProfilesComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Profile profile = profilesPanel.getSelectedProfile();
			profilesPanel.getDeleteButton().setEnabled(profile != null);
			if (profile == null)
				return;
			jAtlasTree.load(profile);
		}
	}

	private class SettingsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			SettingsGUI.showSettingsDialog(MainGUI.this);
		}
	}

	private class CreateAtlasButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (!jAtlasTree.testAtlasContentValid())
				return;
			System.gc();
			try {
				AtlasOutputFormat atlasOutputFormat = (AtlasOutputFormat) atlasOutputFormatCombo
						.getSelectedItem();
				AtlasInterface atlasInterface = jAtlasTree.getAtlas();
				atlasInterface.setOutputFormat(atlasOutputFormat);
				Thread atlasThread = new AtlasThread(atlasInterface);
				atlasThread.start();
			} catch (Exception exception) {
				log.error("", exception);
			}
		}
	}

	private void updateZoomLevelCheckBoxes() {
		MapSource tileSource = previewMap.getMapSource();
		int zoomLevels = tileSource.getMaxZoom() - tileSource.getMinZoom() + 1;
		JCheckBox oldZoomLevelCheckBoxes[] = cbZoom;
		cbZoom = new JCheckBox[zoomLevels];
		zoomLevelPanel.removeAll();

		boolean fullScreenEnabled = false; // Settings.getInstance().
		// getFullScreenEnabled();
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
		coordinatesPanel.setMaxCoordinate(max);
		coordinatesPanel.setMinCoordinate(min);
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
		AtlasInterface atlasInterface = jAtlasTree.getAtlas();
		String atlasNameFmt = atlasNameTextField.getText() + "-%02d";
		MapSource tileSource = (MapSource) mapSourceCombo.getSelectedItem();
		SelectedZoomLevels sZL = new SelectedZoomLevels(previewMap.getMapSource().getMinZoom(),
				cbZoom);
		MapSelection ms = getMapSelectionCoordinates();
		Settings settings = Settings.getInstance();
		String errorText = validateInput();
		if (errorText.length() > 0) {
			JOptionPane.showMessageDialog(null, errorText, "Errors", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int[] zoomLevels = sZL.getZoomLevels();
		if (zoomLevels.length == 0) {
			JOptionPane.showMessageDialog(this, "Please select at least one zoom level");
			return;
		}

		for (int zoom : zoomLevels) {
			String name = String.format(atlasNameFmt, new Object[] { zoom });
			Point tl = ms.getTopLeftTileCoordinate(zoom);
			Point br = ms.getBottomRightTileCoordinate(zoom);
			TileImageParameters customTileParameters = getSelectedTileImageParameters();

			String layerName = name;
			boolean success = false;
			int c = 1;
			do {
				try {
					Layer layer = new Layer(atlasInterface, layerName, tileSource, tl, br, zoom,
							customTileParameters, settings.getMaxMapSize());
					atlasInterface.addLayer(layer);
					success = true;
				} catch (InvalidNameException e) {
					layerName = name + "_" + Integer.toString(c++);
				}
			} while (!success);
		}
		jAtlasTree.getTreeModel().notifyStructureChanged();
	}

	public void mapSourceChanged(MapSource newMapSource) {
		if (newMapSource.equals(mapSourceCombo.getSelectedItem()))
			return;
		mapSourceCombo.setSelectedItem(newMapSource);
	}

	private void previewSelection() {
		MapSelection ms = getMapSelectionCoordinates();
		if (ms.coordinatesAreValid()) {
			coordinatesPanel.setMaxCoordinate(ms.getMax());
			coordinatesPanel.setMinCoordinate(ms.getMin());
			previewMap.zoomToSelection(ms, false);
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	private MapSelection getMapSelectionCoordinates() {
		EastNorthCoordinate max = coordinatesPanel.getMaxCoordinate();
		EastNorthCoordinate min = coordinatesPanel.getMinCoordinate();
		return new MapSelection(max, min);
	}

	private String validateInput() {

		String errorText = "";
		errorText += coordinatesPanel.getValidationErrorMessages();
		errorText += tileImageParametersPanel.getValidationErrorMessages();

		return errorText;
	}

	public TileImageParameters getSelectedTileImageParameters() {
		return tileImageParametersPanel.getSelectedTileImageParameters();
	}

	private void calculateNrOfTilesToDownload() {
		MapSelection ms = getMapSelectionCoordinates();
		String baseText;
		baseText = " %s tiles ";
		if (ms.getLat_max() == ms.getLat_min() || ms.getLon_max() == ms.getLon_min()) {
			amountOfTilesLabel.setText(String.format(baseText, new Object[] { "0" }));
			amountOfTilesLabel.setToolTipText("");
		} else {
			try {
				SelectedZoomLevels sZL = new SelectedZoomLevels(previewMap.getMapSource()
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

	private class AtlasListener implements TreeModelListener {

		protected void changed() {
			profilesPanel.getSaveAsButton().setEnabled(jAtlasTree.getAtlas().getLayerCount() > 0);
		}

		public void treeNodesChanged(TreeModelEvent e) {
			changed();
		}

		public void treeNodesInserted(TreeModelEvent e) {
			changed();
		}

		public void treeNodesRemoved(TreeModelEvent e) {
			changed();
		}

		public void treeStructureChanged(TreeModelEvent e) {
			changed();
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