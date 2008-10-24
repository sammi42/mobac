package tac.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.StartTAC;
import tac.gui.preview.MapSources;
import tac.program.Settings;
import tac.program.TileStore;
import tac.utilities.GBC;
import tac.utilities.Utilities;

public class SettingsGUI extends JDialog {
	private static final long serialVersionUID = -5227934684609357198L;

	private JPanel tileStoreInfoPanel;

	private JCheckBox tileStoreEnabled;

	private JComboBox mapSize;

	private JTextField proxyHost;
	private JTextField proxyPort;

	private JButton okButton;
	private JButton cancelButton;

	private JTabbedPane tabbedPane;

	private Vector<Integer> mapSizes;

	public SettingsGUI(JFrame owner) {
		super(owner);
		setModal(true);
		this.createJFrame();
		this.createTabbedPane();
		this.createJButtons();
		this.loadSettings();
		this.addListeners();
		this.pack();
	}

	private void createJFrame() {
		setLayout(new BorderLayout());
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dContent = new Dimension(500, 342);

		this.setLocation((dScreen.width - dContent.width) / 2,
				(dScreen.height - dContent.height) / 2);
		this.setSize(dContent);
		this.setTitle("Settings");
	}

	// Create tabbed pane
	public void createTabbedPane() {
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 492, 275);
		addTileStorePanel();
		addMapSizePanel();
		addNetworkPanel();

		add(tabbedPane, BorderLayout.CENTER);
	}

	private JPanel createNewTab(String tabTitle) {
		JPanel tabPanel = new JPanel(new BorderLayout());
		tabPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.add(tabPanel, tabTitle);
		return tabPanel;
	}

	private void addTileStorePanel() {

		JPanel backGround = createNewTab("Tile store");

		tileStoreEnabled = new JCheckBox("Enable tile store");

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBorder(BorderFactory.createTitledBorder("Tile store settings"));
		leftPanel.add(tileStoreEnabled, BorderLayout.NORTH);

		tileStoreInfoPanel = new JPanel(new GridBagLayout());
		tileStoreInfoPanel.setBorder(BorderFactory.createTitledBorder("Information"));

		updateTileStoreInfoPanel();

		backGround.add(leftPanel, BorderLayout.CENTER);
		backGround.add(tileStoreInfoPanel, BorderLayout.EAST);
	}

	private void updateTileStoreInfoPanel() {
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

		InputStream imageStream = StartTAC.class.getResourceAsStream("images/trash.png");
		ImageIcon trash = new ImageIcon();
		try {
			trash.setImage(ImageIO.read(imageStream));
		} catch (IOException e) {
		} finally {
			Utilities.closeStream(imageStream);
		}

		long totalTileCount = 0;
		long totalTileSize = 0;
		for (TileSource ts : MapSources.getMapSources()) {
			int count = tileStore.getNrOfTiles(ts);
			long size = tileStore.getStoreSize(ts);
			totalTileCount += count;
			totalTileSize += size;
			tileStoreInfoPanel.add(new JLabel(ts.getName()), gbc_mapSource);
			tileStoreInfoPanel.add(new JLabel(Integer.toString(count)), gbc_mapTiles);
			tileStoreInfoPanel.add(new JLabel(Utilities.formatBytes(size)), gbc_mapTiles);
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

		// Sizes from 1024 to 32768
		mapSizes = new Vector<Integer>(10);
		int size = 32768;
		do {
			mapSizes.addElement(new Integer(size));
			size -= 1024;
		} while (size >= 1024);

		mapSize = new JComboBox(mapSizes);

		JLabel mapSizeLabel = new JLabel("<html>If the image of the selected region to download "
				+ "is larger in height or width than the mapsize it will be splitted into "
				+ "several maps that are no larger than the selected mapsize.</html>");
		mapSizeLabel.setPreferredSize(new Dimension(250, 100));

		JPanel leftPanel = new JPanel(new GridBagLayout());
		leftPanel.setBorder(BorderFactory.createTitledBorder("Map size settings"));

		GBC gbc = GBC.std().insets(10, 5, 5, 5);
		leftPanel.add(mapSize, gbc);
		leftPanel.add(mapSizeLabel, gbc);
		leftPanel.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));

		backGround.add(leftPanel);
	}

	private void addNetworkPanel() {
		JPanel backGround = createNewTab("Network");
		JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
		panel.setBorder(BorderFactory.createTitledBorder("HTTP Proxy"));
		JLabel proxyHostLabel = new JLabel("Proxy host name: ");
		proxyHost = new JTextField(System.getProperty("http.proxyHost"));
		JLabel proxyPortLabel = new JLabel("Proxy port: ");
		proxyPort = new JTextField(System.getProperty("http.proxyPort"));
		panel.add(proxyHostLabel);
		panel.add(proxyHost);
		panel.add(proxyPortLabel);
		panel.add(proxyPort);
		backGround.add(panel, BorderLayout.NORTH);
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
	}

	private void applySettings() {
		Settings s = Settings.getInstance();

		s.setTileStoreEnabled(tileStoreEnabled.isSelected());

		int size = ((Integer) mapSize.getSelectedItem()).intValue();
		s.setMaxMapSize(size);

		System.setProperty("http.proxyHost", proxyHost.getText());
		System.setProperty("http.proxyPort", proxyPort.getText());

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
					TileStore ts = TileStore.getInstance();
					ts.clearStore(source);
					SettingsGUI.this.updateTileStoreInfoPanel();
					SettingsGUI.this.repaint();
				}
			};
			t.start();
		}
	}
}
