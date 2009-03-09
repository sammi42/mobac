package tac.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import tac.gui.preview.PreviewMap;
import tac.program.interfaces.CapabilityDeletable;
import tac.program.interfaces.MapInterface;
import tac.program.interfaces.ToolTipProvider;
import tac.program.model.Atlas;
import tac.program.model.AtlasTreeModel;
import tac.program.model.AutoCutMultiMapLayer;
import tac.utilities.Utilities;

public class AtlasTree extends JTree implements MouseListener {

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
		addMouseListener(this);
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

	protected void showNodePopupMenu(MouseEvent event) {
		JPopupMenu pm = new JPopupMenu();
		TreePath selPath = getPathForLocation(event.getX(), event.getY());
		if (selPath == null)
			return; // clicked on empty area
		final Object o = selPath.getLastPathComponent();
		if (o == null)
			return;
		if (o instanceof CapabilityDeletable) {
			JMenuItem mi = new JMenuItem("Delete");
			mi.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					((CapabilityDeletable) o).delete();
					treeModel.notifyStructureChanged();
				}
			});
			pm.add(mi);
		}
		pm.show(this, event.getX(), event.getY());
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2)
			return;
		TreePath selPath = getSelectionPath();
		if (selPath == null)
			return; // clicked on empty area
		Object o = selPath.getLastPathComponent();
		if (o instanceof MapInterface) {
			MapInterface map = (MapInterface) o;
			mapView.setSelectionByTileCoordinate(map.getZoom(), map.getMinTileCoordinate(), map
					.getMaxTileCoordinate(), true);
		} else if (o instanceof AutoCutMultiMapLayer) {
			AutoCutMultiMapLayer layer = (AutoCutMultiMapLayer) o;
			mapView.setSelectionByTileCoordinate(layer.getZoom(), layer.getMinTileCoordinate(),
					layer.getMaxTileCoordinate(), true);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showNodePopupMenu(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showNodePopupMenu(e);
		}
	}

	protected static class AtlasTreeCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		private static ImageIcon layerIcon = new ImageIcon();
		private static ImageIcon mapIcon = new ImageIcon();

		static {
			layerIcon = Utilities.loadResourceImageIcon("layer.png");
			mapIcon = Utilities.loadResourceImageIcon("map.png");
		}

		public AtlasTreeCellRenderer() {
			setLeafIcon(mapIcon);
			setOpenIcon(layerIcon);
			setClosedIcon(layerIcon);
		}

	}

}
