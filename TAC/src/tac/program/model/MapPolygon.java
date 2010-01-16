package tac.program.model;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Enumeration;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.mapsources.MultiLayerMapSource;
import tac.program.JobDispatcher.Job;
import tac.program.download.jobenumerators.DJEMultiLayer;
import tac.program.download.jobenumerators.DJEPolygon;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;
import tac.utilities.tar.TarIndexedArchive;

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
