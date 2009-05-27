package tac.program.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.LayerInterface;

public class Atlas implements AtlasInterface {

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

}
