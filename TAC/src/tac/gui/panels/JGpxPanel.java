package tac.gui.panels;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;

import tac.gui.actions.GpxAddPoint;
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

		JButton clearGpx = new JButton("Clear List");
		clearGpx.addActionListener(new GpxClear(this));

		JButton addPointGpx = new JButton("Add wpt");
		addPointGpx.addActionListener(new GpxAddPoint(this));

		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setPreferredSize(new Dimension(100, 100));

		GBC eol = GBC.eol().fill(GBC.HORIZONTAL);
		GBC std = GBC.std().fill(GBC.HORIZONTAL);
		addContent(list, eol);
		addContent(clearGpx, std);
		addContent(addPointGpx, eol);
		addContent(loadGpx, std);
		addContent(saveGpx, eol);
	}

	public ListModelEntry getSelectedEntry() {
		return (ListModelEntry) list.getSelectedValue();
	}

	public DefaultListModel getListModel() {
		return listModel;
	}

	public void addListEntry(File gpxFile, GpxLayer layer) {
		ListModelEntry entry = new JGpxPanel.ListModelEntry(gpxFile, layer);
		listModel.addElement(entry);
		list.setSelectedValue(entry, true);
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
