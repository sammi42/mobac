package mobac.program.model;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.ToolTipProvider;


@XmlRootElement
public class Atlas implements AtlasInterface, ToolTipProvider, TreeNode {

	public static final int CURRENT_ATLAS_VERSION = 1;

	@XmlAttribute
	private int version = 0;

	private String name = "Unnamed atlas";

	@XmlElements( { @XmlElement(name = "Layer", type = Layer.class) })
	private List<LayerInterface> layers = new LinkedList<LayerInterface>();

	private AtlasOutputFormat outputFormat = AtlasOutputFormat.TaredAtlas;

	public static Atlas newInstance() {
		Atlas atlas = new Atlas();
		atlas.version = CURRENT_ATLAS_VERSION;
		return atlas;
	}

	private Atlas() {
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

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	@XmlAttribute
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

	public int calculateTilesToDownload() {
		int tiles = 0;
		for (LayerInterface layer : layers)
			tiles += layer.calculateTilesToDownload();
		return tiles;
	}

	public boolean checkData() {
		if (name == null) // name set?
			return true;
		// Check for duplicate layer names
		HashSet<String> names = new HashSet<String>(layers.size());
		for (LayerInterface layer : layers)
			names.add(layer.getName());
		if (names.size() < layers.size())
			return true; // at least one duplicate name found
		return false;
	}

	public String getToolTip() {
		StringWriter sw = new StringWriter(1024);
		sw.write("<html>");
		sw.write("<b>Atlas</b><br>");
		sw.write("Name: " + name + "<br>");
		sw.write("Layer count: " + layers.size() + "<br>");
		sw.write("Maximum tiles to download: " + calculateTilesToDownload() + "<br>");
		sw.write("Atlas format: " + outputFormat + "<br>");
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

	public int getVersion() {
		return version;
	}

	public AtlasInterface deepClone() {
		Atlas atlas = new Atlas();
		atlas.version = version;
		atlas.name = name;
		atlas.outputFormat = outputFormat;
		for (LayerInterface layer : layers) {
			atlas.layers.add(layer.deepClone(atlas));
		}
		return atlas;
	}

}
