package mobac.gui.components;

import javax.swing.tree.DefaultMutableTreeNode;

import mobac.gui.mapview.GpxLayer;

/**
 * Generalized entry in the gpx tree. All actual entries derive from this class.
 * The class encapsulates everything gui-related as well as the actual gpx data
 * for the editor. Subclasses: GpxRootEntry, TrkEntry, RteEntry, WptEntry
 * 
 * @author lhoeppner
 * 
 */
public class GpxEntry {
	private DefaultMutableTreeNode node;
	private GpxLayer layer;
	/** determines whether an entry can be a parent for waypoints */
	private boolean isWaypointParent = false;

	public void setLayer(GpxLayer layer) {
		this.layer = layer;
	}

	public GpxLayer getLayer() {
		return layer;
	}

	/**
	 * Remembers the associated tree node.
	 * 
	 * @param node
	 */
	public void setNode(DefaultMutableTreeNode node) {
		this.node = node;
	}

	public DefaultMutableTreeNode getNode() {
		return node;
	}

	public void setWaypointParent(boolean isWaypointParent) {
		this.isWaypointParent = isWaypointParent;
	}

	public boolean isWaypointParent() {
		return isWaypointParent;
	}
}
