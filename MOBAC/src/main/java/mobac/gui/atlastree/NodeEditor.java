package mobac.gui.atlastree;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import mobac.gui.components.JAtlasNameField;


public class NodeEditor extends DefaultTreeCellEditor {

	public NodeEditor(JAtlasTree atlasTree) {
		super(atlasTree, null);
		atlasTree.setEditable(true);
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
			boolean expanded, boolean leaf, int row) {
		// Each node type has it's own TreeCellRenderer implementation
		// this not covered by DefaultTreeCellEditor - therefore we have to
		// correct the renderer each time an editorComponent is requested
		TreeCellRenderer tcr = tree.getCellRenderer();
		renderer = (DefaultTreeCellRenderer) tcr.getTreeCellRendererComponent(tree, value,
				isSelected, expanded, leaf, row, true);
		return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
	}

	@Override
	protected TreeCellEditor createTreeCellEditor() {
		return new DefaultCellEditor(new JAtlasNameField());
	}

}
