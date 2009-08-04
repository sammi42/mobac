package tac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;

import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import tac.data.gpx.GPXTest;
import tac.data.gpx.interfaces.Gpx;
import tac.data.gpx.interfaces.Wpt;
import tac.gui.MainGUI;

public class LoadGpx implements ActionListener {

	public void actionPerformed(ActionEvent event) {
		MainGUI.getMainGUI().previewMap.setMapMarkerVisible(true);
		List<MapMarker> mapMarkers = MainGUI.getMainGUI().previewMap.getMapMarkerList();
		mapMarkers.clear();
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().endsWith(".gpx");
			}

			@Override
			public String getDescription() {
				return "GPX 1.0/1.1 files (*.gpx)";
			}
		});
		int returnVal = fc.showOpenDialog(MainGUI.getMainGUI());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		try {
			Gpx gpx = GPXTest.loadGpxFile(fc.getSelectedFile());
			for (Wpt wpt : gpx.getWpt()) {
				mapMarkers.add(new MapMarkerDot(wpt.getLat().doubleValue(), wpt.getLon()
						.doubleValue()));
			}
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		MainGUI.getMainGUI().previewMap.repaint();
	}

}
