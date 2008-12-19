package tac.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import tac.StartTAC;
import tac.gui.preview.PreviewMap;
import tac.program.interfaces.MapInterface;
import tac.program.interfaces.ToolTipProvider;
import tac.program.model.Atlas;
import tac.program.model.AtlasTreeModel;

public class AtlasTree extends JTree {

	private static final long serialVersionUID = 1L;

	private AtlasTreeModel treeModel;

	private PreviewMap mapView;

	public AtlasTree(PreviewMap mapView) {
		super(new AtlasTreeModel());
		if (mapView == null)
			throw new NullPointerException("mapView is null");
		this.mapView = mapView;
		treeModel = (AtlasTreeModel) getModel();
		setRootVisible(false);
		setShowsRootHandles(true);
		setCellRenderer(new AtlasTreeCellRenderer());
		setToolTipText("");
		addMouseListener(new MouseListener());
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (getRowForLocation(event.getX(), event.getY()) == -1)
			return null;
		TreePath curPath = getPathForLocation(event.getX(), event.getY());
		Object o = curPath.getLastPathComponent();
		if (o == null || !(o instanceof ToolTipProvider))
			return null;
		return ((ToolTipProvider) o).getToolTip();

	}

	public AtlasTreeModel getTreeModel() {
		return treeModel;
	}

	public void clearAtlas() {
		treeModel.setAtlas(new Atlas());
	}

	public Atlas getAtlas() {
		return treeModel.getAtlas();
	}

	protected class MouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2)
				return;
			TreePath selPath = getPathForLocation(e.getX(), e.getY());
			System.out.println(selPath);
			if (selPath == null)
				return; // clicked on empty area
			Object o = selPath.getLastPathComponent();
			if (o instanceof MapInterface) {
				MapInterface map = (MapInterface) o;
				mapView.setSelectionByTileCoordinate(map.getZoom(), map.getMinTileCoordinate(), map
						.getMaxTileCoordinate(), true);
			}
		}
	}

	protected static class AtlasTreeCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		private static ImageIcon layerIcon = new ImageIcon();
		private static ImageIcon mapIcon = new ImageIcon();

		static {
			InputStream imageStream;
			try {
				imageStream = StartTAC.class.getResourceAsStream("images/layer.png");
				layerIcon.setImage(ImageIO.read(imageStream));
				imageStream = StartTAC.class.getResourceAsStream("images/map.png");
				mapIcon.setImage(ImageIO.read(imageStream));
			} catch (Exception e) {
			}
		}

		public AtlasTreeCellRenderer() {
			setLeafIcon(mapIcon);
			setOpenIcon(layerIcon);
			setClosedIcon(layerIcon);
		}

	}
}
