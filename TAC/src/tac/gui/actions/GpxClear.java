package tac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import org.openstreetmap.gui.jmapviewer.interfaces.MapLayer;

import tac.gui.MainGUI;
import tac.gui.mapview.GpxLayer;

/**
 * Deletes all loaded {@link GpxLayer}s from the main map viewer.
 * 
 */
public class GpxClear implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		Iterator<MapLayer> mapLayers = MainGUI.getMainGUI().previewMap.mapLayers.iterator();
		while (mapLayers.hasNext()) {
			if (mapLayers.next() instanceof GpxLayer)
				mapLayers.remove();
		}
		MainGUI.getMainGUI().previewMap.repaint();
	}

}
