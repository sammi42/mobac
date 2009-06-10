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

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.InvalidNameException;
import tac.program.DownloadJobEnumerator;
import tac.program.JobDispatcher.Job;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.CapabilityDeletable;
import tac.program.interfaces.CapabilityRenameable;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.DownloadableElement;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.program.interfaces.ToolTipProvider;
import tac.tar.TarIndexedArchive;
import tac.utilities.MyMath;

/**
 * A layer holding one or multiple maps of the same map source and the same zoom
 * level. The number of maps depends on the size of the covered area - if it is
 * smaller than the specified <code>maxMapSize</code> then there will be only
 * one map.
 * 
 */
@XmlRootElement
public class AutoCutMultiMapLayer implements LayerInterface, TreeNode, ToolTipProvider,
		CapabilityDeletable, CapabilityRenameable, Iterable<MapInterface> {

	@XmlTransient
	private AtlasInterface atlas;

	private String name;

	@XmlElements( { @XmlElement(name = "SubMap", type = SubMap.class) })
	private LinkedList<MapInterface> maps = new LinkedList<MapInterface>();

	protected AutoCutMultiMapLayer() {
	}

	public AutoCutMultiMapLayer(AtlasInterface atlas, String name, MapSource mapSource,
			Point minTileCoordinate, Point maxTileCoordinate, int zoom,
			TileImageParameters parameters, int maxMapSize) throws InvalidNameException {
		this.atlas = atlas;
		setName(name);

		minTileCoordinate.x = minTileCoordinate.x - (minTileCoordinate.x % Tile.SIZE);
		minTileCoordinate.y = minTileCoordinate.y - (minTileCoordinate.y % Tile.SIZE);

		maxTileCoordinate.x = maxTileCoordinate.x + 255 - (maxTileCoordinate.x % Tile.SIZE);
		maxTileCoordinate.y = maxTileCoordinate.y + 255 - (maxTileCoordinate.y % Tile.SIZE);

		Dimension tileDimension;
		if (parameters == null)
			tileDimension = new Dimension(Tile.SIZE, Tile.SIZE);
		else
			tileDimension = new Dimension(parameters.width, parameters.height);
		// We adapt the max map size to the tile size so that we do
		// not get ugly cutted/incomplete tiles at the borders
		Dimension maxMapDimension = new Dimension(maxMapSize, maxMapSize);
		maxMapDimension.width -= maxMapSize % tileDimension.width;
		maxMapDimension.height -= maxMapSize % tileDimension.height;

		int mapWidth = maxTileCoordinate.x - minTileCoordinate.x;
		int mapHeight = maxTileCoordinate.y - minTileCoordinate.y;
		maps.clear();
		if (mapWidth < maxMapDimension.width && mapHeight < maxMapDimension.height) {
			SubMap s = new SubMap(this, name, mapSource, zoom, minTileCoordinate,
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
				String mapName = String.format("%s-%02d", new Object[] { name, mapCounter++ });
				SubMap s = new SubMap(this, mapName, mapSource, zoom, min, max, parameters);
				maps.add(s);
			}
		}
	}

	public void delete() {
		maps.clear();
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
		return (TreeNode) atlas;
	}

	public boolean isLeaf() {
		return false;
	}

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		this.atlas = (Atlas) parent;
		// TODO: Test loaded data for problems (missing fields, duplicate names)
	}

	/***********************************************************/

	public static class SubMap implements MapInterface, ToolTipProvider, CapabilityDeletable,
			CapabilityRenameable, TreeNode, DownloadableElement {

		private String name;

		private AutoCutMultiMapLayer layer;

		private TileImageParameters parameters = null;

		@XmlAttribute
		private Point maxTileCoordinate = null;

		@XmlAttribute
		private Point minTileCoordinate = null;

		@XmlAttribute
		private MapSource mapSource = null;

		private Dimension tileDimension = null;

		@XmlAttribute
		private int zoom = -1;

		protected SubMap() {
		}

		protected SubMap(AutoCutMultiMapLayer layer, String name, MapSource mapSource, int zoom,
				Point minTileCoordinate, Point maxTileCoordinate, TileImageParameters parameters) {
			this.layer = layer;
			this.maxTileCoordinate = maxTileCoordinate;
			this.minTileCoordinate = minTileCoordinate;
			this.name = name;
			this.mapSource = mapSource;
			this.zoom = zoom;
			this.parameters = parameters;
			calculateRuntimeValues();
		}

		protected void calculateRuntimeValues() {
			if (parameters == null)
				tileDimension = new Dimension(Tile.SIZE, Tile.SIZE);
			else
				tileDimension = new Dimension(parameters.width, parameters.height);
		}

		public LayerInterface getLayer() {
			return layer;
		}

		public MapSource getMapSource() {
			return mapSource;
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
			return zoom;
		}

		@Override
		public String toString() {
			return getName();
		}

		public TileImageParameters getParameters() {
			return parameters;
		}

		public void setParameters(TileImageParameters parameters) {
			this.parameters = parameters;
		}

		public String getToolTip() {
			EastNorthCoordinate tl = new EastNorthCoordinate(zoom, minTileCoordinate.x,
					minTileCoordinate.y);
			EastNorthCoordinate br = new EastNorthCoordinate(zoom, maxTileCoordinate.x,
					maxTileCoordinate.y);

			StringWriter sw = new StringWriter(1024);
			sw.write("<html>");
			sw.write("<b>Map area</b><br>");
			sw.write("Map source: " + mapSource.getName() + "<br>");
			sw.write("Zoom level: " + zoom + "<br>");
			sw.write("Area start: " + tl + " (" + minTileCoordinate.x + " / " + minTileCoordinate.y
					+ ")<br>");
			sw.write("Area end: " + br + " (" + maxTileCoordinate.x + " / " + maxTileCoordinate.y
					+ ")<br>");
			sw.write("Map size: " + (maxTileCoordinate.x - minTileCoordinate.x + 1) + "x"
					+ (maxTileCoordinate.y - minTileCoordinate.y + 1) + " pixel<br>");
			if (parameters != null) {
				sw.write("Tile size: " + parameters.width + "x" + parameters.height + "<br>");
				sw.write("Tile format: " + parameters.format + "<br>");
			} else
				sw.write("Tile size: 256x256 (no processing)<br>");
			sw.write("Maximum tiles to download: " + calculateTilesToDownload() + "<br>");
			sw.write("</html>");
			return sw.toString();
		}

		public Dimension getTileSize() {
			return tileDimension;
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
			return (TreeNode) layer;
		}

		public boolean isLeaf() {
			return true;
		}

		public int calculateTilesToDownload() {
			int width = MyMath.divCeil((maxTileCoordinate.x - minTileCoordinate.x) + 1, Tile.SIZE);
			int height = MyMath.divCeil((maxTileCoordinate.y - minTileCoordinate.y) + 1, Tile.SIZE);
			return width * height;
		}

		public void afterUnmarshal(Unmarshaller u, Object parent) {
			this.layer = (AutoCutMultiMapLayer) parent;
			// TODO: Test loaded data for problems (missing fields, duplicate
			// names, ...)
			if (maxTileCoordinate == null || minTileCoordinate == null || mapSource == null
					|| zoom < 0)
				throw new RuntimeException(
						"Unable to load data from profile file - may be the profile format has changed");
			calculateRuntimeValues();
		}

		public Enumeration<Job> getDownloadJobs(TarIndexedArchive tileArchive,
				DownloadJobListener listener) {
			return new DownloadJobEnumerator(this, tileArchive, listener);
		}
	}

}
