package tac.program.model;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import tac.gui.preview.MapSources;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;

public class AtlasTreeModel implements TreeModel {

	protected Atlas atlas;

	public AtlasTreeModel() {
		super();
		atlas = new Atlas();
		
		//TODO remove test date when finished
		// Some test data
		SimpleLayer l1 = new SimpleLayer(atlas, "Test1");
		l1.addMap("Map1", MapSources.getMapSources()[0], new Point(1, 1), new Point(2, 2), 2,
				new Dimension(256, 256));
		SimpleLayer l2 = new SimpleLayer(atlas, "Test2");
		l2.addMap("Map2", MapSources.getMapSources()[0], new Point(1, 1), new Point(2, 2), 2,
				new Dimension(256, 256));
		new AutoCutMultiMapLayer(atlas, "Test3", MapSources.getMapSources()[0], new Point(
				256 * 200, 256 * 200), new Point(256 * 400, 256 * 400), 8, new Dimension(256, 256),
				32768);
	}

	public void addTreeModelListener(TreeModelListener l) {
	}

	public void removeTreeModelListener(TreeModelListener l) {
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
		return atlas;
	}

	public boolean isLeaf(Object node) {
		if (node instanceof MapInterface)
			return true;
		return false;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {

	}

}
