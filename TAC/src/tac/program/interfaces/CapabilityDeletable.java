package tac.program.interfaces;

import tac.gui.atlastree.JAtlasTree;
import tac.program.model.AtlasTreeModel;

/**
 * Identifies nodes in {@link JAtlasTree} / {@link AtlasTreeModel} that can be
 * deleted (including sub-nodes). Nodes implementing this interface will show a
 * "delete" entry in it's context menu.
 */
public interface CapabilityDeletable {

	public void delete();
}
