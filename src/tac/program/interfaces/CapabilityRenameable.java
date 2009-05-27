package tac.program.interfaces;

import tac.gui.components.AtlasTree;
import tac.program.model.AtlasTreeModel;

/**
 * Identifies nodes in {@link AtlasTree} / {@link AtlasTreeModel} that can be
 * renamed. Additionally nodes implementing this interface will show a "rename"
 * entry in it's context menu.
 */
public interface CapabilityRenameable {

	public void setName(String newName);
}
