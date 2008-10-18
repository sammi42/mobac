package tac.program.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Represents an atlas to be downloaded. An atlas contains of severals layers
 * and each layer consists of several maps.<br>
 * 
 * <p>
 * Example:
 * </p>
 * <b>Atlas</b>
 * <ul>
 * <li>
 * <p>
 * Layer 1
 * </p>
 * <ul>
 * <li>Map 1.1</li>
 * <li>Map 1.2</li>
 * <li>Map 1.3</li>
 * </ul>
 * </li>
 * <li>
 * <p>
 * Layer 2
 * </p>
 * <ul>
 * <li>Map 2.1</li>
 * <li>Map 2.2</li>
 * </ul>
 * </li>
 * <li>..</li>
 * 
 * </ul>
 * 
 */
public class Atlas {

	private ArrayList<Layer> layers = new ArrayList<Layer>();
	private String name;

	public Atlas(String name) {
		this.name = name;
	}

	public List<Layer> getLayerList() {
		return layers;
	}

	public Layer getLayer(int index) {
		return layers.get(index);
	}

	public void addLayer(Layer newLayer) {
		layers.add(newLayer);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
