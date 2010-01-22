package mobac.gui.atlastree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.tree.TreeNode;

import mobac.program.interfaces.AtlasObject;


public class NodeTransferWrapper implements Transferable {

	public static final DataFlavor ATLAS_OBJECT_FLAVOR = new DataFlavor(AtlasObject.class, "AtlasObject");
	public static final DataFlavor[] FLAVORS = new DataFlavor[] { ATLAS_OBJECT_FLAVOR };

	private TreeNode node;

	public NodeTransferWrapper(TreeNode node) {
		this.node = node;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!ATLAS_OBJECT_FLAVOR.equals(flavor))
			throw new UnsupportedFlavorException(flavor);
		return node;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return FLAVORS;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return ATLAS_OBJECT_FLAVOR.equals(flavor);
	}

}
