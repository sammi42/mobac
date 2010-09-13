/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.mapsources.impl;

import java.awt.Color;

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MultiLayerMapSource;
import mobac.mapsources.mapspace.MapSpaceFactory;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

public class OsmMapSources {

	protected static final String MAP_MAPNIK = "http://tile.openstreetmap.org";
	protected static final String MAP_OSMA = "http://tah.openstreetmap.org/Tiles/tile";
	public static final String MAP_HIKING_TRAILS = "http://topo.openstreetmap.de/topo/";
	public static final String MAP_HIKING_BASE = "http://topo.openstreetmap.de/base/";
	public static final String MAP_HIKING_RELIEF = "http://hills-nc.openstreetmap.de/";
	protected static final String MAP_PISTE = "http://tiles.openpistemap.org/contours/";

	protected static abstract class AbstractOsmTileSource extends AbstractMapSource {

		public AbstractOsmTileSource(String name) {
			super(name, 0, 18, "png");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		public String getTileType() {
			return "png";
		}

		public boolean allowFileStore() {
			return true;
		}
	}

	public static class Mapnik extends AbstractOsmTileSource {

		public Mapnik() {
			super("Mapnik");
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_MAPNIK + super.getTileUrl(zoom, tilex, tiley);
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfNoneMatch;
		}

		@Override
		public String toString() {
			return "OpenStreetMap Mapnik";
		}

	}

	public static class CycleMap extends AbstractOsmTileSource {

		private static final String PATTERN = "http://%s.andy.sandbox.cloudmade.com/tiles/cycle/%d/%d/%d.png";

		private static final String[] SERVER = { "a", "b", "c" };

		private int SERVER_NUM = 0;

		public CycleMap() {
			super("OSM Cycle Map");
			this.maxZoom = 17;
			this.tileUpdate = TileUpdate.ETag;
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			String url = String.format(PATTERN, new Object[] { SERVER[SERVER_NUM], zoom, tilex, tiley });
			SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
			return url;
		}

		@Override
		public String toString() {
			return "OpenStreetMap Cyclemap";
		}

	}

	public static class OsmPublicTransport extends AbstractOsmTileSource {

		private static final String PATTERN = "http://tile.xn--pnvkarte-m4a.de/tilegen/%d/%d/%d.png";

		public OsmPublicTransport() {
			super("OSMPublicTransport");
			this.maxZoom = 16;
			this.minZoom = 2;
			this.tileUpdate = TileUpdate.ETag;
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			String url = String.format(PATTERN, new Object[] { zoom, tilex, tiley });
			return url;
		}

		@Override
		public String toString() {
			return "OpenStreetMap Public Transport";
		}

	}

	public static class TilesAtHome extends AbstractOsmTileSource {

		public TilesAtHome() {
			super("TilesAtHome");
			this.maxZoom = 17;
			this.tileUpdate = TileUpdate.IfModifiedSince;
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_OSMA + super.getTileUrl(zoom, tilex, tiley);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Osmarenderer";
		}

	}

	public static class OsmHikingMap extends AbstractMapSource {

		public OsmHikingMap() {
			super("OSM Hiking", 4, 18, "png", TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking (Germany only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_TRAILS + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	public static class OsmHikingRelief extends AbstractMapSource {

		public OsmHikingRelief() {
			super("OSM Hiking Relief", 4, 15, "png", TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking Relief only (Germany only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_RELIEF + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	public static class OsmHikingBase extends AbstractMapSource {

		public OsmHikingBase() {
			super("OSM Hiking Base", 4, 18, "png", TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking Base only (Germany only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_BASE + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	public static class OsmHikingMapWithRelief extends OsmHikingMap implements MultiLayerMapSource {

		private MapSource background = new OsmHikingRelief();

		@Override
		public String toString() {
			return "OpenStreetMap Hiking with Relief";
		}

		@Override
		public String getName() {
			return "OSM Hiking with Relief";
		}

		@Override
		public int getMaxZoom() {
			return 15;
		}

		public MapSource getBackgroundMapSource() {
			return background;
		}

		@Override
		public Color getBackgroundColor() {
			return Color.WHITE;
		}

	}

	public static class OsmHikingMapWithBase extends OsmHikingMap implements MultiLayerMapSource {

		private MapSource background = new OsmHikingBase();

		@Override
		public String toString() {
			return "OpenStreetMap Hiking with Base";
		}

		@Override
		public String getName() {
			return "OSM Hiking with Base";
		}

		public MapSource getBackgroundMapSource() {
			return background;
		}
	}

	public static class OpenPisteMap extends AbstractMapSource {

		public OpenPisteMap() {
			super("OpenPisteMap", 0, 17, "png", TileUpdate.LastModified);
		}

		@Override
		public String toString() {
			return "Open Piste Map";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_PISTE + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	/**
	 * http://hikebikemap.de/
	 */
	public static class HikebikemapBase extends AbstractMapSource {

		public HikebikemapBase() {
			super("HikebikemapTiles", 0, 18, "png", TileUpdate.None);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hikebikemap Map";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://toolserver.org/tiles/hikebike/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	/**
	 * Hill shades / relief
	 * 
	 * http://hikebikemap.de/
	 */
	public static class HikebikemapRelief extends AbstractMapSource {

		public HikebikemapRelief() {
			super("HikebikemapRelief", 0, 16, "png", TileUpdate.None);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://toolserver.org/~cmarqu/hill/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	public static class Hikebikemap extends HikebikemapRelief implements MultiLayerMapSource {

		private final MapSource BASE = new HikebikemapBase();

		public MapSource getBackgroundMapSource() {
			return BASE;
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hikebikemap.de";
		}

	}

	/**
	 * Uses 512x512 tiles - not fully supported at the moment!
	 */
	public static class Turaterkep extends AbstractMapSource {

		private static MapSpace space = MapSpaceFactory.getInstance(512, true);

		public Turaterkep() {
			super("Turaterkep", 7, 16, "png", TileUpdate.IfNoneMatch);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://turaterkep.hostcity.hu/tiles/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		@Override
		public MapSpace getMapSpace() {
			return space;
		}

		@Override
		public String toString() {
			return "Turaterkep (Hungary)";
		}

		@Override
		public Color getBackgroundColor() {
			return Color.WHITE;
		}

	}

}
