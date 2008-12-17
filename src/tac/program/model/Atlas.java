package tac.program.model;

import java.util.LinkedList;
import java.util.List;

import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.LayerInterface;

public class Atlas implements AtlasInterface {

	private String name = "Atlas";
	private List<LayerInterface> layers = new LinkedList<LayerInterface>();

	protected void addLayer(LayerInterface l) {
		layers.add(l);
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

	@Override
	public String toString() {
		return getName();
	}

}
