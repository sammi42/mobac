package tac.program.download;

import java.awt.Polygon;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.JobDispatcher.Job;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.MapInterface;
import tac.utilities.tar.TarIndexedArchive;

public class DownloadJobEnumeratorPolygon extends DownloadJobEnumerator {

	protected final int tileSize;
	protected final int tileSizeHalf;
	protected Polygon polygon = new Polygon();

	public DownloadJobEnumeratorPolygon(MapInterface map, MapSource mapSource, int layer,
			TarIndexedArchive tileArchive, DownloadJobListener listener) {
		super(map, mapSource, layer, tileArchive, listener);
		tileSize = mapSource.getMapSpace().getTileSize();
		tileSizeHalf = tileSize / 2;
	}

	public DownloadJobEnumeratorPolygon(MapInterface map, TarIndexedArchive tileArchive,
			DownloadJobListener listener) {
		super(map, tileArchive, listener);
		tileSize = mapSource.getMapSpace().getTileSize();
		tileSizeHalf = tileSize / 2;
		// Example triangle
		int xMid = (xMax + xMin) / 2;
		polygon.addPoint(xMin * tileSize, y * tileSize);
		polygon.addPoint(xMax * tileSize + tileSize - 1, y * tileSize);
		polygon.addPoint(xMid * tileSize, yMax * tileSize + tileSize - 1);
	}

	public Polygon getPolygon() {
		return polygon;
	}

	@Override
	public Job nextElement() {
		Job job = nextJob;
		boolean intersects = false;
		do {
			x++;
			if (x > xMax) {
				y++;
				x = xMin;
				if (y > yMax) {
					nextJob = null;
					return job;
				}
			}
			int tileCoordinateX = x * tileSize;
			int tileCoordinateY = y * tileSize;
			intersects = polygon.intersects(tileCoordinateX, tileCoordinateY, tileSize, tileSize);
			// System.out.println(String.format("x=%5d y=%5d %s",
			// tileCoordinateX, tileCoordinateY,
			// Boolean.toString(intersects)));
		} while (!intersects);
		nextJob = new DownloadJob(mapSource, layer, x, y, zoom, tileArchive, listener);
		return job;
	}
}
