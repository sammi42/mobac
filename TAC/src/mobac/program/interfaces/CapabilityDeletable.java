package mobac.program.interfaces;

import mobac.gui.atlastree.JAtlasTree;
import mobac.program.model.AtlasTreeModel;

/**
 * Identifies nodes in {@link JAtlasTree} / {@link AtlasTreeModel} that can be
 * deleted (including sub-nodes). Nodes implementing this interface will show a
 * "delete" entry in it's context menu.
 */
public interface CapabilityDeletable {

	public void delete();
}
