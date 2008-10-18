package tac.program.model;

import java.util.ArrayList;

public class Layer {
	private ArrayList<Map> maps = new ArrayList<Map>();
	private String name;

	public Layer(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Map> getMapList() {
		return maps;
	}

	public Map getMap(int index) {
		return maps.get(index);
	}

}
