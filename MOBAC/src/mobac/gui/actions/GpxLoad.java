package mobac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import mobac.data.gpx.GPXUtils;
import mobac.data.gpx.gpx11.Gpx;
import mobac.gui.MainGUI;
import mobac.gui.mapview.GpxLayer;
import mobac.gui.panels.JGpxPanel;
import mobac.program.model.Settings;
import mobac.utilities.file.GpxFileFilter;


public class GpxLoad implements ActionListener {

	JGpxPanel panel;

	public GpxLoad(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	// TODO check if file already in tree
	public void actionPerformed(ActionEvent event) {
		if (!GPXUtils.checkJAXBVersion())
			return;
		JFileChooser fc = new JFileChooser();
		try {
			File dir = new File(Settings.getInstance().gpxFileChooserDir);
			fc.setCurrentDirectory(dir); // restore the saved directory
		} catch (Exception e) {
		}
		fc.addChoosableFileFilter(new GpxFileFilter(false));
		int returnVal = fc.showOpenDialog(MainGUI.getMainGUI());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;
		Settings.getInstance().gpxFileChooserDir = fc.getCurrentDirectory().getAbsolutePath();

		File f = fc.getSelectedFile();
		try {
			Gpx gpx = GPXUtils.loadGpxFile(f);
			GpxLayer gpxLayer = new GpxLayer(gpx);
			gpxLayer.setFile(f);
			panel.addGpxLayer(gpxLayer);	
		} catch (JAXBException e) {
			JOptionPane
					.showMessageDialog(
							MainGUI.getMainGUI(),
							"<html>Unable to load the GPX file <br><i>"
									+ f.getAbsolutePath()
									+ "</i><br><br><b>Please make sure the file is a valid GPX v1.1 file.</b><br>"
									+ "<br>Internal error message:<br>" + e.getMessage()
									+ "</html>", "GPX loading failed", JOptionPane.ERROR_MESSAGE);

		}
		MainGUI.getMainGUI().previewMap.repaint();
	}
}
