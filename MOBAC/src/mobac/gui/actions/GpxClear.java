package mobac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import mobac.gui.MainGUI;
import mobac.gui.mapview.GpxLayer;
import mobac.gui.panels.JGpxPanel;

import org.openstreetmap.gui.jmapviewer.interfaces.MapLayer;


/**
 * Deletes all loaded {@link GpxLayer}s from the main map viewer.
 * 
 */
public class GpxClear implements ActionListener {
	
	JGpxPanel panel;

	public GpxClear(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent e) {
		Iterator<MapLayer> mapLayers = MainGUI.getMainGUI().previewMap.mapLayers.iterator();
		while (mapLayers.hasNext()) {
			if (mapLayers.next() instanceof GpxLayer)
				mapLayers.remove();
		}
		panel.getListModel().clear();
		MainGUI.getMainGUI().previewMap.repaint();
	}

}
