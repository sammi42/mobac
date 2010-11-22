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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.MemoryCacheImageInputStream;

import mobac.exceptions.UnrecoverableDownloadException;
import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.AbstractMultiLayerMapSource;
import mobac.mapsources.mapspace.MapSpaceFactory;
import mobac.program.Logging;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.MapSource.LoadMethod;
import mobac.program.model.TileImageType;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.TileStoreEntry;
import mobac.utilities.Utilities;
import mobac.utilities.imageio.PngConstants;

public class OsmMapSources {

	public static final String MAP_MAPNIK = "http://tile.openstreetmap.org";
	public static final String MAP_OSMA = "http://tah.openstreetmap.org/Tiles/tile";
	public static final String MAP_HIKING_TRAILS = "http://www.wanderreitkarte.de/topo/";
	public static final String MAP_HIKING_BASE = "http://www.wanderreitkarte.de/base/";
	public static final String MAP_HIKING_RELIEF = "http://www.wanderreitkarte.de/hills/";
	public static final String MAP_PISTE = "http://tiles.openpistemap.org/contours/";
	public static final String LAYER_OPENSEA = "http://tiles.openseamap.org/seamark/";

	protected static abstract class AbstractOsmTileSource extends AbstractHttpMapSource {

		public AbstractOsmTileSource(String name) {
			super(name, 0, 18, TileImageType.PNG);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		public TileImageType getTileImageType() {
			return TileImageType.PNG;
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

		public HttpMapSource.TileUpdate getTileUpdate() {
			return HttpMapSource.TileUpdate.IfNoneMatch;
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
			this.tileUpdate = HttpMapSource.TileUpdate.ETag;
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
			this.tileUpdate = HttpMapSource.TileUpdate.ETag;
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
			this.tileUpdate = HttpMapSource.TileUpdate.IfModifiedSince;
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

	public static class OsmHikingMap extends AbstractHttpMapSource {

		public OsmHikingMap() {
			super("OSM Hiking", 4, 18, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking (Germany only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_TRAILS + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	public static class OsmHikingRelief extends AbstractHttpMapSource {

		public OsmHikingRelief() {
			super("OSM Hiking Relief", 4, 15, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking Relief only (Germany only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_RELIEF + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		@Override
		public Color getBackgroundColor() {
			return Color.WHITE;
		}

	}

	public static class OsmHikingBase extends AbstractHttpMapSource {

		public OsmHikingBase() {
			super("OSM Hiking Base", 4, 18, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking Base only (Germany only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_BASE + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	public static class OsmHikingMapWithReliefBase extends AbstractMultiLayerMapSource {

		public OsmHikingMapWithReliefBase() {
			super("OSM Hiking with Relief and Base", TileImageType.PNG);
			mapSources = new MapSource[] { new OsmHikingBase(), new OsmHikingRelief(), new OsmHikingMap() };
			initializeValues();
		}

		@Override
		public Color getBackgroundColor() {
			return Color.WHITE;
		}

	}

	public static class OsmHikingMapWithRelief extends AbstractMultiLayerMapSource {

		public OsmHikingMapWithRelief() {
			super("OSM Hiking with Relief", TileImageType.PNG);
			mapSources = new MapSource[] { new OsmHikingMap(), new OsmHikingRelief() };
			initializeValues();
		}

		@Override
		public Color getBackgroundColor() {
			return Color.WHITE;
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking with Relief";
		}

	}

	public static class OsmHikingMapWithBase extends AbstractMultiLayerMapSource {

		public OsmHikingMapWithBase() {
			super("OSM Hiking with Base", TileImageType.PNG);
			mapSources = new MapSource[] { new OsmHikingBase(), new OsmHikingMap() };
			initializeValues();
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking with Base";
		}

	}

	public static class OpenPisteMap extends AbstractHttpMapSource {

		public OpenPisteMap() {
			super("OpenPisteMap", 0, 17, TileImageType.PNG, HttpMapSource.TileUpdate.LastModified);
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
	public static class HikebikemapBase extends AbstractHttpMapSource {

		public HikebikemapBase() {
			super("HikebikemapTiles", 0, 18, TileImageType.PNG, HttpMapSource.TileUpdate.None);
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
	public static class HikebikemapRelief extends AbstractHttpMapSource {

		public HikebikemapRelief() {
			super("HikebikemapRelief", 0, 16, TileImageType.PNG, HttpMapSource.TileUpdate.None);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://toolserver.org/~cmarqu/hill/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	public static class Hikebikemap extends AbstractMultiLayerMapSource {

		public Hikebikemap() {
			super("OpenStreetMap Hikebikemap.de", TileImageType.PNG);
			mapSources = new MapSource[] { new HikebikemapBase(), new HikebikemapRelief() };
			initializeValues();
		}

	}

	/**
	 * Not working correctly:
	 * 
	 * 1. The map is a "sparse map" (only tiles are present that have content - the other are missing) <br>
	 * 2. The map layer's background is not transparent!
	 */
	public static class OpenSeaMapLayer extends AbstractHttpMapSource {

		public OpenSeaMapLayer() {
			super("OpenSeaMap", 11, 17, TileImageType.PNG, TileUpdate.LastModified);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return LAYER_OPENSEA + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		@Override
		public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, UnrecoverableDownloadException,
				InterruptedException {
			byte[] data = super.getTileData(zoom, x, y, loadMethod);
			if (data != null && data.length == 0)
				// Non-existent tile loaded from tile store
				return null;
			return data;
		}

		@Override
		public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, UnrecoverableDownloadException,
				InterruptedException {
			try {
				ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
				byte[] data = getTileData(zoom, x, y, LoadMethod.DEFAULT);
				if (data == null)
					return null;
				reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(data)), false, false);
				BufferedImage image = reader.read(0);
				System.out.println(getTileUrl(zoom, x, y) + " " + image.getColorModel().getClass());
				if (image.getTransparency() == Transparency.OPAQUE
						&& image.getColorModel() instanceof ComponentColorModel) {

					Color transparencyColor = null;
					IIOMetadata meta = reader.getImageMetadata(0);
					if (meta instanceof com.sun.imageio.plugins.png.PNGMetadata) {
						com.sun.imageio.plugins.png.PNGMetadata pngmeta;
						pngmeta = (com.sun.imageio.plugins.png.PNGMetadata) meta;
						if (!pngmeta.tRNS_present)
							return image;
						if (pngmeta.tRNS_colorType == PngConstants.COLOR_TRUECOLOR)
							transparencyColor = new Color(pngmeta.tRNS_red, pngmeta.tRNS_green, pngmeta.tRNS_blue);
						else if (pngmeta.tRNS_colorType == PngConstants.COLOR_GRAYSCALE) {
							// transparencyColor = new Color(pngmeta.tRNS_gray, pngmeta.tRNS_gray, pngmeta.tRNS_gray);

							// For an unknown reason the saved transparency color does not match the background color -
							// therefore at the moment we use a hard coded transparency color
							transparencyColor = new Color(0xfcfcfc);
						}

					} else
						transparencyColor = new Color(248, 248, 248);
					Image correctedImage = Utilities.makeColorTransparent(image, transparencyColor);
					BufferedImage image2 = new BufferedImage(image.getWidth(), image.getHeight(),
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = image2.createGraphics();
					try {
						g.drawImage(correctedImage, 0, 0, null);
						// g.setColor(Color.RED);
						// g.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
						image = image2;
					} finally {
						g.dispose();
					}
				}
				return image;
			} catch (FileNotFoundException e) {
				TileStore ts = TileStore.getInstance();
				long time = System.currentTimeMillis();
				TileStoreEntry entry = ts.createNewEntry(x, y, zoom, new byte[] {}, time, time + (1000 * 60 * 60 * 60),
						"");
				ts.putTile(entry, this);
			} catch (Exception e) {
				Logging.LOG.error("Unknown error in OpenSeaMap", e);
			}
			return null;
		}
	}

	/**
	 *@see OpenSeaMapLayer
	 */
	public static class OpenSeaMap extends AbstractMultiLayerMapSource {

		public OpenSeaMap() {
			super("OpenSeaMap", TileImageType.PNG);
			mapSources = new MapSource[] { new Mapnik(), new OpenSeaMapLayer() };
			initializeValues();
		}

	}

	/**
	 * Uses 512x512 tiles - not fully supported at the moment!
	 */
	public static class Turaterkep extends AbstractHttpMapSource {

		private static MapSpace space = MapSpaceFactory.getInstance(512, true);

		public Turaterkep() {
			super("Turaterkep", 7, 16, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
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
