package tac.program.model;

import java.awt.Dimension;
import java.awt.Point;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.TreeNode;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.InvalidNameException;
import tac.program.DownloadJobEnumerator;
import tac.program.JobDispatcher.Job;
import tac.program.MapCreatorCustom.TileImageParameters;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.CapabilityDeletable;
import tac.program.interfaces.CapabilityRenameable;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.DownloadableElement;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.program.interfaces.ToolTipProvider;
import tac.program.jaxb.DimensionAdapter;
import tac.program.jaxb.PointAdapter;
import tac.tar.TarIndexedArchive;
import tac.utilities.MyMath;

//import tac.utilities.MyMath;

/**
 * A layer holding one or multiple maps of the same map source and the same zoom
 * level. The number of maps depends on the size of the covered area - if it is
 * smaller than the specified <code>maxMapSize</code> then there will be only
 * one map.
 * 
 */
@XmlRootElement
public class AutoCutMultiMapLayer implements LayerInterface, TreeNode, DownloadableElement,
		ToolTipProvider, CapabilityDeletable, CapabilityRenameable, Iterable<MapInterface> {

	@XmlTransient
	private AtlasInterface atlas;

	private String name;

	@XmlAttribute
	private MapSource mapSource;

	@XmlAttribute
	@XmlJavaTypeAdapter(PointAdapter.class)
	private Point maxTileCoordinate;
	private Point minTileCoordinate;
	private Point maxTileNum;
	private Point minTileNum;

	@XmlAttribute
	private int zoom;

	private TileImageParameters parameters;

	private Dimension tileDimension;
	
	@XmlAttribute
	@XmlJavaTypeAdapter(DimensionAdapter.class)
	private Dimension maxMapDimension;

	@XmlElements( { @XmlElement(name = "SubMap", type = SubMap.class) })
	private LinkedList<MapInterface> maps = new LinkedList<MapInterface>();

	protected AutoCutMultiMapLayer() {
	}

	public AutoCutMultiMapLayer(Atlas atlas, String name, MapSource mapSource,
			Point minTileCoordinate, Point maxTileCoordinate, int zoom,
			TileImageParameters parameters, int maxMapSize) throws InvalidNameException {
		this.atlas = atlas;
		setName(name);
		this.mapSource = mapSource;
		this.minTileCoordinate = minTileCoordinate;
		this.maxTileCoordinate = maxTileCoordinate;
		this.zoom = zoom;
		this.parameters = parameters;

		maxTileNum = new Point(maxTileCoordinate.x / Tile.SIZE, maxTileCoordinate.y / Tile.SIZE);
		minTileNum = new Point(minTileCoordinate.x / Tile.SIZE, minTileCoordinate.y / Tile.SIZE);

		minTileCoordinate.x = minTileCoordinate.x - (minTileCoordinate.x % Tile.SIZE);
		minTileCoordinate.y = minTileCoordinate.y - (minTileCoordinate.y % Tile.SIZE);

		maxTileCoordinate.x = maxTileCoordinate.x + 255 - (maxTileCoordinate.x % Tile.SIZE);
		maxTileCoordinate.y = maxTileCoordinate.y + 255 - (maxTileCoordinate.y % Tile.SIZE);

		if (parameters == null)
			tileDimension = new Dimension(Tile.SIZE, Tile.SIZE);
		else
			tileDimension = new Dimension(parameters.width, parameters.height);
		maxMapDimension = new Dimension(maxMapSize, maxMapSize);

		// We adapt the max map size to the tile size so that we do
		// not get ugly cutted/incomplete tiles at the borders
		maxMapDimension.width -= maxMapSize % tileDimension.width;
		maxMapDimension.height -= maxMapSize % tileDimension.height;
		calculateSubMaps();

		atlas.addLayer(this);
	}

	protected void calculateSubMaps() {
		int mapWidth = maxTileCoordinate.x - minTileCoordinate.x;
		int mapHeight = maxTileCoordinate.y - minTileCoordinate.y;
		maps.clear();
		if (mapWidth < maxMapDimension.width && mapHeight < maxMapDimension.height) {
			SubMap s = new SubMap(this, name, minTileCoordinate, maxTileCoordinate);
			maps.add(s);
			return;
		}
		/*
		 * int mapCountX = MyMath.divCeil(maxTileCoordinate.x -
		 * minTileCoordinate.x, maxMapDimension.width); int mapCountY =
		 * MyMath.divCeil(maxTileCoordinate.y - minTileCoordinate.y,
		 * maxMapDimension.height);
		 */
		int mapCounter = 0;
		for (int mapX = minTileCoordinate.x; mapX < maxTileCoordinate.x; mapX += maxMapDimension.width) {
			for (int mapY = minTileCoordinate.y; mapY < maxTileCoordinate.y; mapY += maxMapDimension.height) {
				int maxX = Math.min(mapX + maxMapDimension.width, maxTileCoordinate.x);
				int maxY = Math.min(mapY + maxMapDimension.height, maxTileCoordinate.y);
				Point min = new Point(mapX, mapY);
				Point max = new Point(maxX - 1, maxY - 1);
				String mapName = String.format("%s-%02d", new Object[] { name, mapCounter++ });
				SubMap s = new SubMap(this, mapName, min, max);
				maps.add(s);
			}
		}
	}

	public Enumeration<Job> getDownloadJobs(TarIndexedArchive tileArchive,
			DownloadJobListener listener) {
		return new DownloadJobEnumerator(minTileNum.x, maxTileNum.x, minTileNum.y, maxTileNum.y,
				zoom, mapSource, tileArchive, listener);
	}

	public void delete() {
		atlas.deleteLayer(this);
	}

	public AtlasInterface getAtlas() {
		return atlas;
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
		if (atlas != null) {
			for (LayerInterface layer : atlas) {
				if ((layer != this) && newName.equals(layer.getName()))
					throw new InvalidNameException("There is already a layer named \"" + newName
							+ "\" in this atlas.\nLayer names have to unique within an atlas.");
			}
		}
		this.name = newName;
	}

	public Point getMaxTileCoordinate() {
		return maxTileCoordinate;
	}

	public Point getMinTileCoordinate() {
		return minTileCoordinate;
	}

	public int getZoom() {
		return zoom;
	}

	public MapSource getMapSource() {
		return mapSource;
	}

	public TileImageParameters getParameters() {
		return parameters;
	}

	public void setParameters(TileImageParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return name;
	}

	public long calculateTilesToDownload() {
		long result = 0;
		for (MapInterface map : maps)
			result += map.calculateTilesToDownload();
		return result;
	}

	public String getToolTip() {
		StringWriter sw = new StringWriter(1024);
		sw.write("<html>");
		sw.write("<b>Layer</b><br>");
		sw.write("Map count: " + maps.size() + "<br>");
		sw.write("Map source: " + mapSource.getName() + "<br>");
		sw.write("Zoom level: " + zoom + "<br>");
		if (parameters != null) {
			sw.write("Tile size: " + parameters.width + "x" + parameters.height + "<br>");
			sw.write("Tile format: " + parameters.format + "<br>");
		} else
			sw.write("Tile size: 256x256 (no processing)<br>");

		sw.write("Maximum tiles to download: " + calculateTilesToDownload() + "<br>");
		sw.write("Max map size: " + maxMapDimension.width + "x" + maxMapDimension.height + "<br>");
		sw.write("</html>");
		return sw.toString();
	}

	public static class SubMap implements MapInterface, ToolTipProvider, CapabilityDeletable,
			CapabilityRenameable, TreeNode {

		private String name;

		private AutoCutMultiMapLayer layer;

		@XmlAttribute
		@XmlJavaTypeAdapter(PointAdapter.class)
		private Point maxTileCoordinate;

		@XmlAttribute
		@XmlJavaTypeAdapter(PointAdapter.class)
		private Point minTileCoordinate;

		protected SubMap() {
		}

		protected SubMap(AutoCutMultiMapLayer layer, String name, Point minTileCoordinate,
				Point maxTileCoordinate) {
			this.layer = layer;
			this.maxTileCoordinate = maxTileCoordinate;
			this.minTileCoordinate = minTileCoordinate;
			this.name = name;
		}

		public AutoCutMultiMapLayer getLayer() {
			return layer;
		}

		public MapSource getMapSource() {
			return layer.mapSource;
		}

		public Point getMaxTileCoordinate() {
			return maxTileCoordinate;
		}

		public Point getMinTileCoordinate() {
			return minTileCoordinate;
		}

		@XmlAttribute
		public String getName() {
			return name;
		}

		public int getZoom() {
			return layer.zoom;
		}

		@Override
		public String toString() {
			return getName();
		}

		public String getToolTip() {
			EastNorthCoordinate tl = new EastNorthCoordinate(layer.zoom, minTileCoordinate.x,
					minTileCoordinate.y);
			EastNorthCoordinate br = new EastNorthCoordinate(layer.zoom, maxTileCoordinate.x,
					maxTileCoordinate.y);

			StringWriter sw = new StringWriter(1024);
			sw.write("<html>");
			sw.write("<b>Map area</b><br>");
			sw.write("Map source: " + layer.mapSource.getName() + "<br>");
			sw.write("Zoom level: " + layer.zoom + "<br>");
			sw.write("Area start: " + tl + " (" + minTileCoordinate.x + " / " + minTileCoordinate.y
					+ ")<br>");
			sw.write("Area end: " + br + " (" + maxTileCoordinate.x + " / " + maxTileCoordinate.y
					+ ")<br>");
			sw.write("Map size: " + (maxTileCoordinate.x - minTileCoordinate.x + 1) + "x"
					+ (maxTileCoordinate.y - minTileCoordinate.y + 1) + " pixel<br>");
			sw.write("Maximum tiles to download: " + calculateTilesToDownload() + "<br>");
			sw.write("</html>");
			return sw.toString();
		}

		public Dimension getTileSize() {
			return layer.tileDimension;
		}

		public void delete() {
			layer.maps.remove(this);
		}

		public void setName(String newName) throws InvalidNameException {
			if (layer != null) {
				for (MapInterface map : layer) {
					if ((map != this) && (newName.equals(map.getName())))
						throw new InvalidNameException("There is already a map named \"" + newName
								+ "\" in this layer.\nMap names have to unique within an layer.");
				}
			}
			this.name = newName;
		}

		public Enumeration<?> children() {
			return null;
		}

		public boolean getAllowsChildren() {
			return false;
		}

		public TreeNode getChildAt(int childIndex) {
			return null;
		}

		public int getChildCount() {
			return 0;
		}

		public int getIndex(TreeNode node) {
			return 0;
		}

		public TreeNode getParent() {
			return layer;
		}

		public boolean isLeaf() {
			return true;
		}

		public long calculateTilesToDownload() {
			long width = MyMath.divCeil((maxTileCoordinate.x - minTileCoordinate.x) + 1, Tile.SIZE);
			long height = MyMath
					.divCeil((maxTileCoordinate.y - minTileCoordinate.y) + 1, Tile.SIZE);
			return width * height;
		}

		public void afterUnmarshal(Unmarshaller u, Object parent) {
			this.layer = (AutoCutMultiMapLayer) parent;
		}
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
		return (TreeNode) atlas;
	}

	public boolean isLeaf() {
		return false;
	}

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		this.atlas = (Atlas) parent;
	}
}
