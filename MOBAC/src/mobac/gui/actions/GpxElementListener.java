package mobac.gui.actions;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import mobac.data.gpx.gpx11.WptType;
import mobac.gui.MainGUI;
import mobac.gui.components.GpxEntry;
import mobac.gui.components.GpxRootEntry;
import mobac.gui.components.RteEntry;
import mobac.gui.components.TrkEntry;
import mobac.gui.components.TrksegEntry;
import mobac.gui.components.WptEntry;
import mobac.gui.mapview.GpxMapController;
import mobac.gui.mapview.PreviewMap;

/**
 * Listener for the gpx editor tree elements.
 * 
 * @author lhoeppner
 * 
 */
public class GpxElementListener implements MouseListener {
	private JMenuItem item;
	private GpxEntry gpxEntry;
	private GpxMapController mapController = null;
	private GpxEditor editor = GpxEditor.getInstance();

	public GpxElementListener(GpxEntry gpxEntry) {
		this.gpxEntry = gpxEntry;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		handleClick(e);
	}

	public void mouseReleased(MouseEvent e) {
		handleClick(e);
	}

	private void handleClick(MouseEvent e) {
		item = (JMenuItem) e.getSource();
		if (item.getName().equals("rename")) {
			renameEntry();
		} else if (item.getName().equals("delete")) {
			removeEntry();
		}
	}

	/**
	 * Removes an entry (wpt, trk, trkseg, rte) from a gpx file (and the
	 * displayed layer) Currently only works for waypoints.
	 * 
	 */
	private void removeEntry() {
		int answer = JOptionPane.showConfirmDialog(null, "Do you really want to delete this node?",
				"Delete node", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (answer == JOptionPane.YES_OPTION) {
			PreviewMap map = MainGUI.getMainGUI().previewMap;
			map.getMapSelectionController().disable();
			if (mapController == null)
				mapController = new GpxMapController(map, gpxEntry.getLayer().getPanel(), false);
			mapController.enable();

			if (gpxEntry.getClass().equals(RteEntry.class)) {
				// RteEntry rte = (RteEntry) gpxEntry;

			} else if (gpxEntry.getClass().equals(TrkEntry.class)) {
				// TrkEntry trk = (TrkEntry) gpxEntry;

			} else if (gpxEntry.getClass().equals(WptEntry.class)) {
				WptEntry wptEntry = (WptEntry) gpxEntry;
				WptType wpt = wptEntry.getWpt();
				editor.findWptAndDelete(wpt, gpxEntry);
				wptEntry.getLayer().getPanel().removeWpt(wptEntry);
				mapController.repaint();
			} else if (gpxEntry.getClass().equals(GpxRootEntry.class)) {
				// GpxRootEntry root = (GpxRootEntry) gpxEntry;

			}
		} else {
			return;
		}
	}

	/**
	 * Renames (if possible) the entry according to user input.
	 * 
	 */
	private void renameEntry() {
		if (gpxEntry.getClass().equals(TrksegEntry.class)) {
			JOptionPane.showMessageDialog(null, "Track segments cannot be named.", "Error",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		} else {
			if (gpxEntry.getClass().equals(RteEntry.class)) {
				RteEntry rte = (RteEntry) gpxEntry;
				String name = JOptionPane.showInputDialog(null, "Please input the name:", rte
						.getRte().getName());
				if (name == null) {
					return;
				}
				rte.getRte().setName(name);
			} else if (gpxEntry.getClass().equals(TrkEntry.class)) {
				TrkEntry trk = (TrkEntry) gpxEntry;
				String name = JOptionPane.showInputDialog(null, "Please input the name:", trk
						.getTrk().getName());
				if (name == null) {
					return;
				}
				trk.getTrk().setName(name);
			} else if (gpxEntry.getClass().equals(WptEntry.class)) {
				WptEntry wpt = (WptEntry) gpxEntry;
				String name = JOptionPane.showInputDialog(null, "Please input the name:", wpt
						.getWpt().getName());
				if (name == null) {
					return;
				}
				wpt.getWpt().setName(name);
			} else if (gpxEntry.getClass().equals(GpxRootEntry.class)) {
				GpxRootEntry root = (GpxRootEntry) gpxEntry;
				String name = JOptionPane.showInputDialog(null, "Please input the name:", root
						.getLayer().getGpx().getMetadata().getName());
				if (name == null) {
					return;
				}
				root.getLayer().getGpx().getMetadata().setName(name);
			}
		}
	}
}
