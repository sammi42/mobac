package tac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import tac.data.gpx.GPXUtils;
import tac.data.gpx.gpx11.Gpx;
import tac.gui.MainGUI;
import tac.gui.panels.JGpxPanel;
import tac.gui.panels.JGpxPanel.ListModelEntry;

public class GpxSave implements ActionListener {

	JGpxPanel panel;

	public GpxSave(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent event) {

		ListModelEntry entry = panel.getSelectedEntry();
		if (entry == null) {
			JOptionPane.showMessageDialog(null, "No Gpx file selected", "Error saving Gpx file",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Gpx gpx = entry.getLayer().getGpx();

		// JFileChooser fc = new JFileChooser();
		// try {
		// File dir = new File(Settings.getInstance().gpxFileChooserDir);
		// fc.setCurrentDirectory(dir); // restore the saved directory
		// } catch (Exception e) {
		// }
		// fc.addChoosableFileFilter(new FileFilter() {
		//
		// @Override
		// public boolean accept(File f) {
		// return f.isDirectory() || f.getName().endsWith(".gpx");
		// }
		//
		// @Override
		// public String getDescription() {
		// return "GPX 1.0/1.1 files (*.gpx)";
		// }
		// });
		// int returnVal = fc.showOpenDialog(MainGUI.getMainGUI());
		// if (returnVal != JFileChooser.APPROVE_OPTION)
		// return;
		// Settings.getInstance().gpxFileChooserDir =
		// fc.getCurrentDirectory().getAbsolutePath();

		try {
			File f = entry.getGpxFile();// fc.getSelectedFile();
			GPXUtils.saveGpxFile(gpx, f);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		MainGUI.getMainGUI().previewMap.repaint();
	}
}
