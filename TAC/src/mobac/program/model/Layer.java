package mobac.program.model;

import java.awt.Dimension;
import java.awt.Point;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.TreeNode;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import mobac.exceptions.InvalidNameException;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.CapabilityDeletable;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.ToolTipProvider;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


/**
 * A layer holding one or multiple maps of the same map source and the same zoom
 * level. The number of maps depends on the size of the covered area - if it is
 * smaller than the specified <code>maxMapSize</code> then there will be only
 * one map.
 * 
 */
@XmlRootElement
public class Layer implements LayerInterface, TreeNode, ToolTipProvider, CapabilityDeletable,
		Iterable<MapInterface> {

	private static Logger log = Logger.getLogger(Layer.class);

	@XmlTransient
	private AtlasInterface atlasInterface;

	private String name;

	@XmlElements( { @XmlElement(name = "Map", type = Map.class) })
	private LinkedList<MapInterface> maps = new LinkedList<MapInterface>();

	protected Layer() {
	}

	public Layer(AtlasInterface atlasInterface, String name) throws InvalidNameException {
		this.atlasInterface = atlasInterface;
		setName(name);
	}

	public void addMapsAutocut(String mapNameBase, MapSource mapSource,
			EastNorthCoordinate minCoordinate, EastNorthCoordinate maxCoordinate, int zoom,
			TileImageParameters parameters, int maxMapSize) throws InvalidNameException {
		MapSpace mapSpace = mapSource.getMapSpace();
		addMapsAutocut(mapNameBase, mapSource, minCoordinate.toTileCoordinate(mapSpace, zoom),
				maxCoordinate.toTileCoordinate(mapSpace, zoom), zoom, parameters, maxMapSize);
	}

	public void addMapsAutocut(String mapNameBase, MapSource mapSource, Point minTileCoordinate,
			Point maxTileCoordinate, int zoom, TileImageParameters parameters, int maxMapSize)
			throws InvalidNameException {
		log.trace("Adding new map(s): \"" + mapNameBase + "\" " + mapSource + " zoom=" + zoom
				+ " min=" + minTileCoordinate.x + "/" + minTileCoordinate.y + " max="
				+ maxTileCoordinate.x + "/" + maxTileCoordinate.y);

		int tileSize = mapSource.getMapSpace().getTileSize();

		minTileCoordinate.x -= minTileCoordinate.x % tileSize;
		minTileCoordinate.y -= minTileCoordinate.y % tileSize;

		maxTileCoordinate.x += tileSize - 1 - (maxTileCoordinate.x % tileSize);
		maxTileCoordinate.y += tileSize - 1 - (maxTileCoordinate.y % tileSize);

		Dimension tileDimension;
		if (parameters == null)
			tileDimension = new Dimension(tileSize, tileSize);
		else
			tileDimension = parameters.getDimension();
		// We adapt the max map size to the tile size so that we do
		// not get ugly cutted/incomplete tiles at the borders
		Dimension maxMapDimension = new Dimension(maxMapSize, maxMapSize);
		maxMapDimension.width -= maxMapSize % tileDimension.width;
		maxMapDimension.height -= maxMapSize % tileDimension.height;

		int mapWidth = maxTileCoordinate.x - minTileCoordinate.x;
		int mapHeight = maxTileCoordinate.y - minTileCoordinate.y;
		if (mapWidth < maxMapDimension.width && mapHeight < maxMapDimension.height) {
			Map s = new Map(this, mapNameBase, mapSource, zoom, minTileCoordinate,
					maxTileCoordinate, parameters);
			maps.add(s);
			return;
		}
		int mapCounter = 0;
		for (int mapX = minTileCoordinate.x; mapX < maxTileCoordinate.x; mapX += maxMapDimension.width) {
			for (int mapY = minTileCoordinate.y; mapY < maxTileCoordinate.y; mapY += maxMapDimension.height) {
				int maxX = Math.min(mapX + maxMapDimension.width, maxTileCoordinate.x);
				int maxY = Math.min(mapY + maxMapDimension.height, maxTileCoordinate.y);
				Point min = new Point(mapX, mapY);
				Point max = new Point(maxX - 1, maxY - 1);
				String mapName = String.format("%s (%02d)", new Object[] { mapNameBase,
						mapCounter++ });
				Map s = new Map(this, mapName, mapSource, zoom, min, max, parameters);
				maps.add(s);
			}
		}
	}

	public void delete() {
		maps.clear();
		atlasInterface.deleteLayer(this);
	}

	public AtlasInterface getAtlas() {
		return atlasInterface;
	}

	public void addMap(MapInterface map) {
		// TODO: Add name collision check
		maps.add(map);
		map.setLayer(this);
	}

	public MapInterface getMap(int index) {
		return maps.get(index);
	}

	public int getMapCount() {
		return maps.size();
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String newName) throws InvalidNameException {
		if (atlasInterface != null) {
			for (LayerInterface layer : atlasInterface) {
				if ((layer != this) && newName.equals(layer.getName()))
					throw new InvalidNameException("There is already a layer named \"" + newName
							+ "\" in this atlas.\nLayer names have to unique within an atlas.");
			}
		}
		this.name = newName;
	}

	@Override
	public String toString() {
		return name;
	}

	public int calculateTilesToDownload() {
		int result = 0;
		for (MapInterface map : maps)
			result += map.calculateTilesToDownload();
		return result;
	}

	public String getToolTip() {
		StringWriter sw = new StringWriter(1024);
		sw.write("<html>");
		sw.write("<b>Layer</b><br>");
		sw.write("Map count: " + maps.size() + "<br>");
		sw.write("Maximum tiles to download: " + calculateTilesToDownload() + "<br>");
		sw.write("</html>");
		return sw.toString();
	}

	public Iterator<MapInterface> iterator() {
		return maps.iterator();
	}

	public Enumeration<?> children() {
		return Collections.enumeration(maps);
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public TreeNode getChildAt(int childIndex) {
		return (TreeNode) maps.get(childIndex);
	}

	public int getChildCount() {
		return maps.size();
	}

	public int getIndex(TreeNode node) {
		return maps.indexOf(node);
	}

	public TreeNode getParent() {
		return (TreeNode) atlasInterface;
	}

	public boolean isLeaf() {
		return false;
	}

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		this.atlasInterface = (Atlas) parent;
	}

	public boolean checkData() {
		if (atlasInterface == null)
			return true;
		if (name == null)
			return true;
		// Check for duplicate map names
		HashSet<String> names = new HashSet<String>(maps.size());
		for (MapInterface map : maps)
			names.add(map.getName());
		if (names.size() < maps.size())
			return true; // at least one duplicate name found
		return false;
	}

	public void deleteMap(Map map) {
		maps.remove(map);
	}

	public LayerInterface deepClone(AtlasInterface atlas) {
		Layer layer = new Layer();
		layer.atlasInterface = atlas;
		layer.name = name;
		for (MapInterface map : maps)
			layer.maps.add(map.deepClone(layer));
		return layer;
	}

}
