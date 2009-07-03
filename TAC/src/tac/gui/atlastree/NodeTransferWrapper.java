package tac.gui.atlastree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.tree.TreeNode;

import tac.program.interfaces.AtlasObject;

public class NodeTransferWrapper implements Transferable {

	public static final DataFlavor FLAVOR = new DataFlavor(AtlasObject.class, "AtlasObject");
	public static final DataFlavor[] FLAVORS = new DataFlavor[] { FLAVOR };

	private TreeNode node;

	public NodeTransferWrapper(TreeNode node) {
		this.node = node;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!FLAVOR.equals(flavor))
			throw new UnsupportedFlavorException(flavor);
		return node;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return FLAVORS;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return FLAVOR.equals(flavor);
	}

}
