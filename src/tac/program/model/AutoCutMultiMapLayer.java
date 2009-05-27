package tac.program.model;

import java.awt.Dimension;
import java.awt.Point;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

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
import tac.tar.TarIndexedArchive;

//import tac.utilities.MyMath;

/**
 * A layer holding one or multiple maps of the same map source and the same zoom
 * level. The number of maps depends on the size of the covered area - if it is
 * smaller than the specified <code>maxMapSize</code> then there will be only
 * one map.
 * 
 */
public class AutoCutMultiMapLayer implements LayerInterface, DownloadableElement, ToolTipProvider,
		CapabilityDeletable, CapabilityRenameable, Iterable<MapInterface> {

	private AtlasInterface atlas;

	private String name;
	private MapSource mapSource;
	private Point maxTileCoordinate;
	private Point minTileCoordinate;
	private Point maxTileNum;
	private Point minTileNum;
	private int zoom;
	private TileImageParameters parameters;
	private Dimension tileDimension;
	private Dimension maxMapDimension;

	private LinkedList<MapInterface> maps = new LinkedList<MapInterface>();

	public AutoCutMultiMapLayer(Atlas atlas, String name, MapSource mapSource,
			Point minTileCoordinate, Point maxTileCoordinate, int zoom,
			TileImageParameters parameters, int maxMapSize) {
		this.atlas = atlas;
		this.name = name;
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
			SubMap s = new SubMap(0, minTileCoordinate, maxTileCoordinate);
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
				Point min = new Point(mapX, mapY);
				int maxX = Math.min(mapX + maxMapDimension.width, maxTileCoordinate.x);
				int maxY = Math.min(mapY + maxMapDimension.height, maxTileCoordinate.y);
				Point max = new Point(maxX, maxY);
				SubMap s = new SubMap(mapCounter++, min, max);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	@Override
	public String toString() {
		return name;
	}

	public long calculateTilesToDownload() {
		long width = maxTileNum.x - minTileNum.x + 1;
		long height = maxTileNum.y - minTileNum.y + 1;
		return width * height;
	}

	public String getToolTip() {
		StringWriter sw = new StringWriter(1024);
		sw.write("<html>");
		sw.write("<b>Layer</b><br>");
		sw.write("Map count: " + maps.size() + "<br>");
		sw.write("Map source: " + mapSource.getName() + "<br>");
		sw.write("Area: " + "<br>");
		sw.write("Zoom level: " + zoom + "<br>");
		if (parameters != null) {
			sw.write("Tile size: " + parameters.width + "x" + parameters.height + "<br>");
			sw.write("Tile format: " + parameters.format + "<br>");
		} else
			sw.write("Tile size: 256x256 (no processing)<br>");

		sw.write("Max map size: " + maxMapDimension.width + "x" + maxMapDimension.height + "<br>");
		sw.write("</html>");
		return sw.toString();
	}

	public class SubMap implements MapInterface, ToolTipProvider {

		private String name;
		private Point maxTileCoordinate;
		private Point minTileCoordinate;

		protected SubMap(int mapNum, Point minTileCoordinate, Point maxTileCoordinate) {
			super();
			this.maxTileCoordinate = maxTileCoordinate;
			this.minTileCoordinate = minTileCoordinate;
			name = String
					.format("%s-%02d", new Object[] { AutoCutMultiMapLayer.this.name, mapNum });
		}

		public LayerInterface getLayer() {
			return AutoCutMultiMapLayer.this;
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

		public String getToolTip() {
			return "<html><b>Map area</b><br>Area start: " + minTileCoordinate.x + " / "
					+ minTileCoordinate.y + "<br>Area end:" + maxTileCoordinate.x + " / "
					+ maxTileCoordinate.y + "</html>";
		}

		public Dimension getTileSize() {
			return tileDimension;
		}
	}

	public Iterator<MapInterface> iterator() {
		return maps.iterator();
	}

}
