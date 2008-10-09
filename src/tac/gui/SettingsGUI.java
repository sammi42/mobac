package tac.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import tac.program.Settings;

public class SettingsGUI extends JDialog {
	private static final long serialVersionUID = -5227934684609357198L;

	private JCheckBox tileStoreEnabled;

	private JComboBox mapSize;

	private JButton okButton;
	private JButton cancelButton;

	private JTabbedPane tabbedPane;
	private JDialog jdialogGUI;
	private WindowDestroyer windowListener;

	private Vector<MapSize> mapSizes;

	public SettingsGUI() {
		this.createJFrame();
		this.createTabbedPane();
		this.createJButtons();
		this.applySettings();
		this.addListeners();

		jdialogGUI = this;
	}

	private void createJFrame() {
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dContent = new Dimension(500, 342);

		this.setLocation((dScreen.width - dContent.width) / 2,
				(dScreen.height - dContent.height) / 2);
		this.setSize(dContent);
		this.setResizable(false);
		this.getContentPane().setLayout(null);
		this.setTitle("Settings");
	}

	// Create tabbed pane
	public void createTabbedPane() {
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 492, 275);
		tabbedPane.add(createGoogleSitePanel(), "Google site");
		tabbedPane.add(createTileStorePanel(), "Tile store");
		tabbedPane.add(createMapSizePanel(), "Map size");

		this.getContentPane().add(tabbedPane);
	}

	private JPanel createGoogleSitePanel() {
		JPanel leftPanel = new JPanel(null);
		leftPanel.setBounds(5, 5, 475, 240);
		leftPanel.setBorder(BorderFactory.createTitledBorder("Site to download tiles from"));

		JPanel thumbNailBackGround = new JPanel(null);
		thumbNailBackGround.add(leftPanel);
		return thumbNailBackGround;
	}

	private JPanel createTileStorePanel() {

		tileStoreEnabled = new JCheckBox("Enable tile store");
		tileStoreEnabled.setBounds(7, 40, 120, 15);

		JPanel leftPanel = new JPanel(null);
		leftPanel.setBounds(5, 5, 475, 240);
		leftPanel.setBorder(BorderFactory.createTitledBorder("Tile store settings"));

		leftPanel.add(tileStoreEnabled);

		JPanel thumbNailBackGround = new JPanel(null);
		thumbNailBackGround.add(leftPanel);

		return thumbNailBackGround;
	}

	private JPanel createMapSizePanel() {

		// Sizes from 512 to 4096
		mapSizes = new Vector<MapSize>(10);
		mapSizes.addElement(new MapSize(0));
		int valueToAdd = 512;
		for (int i = 0; i < 8; i++) {
			mapSizes.addElement(new MapSize(valueToAdd));
			valueToAdd *= 2;
		}

		mapSize = new JComboBox(mapSizes);
		mapSize.setBounds(7, 40, 120, 20);

		JPanel leftPanel = new JPanel(null);
		leftPanel.setBounds(5, 5, 475, 240);
		leftPanel.setBorder(BorderFactory.createTitledBorder("Map size settings"));

		leftPanel.add(mapSize);

		JPanel thumbNailBackGround = new JPanel(null);
		thumbNailBackGround.add(leftPanel);

		return thumbNailBackGround;
	}

	public void createJButtons() {
		okButton = new JButton("Ok");
		okButton.setBounds(364, 280, 50, 25);

		cancelButton = new JButton("Cancel");
		cancelButton.setBounds(418, 280, 74, 25);

		this.getContentPane().add(okButton);
		this.getContentPane().add(cancelButton);
	}

	private void applySettings() {
		Settings s = Settings.getInstance();

		tileStoreEnabled.setSelected(s.isTileStoreEnabled());

		int size = s.getMapSize();
		int index = mapSizes.indexOf(new MapSize(size));
		if (index < 0)
			index = 0;
		mapSize.setSelectedIndex(index);
	}

	private void addListeners() {

		windowListener = new WindowDestroyer();

		this.addWindowListener(windowListener);
		okButton.addActionListener(new JButtonListener());
		cancelButton.addActionListener(new JButtonListener());
	}

	public void removeWindowListener() {
		this.removeWindowListener(windowListener);
	}

	public void addWindowListener() {
		this.addWindowListener(windowListener);
	}

	// WindowDestroyer
	private class WindowDestroyer extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			((JDialog) e.getComponent()).dispose();
		}

		public void windowDeactivated(WindowEvent e) {
			((JDialog) e.getComponent()).requestFocus();
		}
	}

	private class JButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();

			if (actionCommand.equals("Ok")) {

				Settings s = Settings.getInstance();

				s.setTileStoreEnabled(tileStoreEnabled.isSelected());

				int size = ((MapSize) mapSize.getSelectedItem()).getMapSize();
				s.setMapSize(size);

				// Close the dialog window
				jdialogGUI.dispose();
			} else if (actionCommand.equals("Cancel"))
				jdialogGUI.dispose();
		}
	}

	private static class MapSize implements Comparable<MapSize> {

		private int mapSize;

		public MapSize(int mapSize) {
			super();
			this.mapSize = mapSize;
		}

		@Override
		public String toString() {
			if (mapSize == 0)
				return "Unlimited";
			return Integer.toString(mapSize);
		}

		public int getMapSize() {
			return mapSize;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MapSize)
				return ((MapSize) obj).mapSize == mapSize;
			return false;
		}

		public int compareTo(MapSize o) {
			return new Integer(mapSize).compareTo(o.mapSize);
		}

	}
}