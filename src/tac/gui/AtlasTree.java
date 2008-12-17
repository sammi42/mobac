package tac.gui;

import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import tac.program.interfaces.ToolTipProvider;
import tac.program.model.AtlasTreeModel;

public class AtlasTree extends JTree {

	public AtlasTree() {
		super(new AtlasTreeModel());
		setRootVisible(false);
		setShowsRootHandles(true);
		setCellRenderer(new AtlasTreeCellRenderer());
		setToolTipText("");
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (getRowForLocation(event.getX(), event.getY()) == -1)
			return null;
		TreePath curPath = getPathForLocation(event.getX(), event.getY());
		Object o = curPath.getLastPathComponent();
		if (o == null || !(o instanceof ToolTipProvider))
			return null;
		return ((ToolTipProvider) o).getToolTip();

	}

	protected static class AtlasTreeCellRenderer extends DefaultTreeCellRenderer {

		public AtlasTreeCellRenderer() {
		}

	}
}
