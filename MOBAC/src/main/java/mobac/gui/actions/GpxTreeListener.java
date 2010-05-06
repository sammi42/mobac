package mobac.gui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import mobac.gui.components.GpxEntry;

/**
 * Listener for the gpx editor tree.
 * 
 * @author lhoeppner
 * 
 */
public class GpxTreeListener implements MouseListener {
	private JPopupMenu popup;

	public void actionPerformed(ActionEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopup(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopup(e);
		}
	}

	/**
	 * Popup for all elements in the gpx tree. TODO separate for waypoints,
	 * files, tracks and routes
	 * 
	 * @param e
	 */
	private void showPopup(MouseEvent e) {
		JTree tree = (JTree) e.getSource();
		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		tree.setSelectionPath(selPath);

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
		node.getUserObject();

		GpxEntry gpxEntry = null;
		try {
			gpxEntry = (GpxEntry) node.getUserObject();
			gpxEntry.setNode(node);
		} catch (ClassCastException exc) {
		}

		popup = new JPopupMenu();
		JMenuItem delete = new JMenuItem("delete element");
		delete.setName("delete");
		GpxElementListener listener = new GpxElementListener(gpxEntry);
		delete.addMouseListener(listener);
		popup.add(delete);
		JMenuItem rename = new JMenuItem("rename element");
		rename.setName("rename");
		rename.addMouseListener(listener);
		popup.add(rename);

		popup.show((Component) e.getSource(), e.getX(), e.getY());
	}
}
