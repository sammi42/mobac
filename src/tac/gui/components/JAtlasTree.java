package tac.gui.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import tac.gui.MainGUI;
import tac.gui.mapview.PreviewMap;
import tac.program.MapSelection;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.CapabilityDeletable;
import tac.program.interfaces.CapabilityRenameable;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.program.interfaces.ToolTipProvider;
import tac.program.model.Atlas;
import tac.program.model.AtlasTreeModel;
import tac.program.model.AutoCutMultiMapLayer;
import tac.program.model.Profile;
import tac.program.model.TileImageParameters;
import tac.utilities.TACExceptionHandler;
import tac.utilities.Utilities;

public class JAtlasTree extends JTree implements MouseListener {

	private static final long serialVersionUID = 1L;

	private static final String ACTION_DELETE_NODE = "DELETE_NODE";

	private static final Logger log = Logger.getLogger(JAtlasTree.class);

	private AtlasTreeModel treeModel;

	private PreviewMap mapView;

	protected AtlasTreeCellRenderer treeCellRenderer;

	protected String defaultToolTiptext;

	protected KeyStroke deleteNodeKS;

	public JAtlasTree(PreviewMap mapView) {
		super(new AtlasTreeModel());
		if (mapView == null)
			throw new NullPointerException("MapView parameter is null");
		this.mapView = mapView;
		treeModel = (AtlasTreeModel) getModel();
		// setRootVisible(false);
		setShowsRootHandles(true);
		treeCellRenderer = new AtlasTreeCellRenderer();
		setCellRenderer(treeCellRenderer);
		setCellEditor(new AtlasTreeCellEditor());
		setEditable(true);
		setToolTipText("");
		defaultToolTiptext = "<html>Use context menu of the entries to see all available commands.</html>";
		addMouseListener(this);

		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getActionMap();

		// map moving
		inputMap.put(deleteNodeKS = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				ACTION_DELETE_NODE);
		actionMap.put(ACTION_DELETE_NODE, new AbstractAction("Delete") {

			public void actionPerformed(ActionEvent e) {
				deleteSelectedNode();
			}

		});

	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (getRowForLocation(event.getX(), event.getY()) == -1)
			return defaultToolTiptext;
		TreePath curPath = getPathForLocation(event.getX(), event.getY());
		Object o = curPath.getLastPathComponent();
		if (o == null || !(o instanceof ToolTipProvider))
			return null;
		return ((ToolTipProvider) o).getToolTip();
	}

	@Override
	public boolean isPathEditable(TreePath path) {
		return super.isPathEditable(path)
				&& (path.getLastPathComponent() instanceof CapabilityRenameable);
	}

	public AtlasTreeModel getTreeModel() {
		return treeModel;
	}

	public void clearAtlas() {
		log.debug("Resetting atlas tree model");
		Atlas newAtlas = new Atlas();
		newAtlas.setName(MainGUI.getMainGUI().getUserText());
		treeModel.setAtlas(newAtlas);
	}

	public void deleteSelectedNode() {
		TreePath path = getSelectionPath();
		if (path == null)
			return;
		TreeNode selected = (TreeNode) path.getLastPathComponent();
		int[] selectedRows = getSelectionRows();

		if (!(selected instanceof CapabilityDeletable))
			return;
		treeModel.notifyNodeDelete(selected);
		((CapabilityDeletable) selected).delete();

		int selRow = Math.min(selectedRows[0], getRowCount() - 1);
		TreePath path1 = path.getParentPath();
		TreePath path2 = getPathForRow(selRow).getParentPath();
		if (path1 != path2) {
			// next row belongs to different parent node -> we select parent
			// node instead
			setSelectionPath(path1);
		} else {
			setSelectionRow(selRow);
			scrollRowToVisible(selRow);
		}
	}

	public AtlasInterface getAtlas() {
		return treeModel.getAtlas();
	}

	public boolean load(Profile profile) {
		try {
			treeModel.load(profile);
			return true;
		} catch (Exception e) {
			TACExceptionHandler.processException(e);
			return false;
		}
	}

	public boolean save(Profile profile) {
		try {
			treeModel.save(profile);
			return true;
		} catch (Exception e) {
			TACExceptionHandler.processException(e);
			return false;
		}
	}

	protected void showNodePopupMenu(MouseEvent event) {
		JPopupMenu pm = new JPopupMenu();
		final TreePath selPath = getPathForLocation(event.getX(), event.getY());
		setSelectionPath(selPath);
		JMenuItem mi = null;
		if (selPath != null) {
			// not clicked on empty area
			final Object o = selPath.getLastPathComponent();
			if (o == null)
				return;
			if (o instanceof ToolTipProvider) {
				mi = new JMenuItem("Show item details");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ToolTipProvider ttp = (ToolTipProvider) o;
						JOptionPane.showMessageDialog(MainGUI.getMainGUI(), ttp.getToolTip());
					}
				});
				pm.add(mi);
			}
			if (o instanceof MapInterface) {
				mi = new JMenuItem("Display map area");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MapInterface map = (MapInterface) o;
						mapView.setMapSource(map.getMapSource());
						mapView.setSelectionByTileCoordinate(map.getZoom(), map
								.getMinTileCoordinate(), map.getMaxTileCoordinate(), true);
					}
				});
				pm.add(mi);
				mi = new JMenuItem("Zoom to");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MapInterface map = (MapInterface) o;
						MapSelection ms = new MapSelection(map);
						mapView.setMapSource(map.getMapSource());
						mapView.zoomToSelection(ms, true);
						mapView.setSelectionByTileCoordinate(map.getZoom(), map
								.getMinTileCoordinate(), map.getMaxTileCoordinate(), true);
					}
				});
				pm.add(mi);
			}
			if (o instanceof CapabilityRenameable) {
				mi = new JMenuItem("Rename");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JAtlasTree.this.startEditingAtPath(selPath);
					}
				});
				pm.add(mi);
			}
			if (o instanceof AutoCutMultiMapLayer) {
				mi = new JMenuItem("Apply tile processing options");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AutoCutMultiMapLayer acLayer = (AutoCutMultiMapLayer) o;
						TileImageParameters p = MainGUI.getMainGUI()
								.getSelectedTileImageParameters();
						acLayer.setParameters(p);
					}
				});
				pm.add(mi);
			}
			if (o instanceof CapabilityDeletable) {
				pm.addSeparator();
				mi = new JMenuItem(getActionMap().get(ACTION_DELETE_NODE));
				mi.setAccelerator(deleteNodeKS);
				pm.add(mi);
			}
		}
		if (pm.getComponentCount() > 0)
			pm.addSeparator();
		mi = new JMenuItem("Clear atlas");
		mi.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				clearAtlas();
			}
		});
		pm.add(mi);
		pm.show(this, event.getX(), event.getY());
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2)
			return;
		TreePath selPath = getSelectionPath();
		if (selPath == null)
			return; // clicked on empty area
		selectElementOnMap(selPath.getLastPathComponent());
	}

	protected void selectElementOnMap(Object o) {
		if (o instanceof MapInterface) {
			MapInterface map = (MapInterface) o;
			mapView.setMapSource(map.getMapSource());
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

	protected static class AtlasTreeCellRenderer implements TreeCellRenderer {

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

		public AtlasTreeCellRenderer() {
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

	}

	protected static class SimpleTreeCellRenderer extends DefaultTreeCellRenderer {
		public SimpleTreeCellRenderer(Icon icon) {
			super();
			setIcon(icon);
			setOpenIcon(icon);
			setClosedIcon(icon);
			setLeafIcon(icon);
		}
	}

	protected class AtlasTreeCellEditor extends DefaultTreeCellEditor {

		public AtlasTreeCellEditor() {
			super(JAtlasTree.this, null);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
				boolean expanded, boolean leaf, int row) {
			// Each node type has it's own TreeCellRenderer implementation
			// this not covered by DefaultTreeCellEditor - therefore we have to
			// correct the renderer each time an editorComponent is requested
			TreeCellRenderer tcr = tree.getCellRenderer();
			renderer = (DefaultTreeCellRenderer) tcr.getTreeCellRendererComponent(tree, value,
					isSelected, expanded, leaf, row, true);
			return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		}

		@Override
		protected TreeCellEditor createTreeCellEditor() {
			return new DefaultCellEditor(new JAtlasNameField());
		}

	}
}
