package tac.gui.atlastree;

import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import tac.exceptions.InvalidNameException;
import tac.program.interfaces.LayerInterface;
import tac.program.model.AtlasTreeModel;

public class DragDropController {

	public DragDropController(JAtlasTree atlasTree) {
		super();
		this.atlasTree = atlasTree;
		new AtlasDragSource();
		new AtlasDropTarget();
	}

	JAtlasTree atlasTree;

	protected class AtlasDragSource implements DragSourceListener, DragGestureListener {

		DragGestureRecognizer recognizer;

		Transferable transferable;

		DragSource source;
		TreeNode oldNode;

		public AtlasDragSource() {
			source = new DragSource();
			recognizer = source.createDefaultDragGestureRecognizer(atlasTree,
					DnDConstants.ACTION_MOVE, this);
		}

		public void dragGestureRecognized(DragGestureEvent dge) {
			TreePath path = atlasTree.getSelectionPath();
			if ((path == null) || (path.getPathCount() <= 1)) {
				// We can't move the root node or an empty selection
				return;
			}
			oldNode = (TreeNode) path.getLastPathComponent();
			if (!(oldNode instanceof LayerInterface))
				return; // Only layers can be dragged
			transferable = new NodeTransferWrapper(oldNode);
			source.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable, this);
		}

		/**
		 * Called whenever the drop target changes and it has bee accepted (
		 */
		public void dragEnter(DragSourceDragEvent dsde) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
		}

		public void dragOver(DragSourceDragEvent dsde) {
		}

		public void dragDropEnd(DragSourceDropEvent dsde) {
		}

		public void dragExit(DragSourceEvent dse) {
			dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
		}

		public void dropActionChanged(DragSourceDragEvent dsde) {
		}

	}

	protected class AtlasDropTarget implements DropTargetListener {

		DropTarget target;

		public AtlasDropTarget() throws HeadlessException {
			super();
			target = new DropTarget(atlasTree, this);
		}

		public synchronized void dragEnter(DropTargetDragEvent dtde) {
		}

		public synchronized void dragExit(DropTargetEvent dte) {
		}

		public synchronized void dragOver(DropTargetDragEvent dtde) {
			TreeNode node = getNodeForEvent(dtde);
			if (node instanceof LayerInterface)
				dtde.acceptDrag(dtde.getDropAction());
			else
				dtde.rejectDrag();
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		public synchronized void drop(DropTargetDropEvent dtde) {
			try {
				TreeNode sourceNode = (TreeNode) dtde.getTransferable().getTransferData(
						NodeTransferWrapper.FLAVOR);

				Point pt = dtde.getLocation();
				DropTargetContext dtc = dtde.getDropTargetContext();
				JTree tree = (JTree) dtc.getComponent();
				TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
				TreeNode targetNode = (TreeNode) parentpath.getLastPathComponent();

				if (targetNode.equals(sourceNode) || targetNode.getParent().equals(sourceNode)) {
					dtde.rejectDrop();
					return;
				}
				LayerInterface sourceLayer = (LayerInterface) sourceNode;
				LayerInterface targetLayer = (LayerInterface) targetNode;
				int answer = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to merge the maps of layer\n" + "\""
								+ sourceLayer.getName() + "\" into layer " + "\""
								+ targetLayer.getName() + "\"?", "Confirm layer merging",
						JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION) {
					try {
						((AtlasTreeModel) atlasTree.getModel()).mergeLayers(sourceLayer,
								targetLayer);
					} catch (InvalidNameException e) {
						JOptionPane.showMessageDialog(null, e.getMessage(), "Layer merging failed",
								JOptionPane.ERROR_MESSAGE);
						throw e;
					}
				}
			} catch (Exception e) {
				dtde.rejectDrop();
			}
		}

		private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {
			Point p = dtde.getLocation();
			DropTargetContext dtc = dtde.getDropTargetContext();
			JTree tree = (JTree) dtc.getComponent();
			TreePath path = tree.getClosestPathForLocation(p.x, p.y);
			return (TreeNode) path.getLastPathComponent();
		}

	}

}
