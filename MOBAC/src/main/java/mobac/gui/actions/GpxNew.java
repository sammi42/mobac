package mobac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mobac.data.gpx.GPXUtils;
import mobac.data.gpx.gpx11.Gpx;
import mobac.gui.MainGUI;
import mobac.gui.components.GpxRootEntry;
import mobac.gui.mapview.GpxLayer;
import mobac.gui.panels.JGpxPanel;
//import mobac.gui.panels.JGpxPanel.ListModelEntry;


public class GpxNew implements ActionListener {

	JGpxPanel panel;

	public GpxNew(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent event) {
		if (!GPXUtils.checkJAXBVersion())
			return;
		newGpx();
		MainGUI.getMainGUI().previewMap.repaint();
	}

	public GpxRootEntry newGpx() {
		Gpx gpx = Gpx.createGpx();
		GpxLayer gpxLayer = new GpxLayer(gpx);
		return panel.addGpxLayer(gpxLayer);
	}
}
