package tac.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.StartTAC;
import tac.gui.preview.MapSources;
import tac.program.Settings;
import tac.program.TileStore;
import tac.utilities.GBC;
import tac.utilities.Utilities;

public class SettingsGUI extends JDialog {
	private static final long serialVersionUID = -5227934684609357198L;

	private static Logger log = Logger.getLogger(SettingsGUI.class);

	private static Vector<Integer> mapSizes;
	private static Vector<Integer> threadNumbers;

	private JComboBox googleLang;

	private JPanel tileStoreInfoPanel;

	private JCheckBox tileStoreEnabled;

	private JComboBox mapSize;

	private JComboBox threadCount;

	private JTextField proxyHost;
	private JTextField proxyPort;

	private JButton okButton;
	private JButton cancelButton;

	private JTabbedPane tabbedPane;

	static {
		// Sizes from 1024 to 32768
		mapSizes = new Vector<Integer>(20);
		int size = 32768;
		do {
			mapSizes.addElement(new Integer(size));
			size -= 1024;
		} while (size >= 1024);
		threadNumbers = new Vector<Integer>(10);
		for (int i = 1; i <= 10; i++)
			threadNumbers.add(new Integer(i));
	}

	public SettingsGUI(JFrame owner) {
		super(owner);
		setModal(true);
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
		updateTileStoreInfoPanelAnsynchronously();
	}

	private void createJFrame() {
		setLayout(new BorderLayout());
		setTitle("Settings");
	}

	// Create tabbed pane
	public void createTabbedPane() {
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 492, 275);
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

	private void addMapSourceSettingsPanel() {

		JPanel tab = createNewTab("Map sources");
		tab.setLayout(new BorderLayout());

		JPanel googlePanel = new JPanel(new GridBagLayout());
		googlePanel.setBorder(BorderFactory.createTitledBorder("Google Maps"));

		String[] languages = new String[] { "en", "de" };
		googleLang = new JComboBox(languages);
		googleLang.setEditable(true);

		googlePanel.add(new JLabel("Language (hl parameter): "), GBC.std());
		googlePanel.add(googleLang, GBC.eol());

		// JPanel mapSourcesPanel = new JPanel(new GridBagLayout());
		// mapSourcesPanel.setBorder(BorderFactory.createTitledBorder(
		// "Enabled Map Sources"));
		//
		// for (TileSource ts : MapSources.getMapSources()) {
		// JCheckBox mapSource = new JCheckBox(ts.toString());
		// mapSourcesPanel.add(mapSource, GBC.eol());
		// }

		tab.add(googlePanel, BorderLayout.NORTH);
		// tab.add(mapSourcesPanel, BorderLayout.CENTER);
	}

	private void addTileStorePanel() {

		JPanel backGround = createNewTab("Tile store");
		backGround.setLayout(new BorderLayout());

		tileStoreEnabled = new JCheckBox("Enable tile store");

		JPanel tileStorePanel = new JPanel(new BorderLayout());
		tileStorePanel.setBorder(BorderFactory.createTitledBorder("Tile store settings"));
		tileStorePanel.add(tileStoreEnabled, BorderLayout.NORTH);

		tileStoreInfoPanel = new JPanel(new GridBagLayout());
		tileStoreInfoPanel.setBorder(BorderFactory.createTitledBorder("Information"));

		updateTileStoreInfoPanel(true);

		backGround.add(tileStorePanel, BorderLayout.NORTH);
		backGround.add(tileStoreInfoPanel, BorderLayout.CENTER);
	}

	private void updateTileStoreInfoPanelAnsynchronously() {
		new Thread() {

			@Override
			public void run() {
				updateTileStoreInfoPanel(false);
			}
		}.start();
	}

	private synchronized void updateTileStoreInfoPanel(boolean fakeContent) {
		tileStoreInfoPanel.removeAll();
		GridBagConstraints gbc_mapSource = new GridBagConstraints();
		gbc_mapSource.insets = new Insets(5, 10, 5, 10);
		gbc_mapSource.anchor = GridBagConstraints.WEST;
		GridBagConstraints gbc_mapTiles = new GridBagConstraints();
		// gbc_mapTiles.gridwidth = GridBagConstraints.REMAINDER;
		gbc_mapTiles.insets = gbc_mapSource.insets;
		gbc_mapTiles.anchor = GridBagConstraints.EAST;
		GridBagConstraints gbc_eol = new GridBagConstraints();
		gbc_eol.gridwidth = GridBagConstraints.REMAINDER;
		// gbc_eol.insets = gbc_mapSource.insets;

		TileStore tileStore = TileStore.getInstance();

		tileStoreInfoPanel.add(new JLabel("<html><b>Map source</b></html>"), gbc_mapSource);
		tileStoreInfoPanel.add(new JLabel("<html><b>Tiles</b></html>"), gbc_mapTiles);
		tileStoreInfoPanel.add(new JLabel("<html><b>Size</b></html>"), gbc_eol);

		InputStream imageStream = null;
		ImageIcon trash = new ImageIcon();
		try {
			imageStream = StartTAC.class.getResourceAsStream("images/trash.png");
			trash.setImage(ImageIO.read(imageStream));
		} catch (Exception e) {
			log.error("An error occured while loading trash icon:", e);
		} finally {
			Utilities.closeStream(imageStream);
		}

		long totalTileCount = 0;
		long totalTileSize = 0;
		for (TileSource ts : MapSources.getMapSources()) {
			String mapTileCountText = "?";
			String mapTileSizeText = "?";
			if (!fakeContent) {
				int count = tileStore.getNrOfTiles(ts);
				long size = tileStore.getStoreSize(ts);
				totalTileCount += count;
				totalTileSize += size;
				mapTileCountText = Integer.toString(count);
				mapTileSizeText = Utilities.formatBytes(size);
			}
			tileStoreInfoPanel.add(new JLabel(ts.toString()), gbc_mapSource);
			tileStoreInfoPanel.add(new JLabel(mapTileCountText), gbc_mapTiles);
			tileStoreInfoPanel.add(new JLabel(mapTileSizeText), gbc_mapTiles);
			JButton deleteButton = new JButton(trash);
			deleteButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			deleteButton.setToolTipText("Delete all stored " + ts.getName() + " tiles.");
			deleteButton.addActionListener(new ClearTileCacheAction(ts));
			tileStoreInfoPanel.add(deleteButton, gbc_eol);
		}
		JSeparator hr = new JSeparator(JSeparator.HORIZONTAL);
		hr.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		tileStoreInfoPanel.add(hr, gbc);

		tileStoreInfoPanel.add(new JLabel("<html><b>Total</b></html>"), gbc_mapSource);
		tileStoreInfoPanel.add(new JLabel("<html><b>" + Long.toString(totalTileCount)
				+ "</b></html>"), gbc_mapTiles);
		tileStoreInfoPanel.add(new JLabel("<html><b>" + Utilities.formatBytes(totalTileSize)
				+ "</b></html>"), gbc_mapTiles);
	}

	private void addMapSizePanel() {
		JPanel backGround = createNewTab("Map size");
		backGround.setLayout(new GridBagLayout());
		mapSize = new JComboBox(mapSizes);

		JLabel mapSizeLabel = new JLabel("<html>If the image of the selected region to download "
				+ "is larger in <br> height or width than the mapsize it will be splitted into "
				+ "several <br> maps that are no larger than the selected mapsize.</html>");
		mapSizeLabel.setVerticalAlignment(JLabel.TOP);
		// mapSizeLabel.setPreferredSize(new Dimension(300, 100));

		JPanel leftPanel = new JPanel(new GridBagLayout());
		leftPanel.setBorder(BorderFactory.createTitledBorder("Map size settings"));

		GBC gbc = GBC.std().insets(10, 5, 5, 5).anchor(GBC.EAST);
		leftPanel.add(mapSize, gbc);
		leftPanel.add(mapSizeLabel, gbc.toggleEol());
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
		threadCount = new JComboBox(threadNumbers);
		panel.add(threadCount, GBC.std().insets(5, 5, 5, 5));
		panel.add(new JLabel("Number of parallel network connections for tile downloading"), GBC
				.std().fill(GBC.HORIZONTAL));

		backGround.add(panel, gbc_eolh);

		// panel = new JPanel(new GridBagLayout());
		// panel.setBorder(BorderFactory.createTitledBorder("HTTP User-Agent"));
		// backGround.add(panel, gbc_eolh);

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("HTTP Proxy"));
		JLabel proxyHostLabel = new JLabel("Proxy host name: ");
		proxyHost = new JTextField(System.getProperty("http.proxyHost"));
		JLabel proxyPortLabel = new JLabel("Proxy port: ");
		proxyPort = new JTextField(System.getProperty("http.proxyPort"));
		panel.add(proxyHostLabel, GBC.std());
		panel.add(proxyHost, gbc_eolh);
		panel.add(proxyPortLabel, GBC.std());
		panel.add(proxyPort, gbc_eolh);
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

		tileStoreEnabled.setSelected(s.isTileStoreEnabled());

		int size = s.getMaxMapsSize();
		int index = mapSizes.indexOf(new Integer(size));
		if (index < 0)
			index = 0;
		mapSize.setSelectedIndex(index);
		index = threadNumbers.indexOf(s.getThreadCount());
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

		s.setTileStoreEnabled(tileStoreEnabled.isSelected());

		int size = ((Integer) mapSize.getSelectedItem()).intValue();
		s.setMaxMapSize(size);

		int threads = ((Integer) threadCount.getSelectedItem()).intValue();
		s.setThreadCount(threads);

		System.setProperty("http.proxyHost", proxyHost.getText());
		System.setProperty("http.proxyPort", proxyPort.getText());

		if (googleLang.getSelectedIndex() < 0) {
			s.setGoogleLanguage(googleLang.getEditor().getItem().toString());
		} else {
			s.setGoogleLanguage(googleLang.getSelectedItem().toString());
		}

		// Close the dialog window
		SettingsGUI.this.dispose();
	}

	private void addListeners() {

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

	private class ClearTileCacheAction implements ActionListener {

		TileSource source;

		public ClearTileCacheAction(TileSource source) {
			this.source = source;
		}

		public void actionPerformed(ActionEvent e) {
			final JButton b = (JButton) e.getSource();
			b.setEnabled(false);
			b.setToolTipText("Deleting in progress - please wait");
			Thread t = new Thread() {

				@Override
				public void run() {
					try {
						TileStore ts = TileStore.getInstance();
						ts.clearStore(source);
						SettingsGUI.this.updateTileStoreInfoPanel(false);
						SettingsGUI.this.repaint();
					} catch (Exception e) {
						log.error("An error occured while cleaning tile cache: ", e);
					}
				}
			};
			t.start();
		}
	}
}
