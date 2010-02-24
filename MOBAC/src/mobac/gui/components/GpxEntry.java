package mobac.gui.components;

import javax.swing.tree.DefaultMutableTreeNode;

import mobac.gui.mapview.GpxLayer;

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
