package mobac.gui.atlastree;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.utilities.Utilities;


public class NodeRenderer implements TreeCellRenderer {

	private static ImageIcon atlasIcon = new ImageIcon();
	private static ImageIcon layerIcon = new ImageIcon();
	private static ImageIcon mapIcon = new ImageIcon();

	static {
		atlasIcon = Utilities.loadResourceImageIcon("atlas.png");
		layerIcon = Utilities.loadResourceImageIcon("layer.png");
		mapIcon = Utilities.loadResourceImageIcon("map.png");
	}

	DefaultTreeCellRenderer atlasRenderer;
	DefaultTreeCellRenderer layerRenderer;
	DefaultTreeCellRenderer mapRenderer;

	public NodeRenderer() {
		atlasRenderer = new SimpleTreeCellRenderer(atlasIcon);
		layerRenderer = new SimpleTreeCellRenderer(layerIcon);
		mapRenderer = new SimpleTreeCellRenderer(mapIcon);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {
		TreeCellRenderer tcr;
		if (value instanceof AtlasInterface) {
			tcr = atlasRenderer;
		} else if (value instanceof LayerInterface)
			tcr = layerRenderer;
		else
			tcr = mapRenderer;
		return tcr.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
				hasFocus);
	}

	protected static class SimpleTreeCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		public SimpleTreeCellRenderer(Icon icon) {
			super();
			setIcon(icon);
			setOpenIcon(icon);
			setClosedIcon(icon);
			setLeafIcon(icon);
		}
	}
}
