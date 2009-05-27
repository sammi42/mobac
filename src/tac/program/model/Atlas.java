package tac.program.model;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreeNode;

import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.ToolTipProvider;

public class Atlas implements AtlasInterface, ToolTipProvider, TreeNode {

	private String name = "Atlas";
	private List<LayerInterface> layers = new LinkedList<LayerInterface>();

	private AtlasOutputFormat outputFormat = AtlasOutputFormat.TaredAtlas;

	public Atlas() {
		super();
	}

	public void addLayer(LayerInterface l) {
		layers.add(l);
	}

	public void deleteLayer(LayerInterface l) {
		layers.remove(l);
	}

	public LayerInterface getLayer(int index) {
		return layers.get(index);
	}

	public int getLayerCount() {
		return layers.size();
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public AtlasOutputFormat getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(AtlasOutputFormat atlasOutputFormat) {
		this.outputFormat = atlasOutputFormat;
	}

	@Override
	public String toString() {
		return getName();
	}

	public Iterator<LayerInterface> iterator() {
		return layers.iterator();
	}

	public long calculateTilesToDownload() {
		long tiles = 0;
		for (LayerInterface layer : layers)
			tiles += layer.calculateTilesToDownload();
		return tiles;
	}

	public String getToolTip() {
		StringWriter sw = new StringWriter(1024);
		sw.write("<html>");
		sw.write("<b>Atlas</b><br>");
		sw.write("Name: " + name + "<br>");
		sw.write("Layer count: " + layers.size() + "<br>");
		sw.write("Maximum tiles to download: " + calculateTilesToDownload() + "<br>");
		sw.write("</html>");
		return sw.toString();
	}

	public Enumeration<?> children() {
		return Collections.enumeration(layers);
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public TreeNode getChildAt(int childIndex) {
		return (TreeNode) layers.get(childIndex);
	}

	public int getChildCount() {
		return layers.size();
	}

	public int getIndex(TreeNode node) {
		return layers.indexOf(node);
	}

	public TreeNode getParent() {
		return null;
	}

	public boolean isLeaf() {
		return false;
	}

}
