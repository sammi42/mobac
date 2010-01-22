package mobac.program.download.jobenumerators;

import mobac.program.interfaces.DownloadJobEnumerator;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.Map;
import mobac.program.model.MapPolygon;
import mobac.utilities.tar.TarIndexedArchive;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


public class DJEFactory {

	protected static DownloadJobEnumerator createInstance(MapInterface map, MapSource mapSource,
			int layer, TarIndexedArchive tileArchive, DownloadJobListener listener) {
		if (map instanceof Map)
			return new DJERectangle((Map) map, mapSource, layer, tileArchive, listener);
		if (map instanceof MapPolygon)
			return new DJEPolygon((MapPolygon) map, mapSource, layer,
					tileArchive, listener);
		throw new RuntimeException("Unsupported map type" + map.getClass());
	}
}
