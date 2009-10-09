package tac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import tac.gui.MainGUI;
import tac.gui.mapview.GpxMapController;
import tac.gui.mapview.PreviewMap;
import tac.gui.panels.JGpxPanel;
import tac.gui.panels.JGpxPanel.ListModelEntry;

public class GpxAddPoint implements ActionListener {

	JGpxPanel panel;

	public GpxAddPoint(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent event) {
		ListModelEntry entry = panel.getSelectedEntry();
		if (entry == null) {
			int answer = JOptionPane.showConfirmDialog(null, "No GPX file selected.\n"
					+ "Do you want to create a new GPX file?", "Add point to new GPX file?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (answer!= JOptionPane.YES_OPTION)
				return;
			entry = new GpxNew(panel).newGpx();
		}
		PreviewMap map = MainGUI.getMainGUI().previewMap;
		map.getMapSelectionController().disable();
		new GpxMapController(map, entry, true);
	}
}
