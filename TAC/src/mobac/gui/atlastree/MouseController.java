package mobac.gui.atlastree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.tree.TreePath;

public class MouseController extends MouseAdapter {

	JAtlasTree atlasTree;

	public MouseController(JAtlasTree atlasTree) {
		super();
		this.atlasTree = atlasTree;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2)
			return;
		TreePath selPath = atlasTree.getSelectionPath();
		if (selPath == null)
			return; // clicked on empty area
		atlasTree.selectElementOnMap(selPath.getLastPathComponent());
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			atlasTree.showNodePopupMenu(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			atlasTree.showNodePopupMenu(e);
		}
	}

}
