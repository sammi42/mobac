package mobac.gui.actions;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import mobac.gui.components.GpxEntry;
import mobac.gui.components.GpxRootEntry;
import mobac.gui.components.RteEntry;
import mobac.gui.components.TrkEntry;
import mobac.gui.components.TrksegEntry;
import mobac.gui.components.WptEntry;

/**
 * Listener for the gpx editor tree elements.
 * 
 * @author lhoeppner
 * 
 */
public class GpxElementListener implements MouseListener {
	private JMenuItem item;
	private GpxEntry gpxEntry;

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

	private void removeEntry() {
		// TODO show warning and delete
	}

	// TODO move edit to GpxEditor
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
