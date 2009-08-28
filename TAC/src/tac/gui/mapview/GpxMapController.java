package tac.gui.mapview;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.openstreetmap.gui.jmapviewer.JMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

public class GpxMapController extends JMapController implements MouseListener {

	public GpxMapController(JMapViewer map, boolean enabled) {
		super(map, enabled);
	}

	public void mouseClicked(MouseEvent e) {
		// Add new GPX point to currently selected GPX file 
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

}
