package tac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import tac.data.gpx.GPXUtils;
import tac.data.gpx.gpx11.Gpx;
import tac.gui.MainGUI;
import tac.gui.panels.JGpxPanel;
import tac.gui.panels.JGpxPanel.ListModelEntry;
import tac.program.model.Settings;
import tac.utilities.file.GpxFileFilter;

public class GpxSave implements ActionListener {

	private JGpxPanel panel;
	private boolean saveAs;

	public GpxSave(JGpxPanel panel) {
		this(panel, false);
	}

	/**
	 * 
	 * @param panel
	 * @param saveAs
	 *            if true a file chooser dialog is displayed where the user can
	 *            change the filename
	 */
	public GpxSave(JGpxPanel panel, boolean saveAs) {
		super();
		this.panel = panel;
		this.saveAs = saveAs;
	}

	public void actionPerformed(ActionEvent event) {

		ListModelEntry entry = panel.getSelectedEntry();
		if (entry == null) {
			JOptionPane.showMessageDialog(null, "No Gpx file selected", "Error saving Gpx file",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Gpx gpx = entry.getLayer().getGpx();

		try {
			File f = entry.getGpxFile();
			if (saveAs || f == null)
				f = selectFile(f);
			if (f == null)
				return;
			entry.setFile(f);
			GPXUtils.saveGpxFile(gpx, f);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		MainGUI.getMainGUI().previewMap.repaint();
	}

	private File selectFile(File f) {
		JFileChooser fc = new JFileChooser();
		try {
			File dir = new File(Settings.getInstance().gpxFileChooserDir);
			if (f == null)
				fc.setCurrentDirectory(dir); // restore the saved directory
			else
				fc.setSelectedFile(f);
		} catch (Exception e) {
		}
		fc.addChoosableFileFilter(new GpxFileFilter(true));
		int returnVal = fc.showOpenDialog(MainGUI.getMainGUI());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;
		Settings.getInstance().gpxFileChooserDir = fc.getCurrentDirectory().getAbsolutePath();
		return fc.getSelectedFile();
	}
}
