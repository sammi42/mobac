package tac.program.download.jobenumerators;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.interfaces.DownloadJobEnumerator;
import tac.program.interfaces.DownloadJobListener;
import tac.program.interfaces.MapInterface;
import tac.program.model.Map;
import tac.program.model.MapPolygon;
import tac.utilities.tar.TarIndexedArchive;

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
