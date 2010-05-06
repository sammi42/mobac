package mobac.program.download.jobenumerators;

import java.awt.Polygon;

import mobac.program.JobDispatcher.Job;
import mobac.program.download.DownloadJob;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.model.MapPolygon;
import mobac.utilities.tar.TarIndexedArchive;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


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
