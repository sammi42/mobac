package tac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;

import tac.data.gpx.GPXUtils;
import tac.data.gpx.gpx11.Gpx;
import tac.gui.MainGUI;
import tac.gui.mapview.GpxLayer;
import tac.gui.panels.JGpxPanel;
import tac.program.model.Settings;

public class GpxLoad implements ActionListener {

	JGpxPanel panel;

	public GpxLoad(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent event) {
		MainGUI.getMainGUI().previewMap.setMapMarkerVisible(true);

		JFileChooser fc = new JFileChooser();
		try {
			File dir = new File(Settings.getInstance().gpxFileChooserDir);
			fc.setCurrentDirectory(dir); // restore the saved directory
		} catch (Exception e) {
		}
		fc.addChoosableFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().endsWith(".gpx");
			}

			@Override
			public String getDescription() {
				return "GPX 1.1 files (*.gpx)";
			}
		});
		int returnVal = fc.showOpenDialog(MainGUI.getMainGUI());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;
		Settings.getInstance().gpxFileChooserDir = fc.getCurrentDirectory().getAbsolutePath();

		File f = fc.getSelectedFile();
		try {
			Gpx gpx = GPXUtils.loadGpxFile(f);
			GpxLayer gpxLayer = new GpxLayer(gpx);
			panel.addListEntry(f, gpxLayer);
			MainGUI.getMainGUI().previewMap.mapLayers.add(gpxLayer);
		} catch (JAXBException e) {
			JOptionPane.showMessageDialog(MainGUI.getMainGUI(),
					"<html>Unable to load the GPX file <br><i>" + f.getAbsolutePath()
							+ "</i><br><br><b>Please make sure the file is a valid GPX v1.1 file.</b><br>"
							+ "<br>Internal error message:<br>" + e.getMessage() + "</html>",
					"GPX loading failed", JOptionPane.ERROR_MESSAGE);

		}
		MainGUI.getMainGUI().previewMap.repaint();
	}
}
