package tac.program.model;

import java.awt.Toolkit;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import tac.exceptions.InvalidNameException;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.CapabilityRenameable;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;

public class AtlasTreeModel implements TreeModel {

	private static Logger log = Logger.getLogger(AtlasTreeModel.class);

	protected AtlasInterface atlasInterface;

	protected Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();

	public AtlasTreeModel() {
		super();
		atlasInterface = new Atlas();
		// Sample data:
		// try {
		// new AutoCutMultiMapLayer(atlas, "Test 1", new MapSources.Mapnik(),
		// new Point(1000, 1000), new Point(2000, 2000), 3, null, 32000);
		// new AutoCutMultiMapLayer(atlas, "Test 2", new
		// MapSources.Cykloatlas(), new Point(1000,
		// 1000), new Point(2000, 2000), 4, null, 32000);
		// new AutoCutMultiMapLayer(atlas, "Test 3", new
		// MapSources.OpenArialMap(), new Point(
		// 1000, 1000), new Point(2000, 2000), 5, null, 32000);
		// new AutoCutMultiMapLayer(atlas, "Test 4", new MapSources.Mapnik(),
		// new Point(1000, 1000), new Point(200000, 200000), 16, null, 32000);
		// } catch (InvalidNameException e) {
		// }
	}

	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	public void notifyStructureChanged() {
		notifyStructureChanged(new TreeModelEvent(this, new Object[] { atlasInterface }));
	}

	/**
	 * IMPORTANT: This method have to be called BEFORE deleting the element in
	 * the data model!!! Otherwise the child index can not be retrieved anymore
	 * which is important.
	 * 
	 * @param node
	 */
	public void notifyNodeDelete(TreeNode node) {
		Object[] children = new Object[] { node };
		int[] childrenIdx = new int[] { node.getParent().getIndex(node) };
		if (childrenIdx[0] == -1) {
			// A problem detected - use fall back solution
			notifyStructureChanged();
			return;
		}

		TreeNode n = node;
		LinkedList<TreeNode> path = new LinkedList<TreeNode>();
		n = n.getParent();
		while (n != null) {
			path.addFirst(n);
			n = n.getParent();
		}
		TreeModelEvent event = new TreeModelEvent(this, path.toArray(), childrenIdx, children);
		for (TreeModelListener l : listeners)
			l.treeNodesRemoved(event);
	}

	protected void notifyStructureChanged(TreeModelEvent event) {
		for (TreeModelListener l : listeners)
			l.treeStructureChanged(event);
	}

	public Object getChild(Object parent, int index) {
		if (parent instanceof AtlasInterface)
			return ((AtlasInterface) parent).getLayer(index);
		if (parent instanceof LayerInterface)
			return ((LayerInterface) parent).getMap(index);
		return null;
	}

	public int getChildCount(Object parent) {
		if (parent instanceof AtlasInterface)
			return ((AtlasInterface) parent).getLayerCount();
		if (parent instanceof LayerInterface)
			return ((LayerInterface) parent).getMapCount();
		return 0;
	}

	public int getIndexOfChild(Object parent, Object child) {
		return 0;
	}

	public Object getRoot() {
		return atlasInterface;
	}

	public boolean isLeaf(Object node) {
		if (node instanceof MapInterface)
			return true;
		return false;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		Object o = path.getLastPathComponent();
		boolean success = false;
		try {
			CapabilityRenameable sel = (CapabilityRenameable) o;
			String newName = (String) newValue;
			if (newName.length() == 0)
				return;
			sel.setName(newName);
			success = true;
		} catch (ClassCastException e) {
			log.error("", e);
		} catch (InvalidNameException e) {
			log.error(e.getLocalizedMessage());
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Renaming failed",
					JOptionPane.ERROR_MESSAGE);
		} finally {
			if (!success) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}

	public AtlasInterface getAtlas() {
		return atlasInterface;
	}

	public void setAtlas(Atlas atlas) {
		this.atlasInterface = atlas;
		notifyStructureChanged();
	}

	public void save(Profile profile) throws Exception {
		profile.save(atlasInterface);
	}

	public void load(Profile profile) throws Exception {
		atlasInterface = profile.load();
		notifyStructureChanged();
	}
}
