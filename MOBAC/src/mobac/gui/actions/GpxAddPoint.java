package mobac.gui.actions;

import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import mobac.gui.MainGUI;
import mobac.gui.components.GpxEntry;
import mobac.gui.mapview.GpxMapController;
import mobac.gui.mapview.PreviewMap;
import mobac.gui.panels.JGpxPanel;



public class GpxAddPoint implements ActionListener {

	JGpxPanel panel;
	
	static GpxMapController mapController = null;

	public GpxAddPoint(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public synchronized void actionPerformed(ActionEvent event) {
		GpxEntry entry = panel.getSelectedEntry();
		if (entry == null) {
			int answer = JOptionPane.showConfirmDialog(null, "No GPX file selected.\n"
					+ "Do you want to create a new GPX file?", "Add point to new GPX file?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (answer != JOptionPane.YES_OPTION)
				return;
			entry = new GpxNew(panel).newGpx();
		}
		PreviewMap map = MainGUI.getMainGUI().previewMap;
		map.getMapSelectionController().disable();
		if (mapController == null)
			mapController = new GpxMapController(map, entry, false);
		mapController.enable();
	}
}
