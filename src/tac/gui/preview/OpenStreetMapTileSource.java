package tac.gui.preview;

import org.openstreetmap.gui.jmapviewer.OsmTileSource;

public class OpenStreetMapTileSource {
	public static class Mapnik extends OsmTileSource.Mapnik {

		@Override
		public String toString() {
			return "OpenStreetMap Mapnik";
		}
		
	}

	public static class TilesAtHome extends OsmTileSource.TilesAtHome {
		@Override
		public String toString() {
			return "OpenStreetMap Osmrenderer";
		}
	}

	public static class CycleMap extends OsmTileSource.CycleMap {
		@Override
		public String toString() {
			return "OpenStreetMap Cyclemap";
		}
	}
}
