package tac.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.MapSourcesUpdateException;
import tac.gui.components.JMapSizeCombo;
import tac.gui.components.JObjectCheckBox;
import tac.mapsources.MapSourcesManager;
import tac.program.model.ProxyType;
import tac.program.model.Settings;
import tac.program.model.UnitSystem;
import tac.tilestore.TileStore;
import tac.tilestore.TileStoreInfo;
import tac.utilities.GBC;
import tac.utilities.TACExceptionHandler;
import tac.utilities.Utilities;

public class SettingsGUI extends JDialog {
	private static final long serialVersionUID = -5227934684609357198L;

	private static Logger log = Logger.getLogger(SettingsGUI.class);

	private static final Integer[] THREADCOUNT_LIST = { 1, 2, 4, 6, 8, 10, 15 };

	private JComboBox unitSystem;

	private JButton mapSourcesOnlineUpdate;
	private JComboBox googleLang;

	private JPanel tileStoreInfoPanel;

	private JCheckBox tileStoreEnabled;
	private JLabel totalTileCountLabel;
	private JLabel totalTileSizeLabel;

	private JMapSizeCombo mapSize;

	private JComboBox threadCount;

	private JComboBox proxyType;
	private JTextField proxyHost;
	private JTextField proxyPort;

	private JButton okButton;
	private JButton cancelButton;

	private JTabbedPane tabbedPane;

	private Thread tileStoreAsyncThread = null;

	private List<TileSourceInfoComponents> tileStoreInfoList = new LinkedList<TileSourceInfoComponents>();
	private Vector<JMapSourceCB> mapSourceCbList = new Vector<JMapSourceCB>();

	static void showSettingsDialog(final JFrame owner) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new SettingsGUI(owner);
			}
		});
	}

	private SettingsGUI(JFrame owner) {
		super(owner);
		TACExceptionHandler.registerForCurrentThread();
		setModal(true);
		setMinimumSize(new Dimension(300, 300));
		createJFrame();
		createTabbedPane();
		createJButtons();
		loadSettings();
		addListeners();
		pack();
		// don't allow shrinking, but allow enlarging
		setMinimumSize(getSize());
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dScreen.width - getWidth()) / 2, (dScreen.height - getHeight()) / 2);
		setVisible(true);
	}

	private void createJFrame() {
		setLayout(new BorderLayout());
		setTitle("Settings");
	}

	// Create tabbed pane
	public void createTabbedPane() {
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 492, 275);
		addDisplaySettingsPanel();
		addMapSourceSettingsPanel();
		addTileStorePanel();
		addMapSizePanel();
		addNetworkPanel();

		add(tabbedPane, BorderLayout.CENTER);
	}

	private JPanel createNewTab(String tabTitle) {
		JPanel tabPanel = new JPanel();
		tabPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.add(tabPanel, tabTitle);
		return tabPanel;
	}

	private void addDisplaySettingsPanel() {
		JPanel tab = createNewTab("Display");
		tab.setLayout(new GridBagLayout());

		JPanel unitSystemPanel = new JPanel(new GridBagLayout());
		unitSystemPanel.setBorder(BorderFactory.createTitledBorder("Unit System"));

		UnitSystem[] us = UnitSystem.values();
		unitSystem = new JComboBox(us);
		unitSystemPanel.add(new JLabel("Unit system for map scale bar: "), GBC.std());
		unitSystemPanel.add(unitSystem, GBC.std());
		unitSystemPanel.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(unitSystemPanel, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));
	}

	private void addMapSourceSettingsPanel() {

		JPanel tab = createNewTab("Map sources");
		tab.setLayout(new GridBagLayout());

		JPanel updatePanel = new JPanel(new GridBagLayout());
		updatePanel.setBorder(BorderFactory.createTitledBorder("Map sources online update"));

		mapSourcesOnlineUpdate = new JButton("Perform online update");
		mapSourcesOnlineUpdate.addActionListener(new MapSourcesOnlineUpdateAction());
		updatePanel.add(mapSourcesOnlineUpdate, GBC.std());

		JPanel googlePanel = new JPanel(new GridBagLayout());
		googlePanel.setBorder(BorderFactory.createTitledBorder("Google Maps"));

		String[] languages = new String[] { "en", "de", "zh-CN" };
		googleLang = new JComboBox(languages);
		googleLang.setEditable(true);

		googlePanel.add(new JLabel("Language (hl parameter): "), GBC.std());
		googlePanel.add(googleLang, GBC.eol());

		JPanel mapSourcesInnerPanel = new JPanel();

		Color c = UIManager.getColor("List.background");
		mapSourcesInnerPanel.setBackground(c);

		TreeSet<String> disabledMapSources = new TreeSet<String>(Settings.getInstance()
				.getDisabledMapSources());

		mapSourceCbList.clear();
		for (MapSource ms : MapSourcesManager.getAllMapSources()) {
			JMapSourceCB checkBox = new JMapSourceCB(ms.toString());
			checkBox.setObject(ms);
			checkBox.setSelected(!disabledMapSources.contains(ms.getName()));
			checkBox.setBackground(c);
			mapSourcesInnerPanel.add(checkBox);
			mapSourceCbList.add(checkBox);
		}
		mapSourcesInnerPanel.setLayout(new BoxLayout(mapSourcesInnerPanel, BoxLayout.Y_AXIS));
		JScrollPane mapSourcesScrollPane = new JScrollPane(mapSourcesInnerPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mapSourcesScrollPane.setMinimumSize(new Dimension(300, 200));
		JPanel mapSourcesOuterPanel = new JPanel(new BorderLayout());
		mapSourcesOuterPanel.add(mapSourcesScrollPane, BorderLayout.CENTER);
		mapSourcesOuterPanel.setBorder(BorderFactory.createTitledBorder("Enabled Map Sources"));
		mapSourcesOuterPanel.setPreferredSize(new Dimension(200, 200));

		tab.add(updatePanel, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(googlePanel, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(mapSourcesOuterPanel, GBC.eol().fill());
	}

	private void addTileStorePanel() {

		JPanel backGround = createNewTab("Tile store");
		backGround.setLayout(new BorderLayout());

		tileStoreEnabled = new JCheckBox("Enable tile store for atlas download");

		JPanel tileStorePanel = new JPanel(new BorderLayout());
		tileStorePanel.setBorder(BorderFactory.createTitledBorder("Tile store settings"));
		tileStorePanel.add(tileStoreEnabled, BorderLayout.NORTH);

		tileStoreInfoPanel = new JPanel(new GridBagLayout());
		tileStoreInfoPanel.setBorder(BorderFactory.createTitledBorder("Information"));

		prepareTileStoreInfoPanel();

		backGround.add(tileStorePanel, BorderLayout.NORTH);
		backGround.add(tileStoreInfoPanel, BorderLayout.CENTER);
	}

	private synchronized void updateTileStoreInfoPanelAsync() {
		if (tileStoreAsyncThread != null)
			return; // An update is currently running
		tileStoreAsyncThread = new Thread("TileStoreInfoRetriever") {

			@Override
			public void run() {
				log.debug("Updating tilestore information in background");
				updateTileStoreInfoPanel();
				log.debug("Updating tilestore information finished");
				tileStoreAsyncThread = null;
			}
		};
		tileStoreAsyncThread.start();
	}

	private void prepareTileStoreInfoPanel() {
		final GridBagConstraints gbc_mapSource = new GridBagConstraints();
		gbc_mapSource.insets = new Insets(5, 10, 5, 10);
		gbc_mapSource.anchor = GridBagConstraints.WEST;
		final GridBagConstraints gbc_mapTiles = new GridBagConstraints();
		gbc_mapTiles.insets = gbc_mapSource.insets;
		gbc_mapTiles.anchor = GridBagConstraints.EAST;
		final GridBagConstraints gbc_eol = new GridBagConstraints();
		gbc_eol.gridwidth = GridBagConstraints.REMAINDER;

		TileStore tileStore = TileStore.getInstance();

		tileStoreInfoPanel.add(new JLabel("<html><b>Map source</b></html>"), gbc_mapSource);
		tileStoreInfoPanel.add(new JLabel("<html><b>Tiles</b></html>"), gbc_mapTiles);
		tileStoreInfoPanel.add(new JLabel("<html><b>Size</b></html>"), gbc_eol);

		ImageIcon trash = Utilities.loadResourceImageIcon("trash.png");

		for (MapSource ts : MapSourcesManager.getAllMapSources()) {
			if (!tileStore.storeExists(ts))
				continue;
			String mapTileCountText = "?";
			String mapTileSizeText = "?";
			final JLabel mapSourceNameLabel = new JLabel(ts.toString());
			final JLabel mapTileCountLabel = new JLabel(mapTileCountText);
			final JLabel mapTileSizeLabel = new JLabel(mapTileSizeText);
			final JButton deleteButton = new JButton(trash);
			TileSourceInfoComponents info = new TileSourceInfoComponents();
			info.tileSource = ts;
			info.countLabel = mapTileCountLabel;
			info.sizeLabel = mapTileSizeLabel;
			tileStoreInfoList.add(info);
			deleteButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			deleteButton.setToolTipText("Delete all stored " + ts.getName() + " tiles.");
			deleteButton.addActionListener(new ClearTileCacheAction(ts));

			tileStoreInfoPanel.add(mapSourceNameLabel, gbc_mapSource);
			tileStoreInfoPanel.add(mapTileCountLabel, gbc_mapTiles);
			tileStoreInfoPanel.add(mapTileSizeLabel, gbc_mapTiles);
			tileStoreInfoPanel.add(deleteButton, gbc_eol);
		}
		JSeparator hr = new JSeparator(JSeparator.HORIZONTAL);
		hr.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		tileStoreInfoPanel.add(hr, gbc);

		JLabel totalMapLabel = new JLabel("<html><b>Total</b></html>");
		totalTileCountLabel = new JLabel("<html><b>??</b></html>");
		totalTileSizeLabel = new JLabel("<html><b>??</b></html>");
		tileStoreInfoPanel.add(totalMapLabel, gbc_mapSource);
		tileStoreInfoPanel.add(totalTileCountLabel, gbc_mapTiles);
		tileStoreInfoPanel.add(totalTileSizeLabel, gbc_mapTiles);
	}

	private void updateTileStoreInfoPanel() {
		try {
			TileStore tileStore = TileStore.getInstance();

			long totalTileCount = 0;
			long totalTileSize = 0;
			for (final TileSourceInfoComponents info : tileStoreInfoList) {
				MapSource ts = info.tileSource;
				Utilities.checkForInterruption();
				TileStoreInfo tsi = tileStore.getStoreInfo(ts);
				int count = tsi.getTileCount();
				long size = tsi.getStoreSize();
				totalTileCount += count;
				totalTileSize += size;
				final String mapTileCountText = Integer.toString(count);
				final String mapTileSizeText = Utilities.formatBytes(size);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						info.countLabel.setText("<html><b>" + mapTileCountText + "</b></html>");
						info.sizeLabel.setText("<html><b>" + mapTileSizeText + "</b></html>");
					}
				});
			}
			final String totalTileCountText = "<html><b>" + Long.toString(totalTileCount)
					+ "</b></html>";
			final String totalTileSizeText = "<html><b>" + Utilities.formatBytes(totalTileSize)
					+ "</b></html>";
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					totalTileCountLabel.setText(totalTileCountText);
					totalTileSizeLabel.setText(totalTileSizeText);
				}
			});
		} catch (InterruptedException e) {
			log.debug("Tile store information retrieval was canceled");
		}

	}

	private void addMapSizePanel() {
		JPanel backGround = createNewTab("Map size");
		backGround.setLayout(new GridBagLayout());
		mapSize = new JMapSizeCombo();

		JLabel mapSizeLabel = new JLabel("Maximum size (width & height) of each map: ");
		JLabel mapSizeText = new JLabel("<html>If the image of the selected region to download "
				+ "is larger in height or width than <br>the mapsize it will be splitted into "
				+ "several maps that are no larger than the "
				+ "<br>specified maximum map size.<br><br>"
				+ "<b>Warning</b><br>TrekBuddy versions before v0.9.88 "
				+ "do not support map sizes larger than 32767.<br>"
				+ "Newer versions can handle maps up to a size of 1048575.</html>");

		JPanel leftPanel = new JPanel(new GridBagLayout());
		leftPanel.setBorder(BorderFactory.createTitledBorder("Map size settings"));

		GBC gbc = GBC.eol().insets(0, 5, 0, 5);
		leftPanel.add(mapSizeLabel, GBC.std());
		leftPanel.add(mapSize, GBC.eol());
		leftPanel.add(mapSizeText, gbc.fill(GBC.HORIZONTAL));
		leftPanel.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));

		backGround.add(leftPanel, GBC.std().fill(GBC.HORIZONTAL).anchor(GBC.NORTHEAST));
		backGround.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));
	}

	private void addNetworkPanel() {
		JPanel backGround = createNewTab("Network");
		backGround.setLayout(new GridBagLayout());
		GBC gbc_eolh = GBC.eol().fill(GBC.HORIZONTAL);
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Network connections"));
		threadCount = new JComboBox(THREADCOUNT_LIST);
		threadCount.setMaximumRowCount(THREADCOUNT_LIST.length);
		panel.add(threadCount, GBC.std().insets(5, 5, 5, 5));
		panel.add(new JLabel("Number of parallel network connections for tile downloading"), GBC
				.std().fill(GBC.HORIZONTAL));

		backGround.add(panel, gbc_eolh);

		// panel = new JPanel(new GridBagLayout());
		// panel.setBorder(BorderFactory.createTitledBorder("HTTP User-Agent"));
		// backGround.add(panel, gbc_eolh);

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("HTTP Proxy"));
		final JLabel proxyTypeLabel = new JLabel("Proxy settings: ");
		proxyType = new JComboBox(ProxyType.values());
		proxyType.setSelectedItem(Settings.getInstance().getProxyType());
		final JLabel proxyHostLabel = new JLabel("Proxy host name: ");
		proxyHost = new JTextField(Settings.getInstance().getCustomProxyHost());
		final JLabel proxyPortLabel = new JLabel("Proxy port: ");
		proxyPort = new JTextField(Settings.getInstance().getCustomProxyPort());
		ActionListener al = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				boolean b = ProxyType.CUSTOM.equals(proxyType.getSelectedItem());
				proxyHost.setEnabled(b);
				proxyPort.setEnabled(b);
				proxyHostLabel.setEnabled(b);
				proxyPortLabel.setEnabled(b);
			}
		};
		al.actionPerformed(null);
		proxyType.addActionListener(al);

		panel.add(proxyTypeLabel, GBC.std());
		panel.add(proxyType, gbc_eolh.insets(5, 2, 5, 2));
		panel.add(proxyHostLabel, GBC.std());
		panel.add(proxyHost, gbc_eolh);
		panel.add(proxyPortLabel, GBC.std());
		panel.add(proxyPort, gbc_eolh);
		backGround.add(panel, gbc_eolh);

		backGround.add(panel, gbc_eolh);

		backGround.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
	}

	public void createJButtons() {
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		okButton = new JButton("Ok");
		cancelButton = new JButton("Cancel");

		GBC gbc = GBC.std().insets(5, 5, 5, 5);
		buttonPanel.add(okButton, gbc);
		buttonPanel.add(cancelButton, gbc);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void loadSettings() {
		Settings s = Settings.getInstance();

		unitSystem.setSelectedItem(s.getUnitSystem());
		tileStoreEnabled.setSelected(s.tileStoreEnabled);

		mapSize.setValue(s.maxMapSize);

		int index = Arrays.binarySearch(THREADCOUNT_LIST, s.downloadThreadCount);
		if (index < 0)
			index = 0;
		threadCount.setSelectedIndex(index);

		String lang = s.getGoogleLanguage();
		googleLang.setSelectedItem(lang);

	}

	/**
	 * Reads the user defined settings from the gui and updates the
	 * {@link Settings} values according to the read gui settings.
	 */
	private void applySettings() {
		Settings s = Settings.getInstance();

		s.setUnitSystem((UnitSystem) unitSystem.getSelectedItem());
		s.tileStoreEnabled = tileStoreEnabled.isSelected();

		s.maxMapSize = mapSize.getValue();

		int threads = ((Integer) threadCount.getSelectedItem()).intValue();
		s.downloadThreadCount = threads;

		s.setProxyType((ProxyType) proxyType.getSelectedItem());
		s.setCustomProxyHost(proxyHost.getText());
		s.setCustomProxyPort(proxyPort.getText());
		s.applyProxySettings();

		Vector<String> disabledMaps = new Vector<String>();
		for (JMapSourceCB cb : mapSourceCbList) {
			if (!cb.isSelected())
				disabledMaps.add(cb.getObject().getName());
		}
		s.setDisabledMapSources(disabledMaps);

		MainGUI.getMainGUI().updateMapSourcesList();

		if (googleLang.getSelectedIndex() < 0) {
			s.setGoogleLanguage(googleLang.getEditor().getItem().toString());
		} else {
			s.setGoogleLanguage(googleLang.getSelectedItem().toString());
		}
		try {
			MainGUI.getMainGUI().checkAndSaveSettings();
		} catch (Exception e) {
			log.error("Error saving settings to file", e);
			JOptionPane.showMessageDialog(null, "Error saving settings to file:\n" + e.toString()
					+ " (" + e.getClass().getSimpleName() + ")", "Error saving settings to file",
					JOptionPane.ERROR_MESSAGE);
		}

		MainGUI.getMainGUI().previewMap.repaint();
		// Close the dialog window
		SettingsGUI.this.dispose();
	}

	private void addListeners() {

		addComponentListener(new WindowShowListener());
		addWindowListener(new WindowCloseListener());

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applySettings();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SettingsGUI.this.dispose();
			}
		});
	}

	private class WindowShowListener extends ComponentAdapter {

		private boolean firstShown = true;

		@Override
		public void componentShown(ComponentEvent event) {
			// After showing the settings dialog we start the tile store
			// information retrieval thread. This thread can take a long time to
			// finish depending on the number of tiles in the tile cache and the
			// hard disk performance
			synchronized (this) {
				if (firstShown)
					SettingsGUI.this.updateTileStoreInfoPanelAsync();
				firstShown = false;
			}
		}

	}

	private class WindowCloseListener extends WindowAdapter {

		@Override
		public void windowClosed(WindowEvent event) {
			// On close we check if the tile store information retrieval thread
			// is still running and if yes we interrupt it
			Thread t = tileStoreAsyncThread;
			if (t != null)
				t.interrupt();
		}

	}

	private class MapSourcesOnlineUpdateAction implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			try {
				boolean result = MapSourcesManager.mapsourcesOnlineUpdate();
				String msg = (result) ? "Online update successfull" : "No new update avilable";
				DateFormat df = DateFormat.getDateTimeInstance();
				Date date = MapSourcesManager.getMapSourcesDate(System.getProperties());
				msg += "\nCurrent map source date: " + df.format(date);
				JOptionPane.showMessageDialog(SettingsGUI.this, msg);
				if (result)
					MainGUI.getMainGUI().refreshPreviewMap();
			} catch (MapSourcesUpdateException e) {
				JOptionPane.showMessageDialog(SettingsGUI.this, e.getMessage(),
						"Mapsources online update failed", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private class ClearTileCacheAction implements ActionListener {

		MapSource source;

		public ClearTileCacheAction(MapSource source) {
			this.source = source;
		}

		public void actionPerformed(ActionEvent e) {
			final JButton b = (JButton) e.getSource();
			b.setEnabled(false);
			b.setToolTipText("Deleting in progress - please wait");
			Thread t = new Thread("TileStore_" + source.getName() + "_DeleteThread") {

				@Override
				public void run() {
					try {
						TileStore ts = TileStore.getInstance();
						ts.clearStore(source);
						SettingsGUI.this.updateTileStoreInfoPanelAsync();
						SettingsGUI.this.repaint();
					} catch (Exception e) {
						log.error("An error occured while cleaning tile cache: ", e);
					}
				}
			};
			t.start();
		}
	}

	private static class JMapSourceCB extends JObjectCheckBox<MapSource> {

		private static final long serialVersionUID = 1L;

		public JMapSourceCB(String text) {
			super(text);
		}
	}

	private static class TileSourceInfoComponents {
		JLabel sizeLabel;
		JLabel countLabel;
		MapSource tileSource;
	}
}
