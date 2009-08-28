package tac.gui.panels;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;

import tac.gui.actions.GpxClear;
import tac.gui.actions.GpxLoad;
import tac.gui.actions.GpxSave;
import tac.gui.components.JCollapsiblePanel;
import tac.gui.mapview.GpxLayer;
import tac.utilities.GBC;

public class JGpxPanel extends JCollapsiblePanel {

	private DefaultListModel listModel;

	private JList list;

	public JGpxPanel() {
		super("Gpx", new GridBagLayout());
		JButton loadGpx = new JButton("Load Gpx");
		loadGpx.addActionListener(new GpxLoad(this));

		JButton saveGpx = new JButton("Save Gpx");
		saveGpx.addActionListener(new GpxSave(this));

		JButton clearGpx = new JButton("Clear");
		clearGpx.addActionListener(new GpxClear(this));

		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setPreferredSize(new Dimension(100, 100));

		addContent(list, GBC.eol().fill(GBC.HORIZONTAL));
		addContent(loadGpx, GBC.std());
		addContent(saveGpx, GBC.std());
		addContent(clearGpx, GBC.std());
	}

	public ListModelEntry getSelectedEntry() {
		return (ListModelEntry) list.getSelectedValue();
	}

	public DefaultListModel getListModel() {
		return listModel;
	}

	public static class ListModelEntry {
		File gpxFile;
		GpxLayer layer;
		String name;

		public ListModelEntry(File gpxFile, GpxLayer layer) {
			super();
			this.gpxFile = gpxFile;
			this.layer = layer;
			name = gpxFile.getName() + " [wpt]";
		}

		@Override
		public String toString() {
			return name;
		}

		public File getGpxFile() {
			return gpxFile;
		}

		public GpxLayer getLayer() {
			return layer;
		}
		
	}
}
