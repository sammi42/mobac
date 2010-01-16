package tac.program.download.jobenumerators;

import java.awt.Polygon;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.JobDispatcher.Job;
import tac.program.download.DownloadJob;
import tac.program.interfaces.DownloadJobListener;
import tac.program.model.MapPolygon;
import tac.utilities.tar.TarIndexedArchive;

/**
 * Enumerates / creates the download jobs for a single layer map with a
 * polygonal selection.
 */
public class DJEPolygon extends DJERectangle {

	protected final int tileSize;
	protected Polygon polygon;

	public DJEPolygon(MapPolygon map, TarIndexedArchive tileArchive, DownloadJobListener listener) {
		this(map, map.getMapSource(), 0, tileArchive, listener);
	}

	protected DJEPolygon(MapPolygon map, MapSource mapSource, int layer,
			TarIndexedArchive tileArchive, DownloadJobListener listener) {
		super(map, mapSource, layer, tileArchive, listener);
		this.polygon = map.getPolygon();
		tileSize = mapSource.getMapSpace().getTileSize();
		x--;
		nextElement();
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
