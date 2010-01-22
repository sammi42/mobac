package mobac.program.model;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Enumeration;

import mobac.mapsources.MultiLayerMapSource;
import mobac.program.JobDispatcher.Job;
import mobac.program.download.jobenumerators.DJEMultiLayer;
import mobac.program.download.jobenumerators.DJEPolygon;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.tar.TarIndexedArchive;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


public class MapPolygon extends Map {

	protected Polygon polygon = new Polygon();

	protected MapPolygon() {
	}

	public MapPolygon(Layer layer, String name, MapSource mapSource, int zoom,
			Point minTileCoordinate, Point maxTileCoordinate, TileImageParameters parameters) {
		super(layer, name, mapSource, zoom, minTileCoordinate, maxTileCoordinate, parameters);

		// Example diamond
		int xMid = (maxTileCoordinate.x + minTileCoordinate.x) / 2;
		int yMid = (maxTileCoordinate.y + minTileCoordinate.y) / 2;
		polygon.addPoint(xMid, minTileCoordinate.y);
		polygon.addPoint(maxTileCoordinate.x, yMid);
		polygon.addPoint(xMid, maxTileCoordinate.y);
		polygon.addPoint(minTileCoordinate.x, yMid);
	}

	@Override
	public int calculateTilesToDownload() {
		int tileSize = mapSource.getMapSpace().getTileSize();
		double tileSizeD = tileSize;
		int xMin = minTileCoordinate.x;
		int xMax = maxTileCoordinate.x;
		int yMin = minTileCoordinate.y;
		int yMax = maxTileCoordinate.y;

		int count = 0;
		for (int x = xMin; x <= xMax; x += tileSize) {
			for (int y = yMin; y <= yMax; y += tileSize) {
				if (polygon.intersects(x, y, tileSizeD, tileSizeD))
					count++;
			}
		}
		return count;
	}

	@Override
	public String getToolTip() {
		return super.getToolTip() + "\nMap shape: polygonal";
	}

	@Override
	public Enumeration<Job> getDownloadJobs(TarIndexedArchive tileArchive,
			DownloadJobListener listener) {
		if (mapSource instanceof MultiLayerMapSource)
			return new DJEMultiLayer(this, tileArchive, listener);
		else
			return new DJEPolygon(this, tileArchive, listener);
	}

	public Polygon getPolygon() {
		return polygon;
	}

	@Override
	public MapInterface deepClone(LayerInterface newLayer) {
		MapPolygon map = (MapPolygon) super.deepClone(newLayer);
		map.polygon = new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
		return map;
	}

}
