package tac.program.model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.CapabilityDeletable;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;

public class SimpleLayer implements LayerInterface, CapabilityDeletable {

	private String name;
	private Atlas atlas;
	private List<SimpleMap> maps;

	public SimpleLayer(Atlas atlas, String name) {
		super();
		maps = new LinkedList<SimpleMap>();
		this.atlas = atlas;
		this.name = name;
		atlas.addLayer(this);
	}

	public MapInterface getMap(int index) {
		return maps.get(index);
	}

	public int getMapCount() {
		return maps.size();
	}

	public void delete() {
		atlas.deleteLayer(this);
	}

	public String getName() {
		return name;
	}

	public AtlasInterface getAtlas() {
		return atlas;
	}

	@Override
	public String toString() {
		return getName();
	}

	public SimpleMap addMap(String name, MapSource mapSource, Point maxTileNum, Point minTileNum,
			int zoom, Dimension tileSize) {
		SimpleMap m = new SimpleMap(this, name, mapSource, maxTileNum, minTileNum, zoom, tileSize);
		maps.add(m);
		return m;
	}

	public void deleteMap(SimpleMap map) {
		maps.remove(map);
	}

}
