package tac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import tac.data.gpx.gpx11.Gpx;
import tac.gui.MainGUI;
import tac.gui.mapview.GpxLayer;
import tac.gui.panels.JGpxPanel;
import tac.gui.panels.JGpxPanel.ListModelEntry;

public class GpxNew implements ActionListener {

	JGpxPanel panel;

	public GpxNew(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent event) {
		newGpx();
		MainGUI.getMainGUI().previewMap.repaint();
	}

	public ListModelEntry newGpx() {
		Gpx gpx = new Gpx();
		GpxLayer gpxLayer = new GpxLayer(gpx);
		return panel.addGpxLayer(null, gpxLayer);
	}
}
