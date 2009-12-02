package tac.mapsources.impl;

import tac.mapsources.AbstractMapSource;
import tac.mapsources.MapSourceTools;
import tac.mapsources.UpdatableMapSource;

/**
 * Map sources that do not cover the whole world
 */
public class RegionalMapSources {

	/**
	 * Mapa Polski, Europy i Świata - DoCelu.pl (added by "maniek-ols")
	 * <p>
	 * <a href="docelu.pl">docelu.pl</a>
	 * </p>
	 */
	public static class DoCeluPL extends AbstractMapSource implements UpdatableMapSource {

		private String baseUrl;

		public DoCeluPL() {
			super("DoCeluPL", 2, 16, "png", TileUpdate.LastModified);
			update();
		}

		public void update() {
			baseUrl = MapSourceTools.loadMapUrl(this, "baseurl");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String sx = String.format("%06x", tilex);
			String sy = String.format("%06x", tiley);
			char[] cx = sx.toCharArray();
			char[] cy = sy.toCharArray();
			String szoom = Integer.toHexString(zoom);

			String s = baseUrl + szoom + "/" + cx[4] + cy[4] + "/" + cx[3] + cy[3] + "/" + cx[2]
					+ cy[2] + "/" + cx[1] + cy[1] + "/" + cx[0] + cy[0] + "/z" + szoom + "x" + sx
					+ "y" + sy + ".png";
			return s;
		}

		@Override
		public String toString() {
			return "Docelu.pl (Poland only)";
		}

	}

	/**
	 * Darmowa Mapa Polski dla GPS Garmin - UMP-pcPL (added by "maniek-ols")
	 * <p>
	 * <a href="http://ump.waw.pl">ump.waw.pl</a>
	 * </p>
	 */
	public static class UmpWawPl extends AbstractMapSource {

		private static int SERVER_NUM = 0;
		private static final int MAX_SERVER_NUM = 4;

		public UmpWawPl() {
			super("UMP-pcPL", 0, 18, "png", TileUpdate.LastModified);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String s = "http://" + SERVER_NUM + ".tiles.ump.waw.pl/ump_tiles/" + zoom + "/" + tilex
					+ "/" + tiley + ".png";
			SERVER_NUM = (SERVER_NUM + 1) % MAX_SERVER_NUM;
			return s;
		}

		@Override
		public String toString() {
			return getName() + " (Poland only)";
		}

	}

	public static class OutdooractiveGermany extends AbstractMapSource {

		private static int SERVER_NUM = 0;

		protected String mapName = "portal";

		public OutdooractiveGermany() {
			super("Outdooractive.com", 8, 17, "png", TileUpdate.LastModified);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			if (zoom < 8)
				throw new RuntimeException("Zoom level not suported");
			String s = "http://t" + SERVER_NUM + ".outdooractive.com/" + mapName + "/map/" + zoom
					+ "/" + tilex + "/" + tiley + ".png";
			SERVER_NUM = (SERVER_NUM + 1) % 4;
			return s;
		}

		@Override
		public String toString() {
			return "Outdooractive.com (Germany only)";
		}

	}

	public static class OutdooractiveSouthTyrol extends OutdooractiveGermany {

		public OutdooractiveSouthTyrol() {
			super();
			name = "OutdooractiveSouthTyrol";
			mapName = "suedtirol";
			minZoom = 9;
		}

		@Override
		public String toString() {
			return "Outdooractive.com (South Tyrol only)";
		}

	}

	public static class OutdooractiveAustria extends OutdooractiveGermany {

		public OutdooractiveAustria() {
			super();
			name = "OutdooractiveAustria";
			mapName = "austria";
			minZoom = 9;
		}

		@Override
		public String toString() {
			return "Outdooractive.com (Austria only)";
		}

	}

	/**
	 * CykloServer http://www.cykloserver.cz/cykloatlas/index.php
	 */
	public static class Cykloatlas extends AbstractMapSource {

		public Cykloatlas() {
			super("Cykloatlas", 7, 15, "png", TileUpdate.LastModified);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String z = Integer.toString(zoom);
			if (zoom >= 13)
				z += "c";
			return "http://services.tmapserver.cz/tiles/gm/shc/" + z + "/" + tilex + "/" + tiley
					+ ".png";
		}

		@Override
		public String toString() {
			return getName() + " (Czech Republic only)";
		}

	}

	/**
	 * 
	 * Requires known user agent, and something else otherwise we get only a
	 * HTTP 403
	 * <p>
	 * map provider does not work --> currently unused
	 * </p>
	 */
	public static class TuristikaMapSk extends AbstractMapSource {

		public TuristikaMapSk() {
			super("TuristikaMapSk (Slovakia)", 12, 15, "png");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String sx = String.format("%09d", tilex);
			String sy = String.format("%09d", tiley);
			sx = sx.substring(0, 3) + "/" + sx.substring(3, 6) + "/" + sx.substring(6, 9);
			sy = sy.substring(0, 3) + "/" + sy.substring(3, 6) + "/" + sy.substring(6, 9);

			String s = "http://www.turistickamapa.sk/tiles/sr50/" + zoom + "/" + sx + "/" + sy
					+ ".png";
			System.out.println(s);
			return s;
		}

	}

	/**
	 * AustrianMap
	 * <p>
	 * <a href="http://www.austrianmap.at">www.austrianmap.at</a>
	 * </p>
	 */
	public static class AustrianMap extends AbstractMapSource {

		public AustrianMap() {
			super("AustrianMap", 14, 15, "png");
			tileUpdate = TileUpdate.ETag;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			int tileX100 = tilex / 100;
			return "http://www.bergfex.at/images/amap/" + zoom + "/" + tileX100 + "/" + zoom + "_"
					+ tilex + "_" + tiley + ".png";
		}

		@Override
		public String toString() {
			return getName() + " (Austria only)";
		}

	}

	/**
	 * @author SourceForge.net user didoa.
	 */
	public static class FreemapSlovakia extends AbstractMapSource {

		public FreemapSlovakia() {
			super("FreemapSlovakia", 5, 16, "png", TileUpdate.ETag);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://www.freemap.sk/layers/allinone/?/BNp/" + zoom + "/" + tilex + "/"
					+ tiley;
		}

		@Override
		public String toString() {
			return "Freemap Slovakia (Atlas)";
		}

	}

	/**
	 * @author SourceForge.net user didoa.
	 */
	public static class FreemapSlovakiaHikingHillShade extends AbstractMapSource {

		public FreemapSlovakiaHikingHillShade() {
			super("FreemapSlovakiaHikingHillShade", 6, 16, "png", TileUpdate.ETag);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://www.freemap.sk/layers/allinone/?/BVRNTp/" + zoom + "/" + tilex + "/"
					+ tiley;
		}

		@Override
		public String toString() {
			return "Freemap Slovakia Hiking (with HillShade)";
		}
	}

	/**
	 * @author SourceForge.net user didoa.
	 */
	public static class FreemapSlovakiaHiking extends AbstractMapSource {

		public FreemapSlovakiaHiking() {
			super("FreemapSlovakiaHiking", 6, 16, "png", TileUpdate.ETag);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://www.freemap.sk/layers/allinone/?/BNTNp/" + zoom + "/" + tilex + "/"
					+ tiley;
		}

		@Override
		public String toString() {
			return "Freemap Slovakia Hiking";
		}
	}

	/**
	 * Mapa Polski i Europy Emapa.pl (added by "Velociraptor")
	 * <p>
	 * <a href="mapa.emapa.pl">mapa.emapa.pl</a>
	 * </p>
	 */
	public static class EmapaPl extends AbstractMapSource {

		public EmapaPl() {
			super("EmapaPl", 0, 19, "png", TileUpdate.None);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://mapa.emapa.pl/mapsrc/img.aspx?&x=" + tilex + "&y=" + tiley + "&zoom="
					+ zoom;
		}

		@Override
		public String toString() {
			return "Emapa.pl (Poland only)";
		}
	}

	public static class NearMap extends AbstractMapSource {
		public NearMap() {
			super("NearMap Australia", 0, 21, "jpg", TileUpdate.IfNoneMatch);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://www.nearmap.com/maps/hl=en&nml=Vert&z=" + zoom + "&x=" + tilex + "&y="
					+ tiley;
		}

	}

	/**
	 * Hubermedia Bavaria map http://maps.hubermedia.de/
	 */
	public static class HubermediaBavaria extends AbstractMapSource {

		String[] mapUrls;

		int serverNum = 0;

		public HubermediaBavaria() {
			super("Hubermedia Bavaria", 10, 16, "png", TileUpdate.IfNoneMatch);
			mapUrls = new String[17];

			mapUrls[10] = "http://t0.hubermedia.de/TK500/DE/Bayern/";
			mapUrls[11] = mapUrls[10];
			mapUrls[12] = "http://t{$servernum}.wms.hubermedia.de/tk200/de/bayern//Z{$z}/{$y}/{$x}.png";
			mapUrls[13] = "http://t{$servernum}.hubermedia.de/TK50/DE/Bayern//Z{$z}/{$y}/{$x}.png";
			mapUrls[14] = mapUrls[13];
			mapUrls[15] = "http://t{$servernum}.hubermedia.de/TK25/DE/Bayern//Z{$z}/{$y}/{$x}.png";
			mapUrls[16] = "http://t{$servernum}.hubermedia.de/DOK/DE/Bayern//Z{$z}/{$y}/{$x}.png";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			serverNum = (serverNum + 1) % 3;
			if (zoom >= 12) {
				return MapSourceTools.formatMapUrl(mapUrls[zoom], serverNum, zoom, tilex, tiley);
			} else {
				String tc = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
				return mapUrls[zoom] + zoom + "/" + tc + ".png";
			}
		}
	}

	/**
	 * 
	 * http://www.statkart.no/
	 * 
	 * <p>
	 * There is a limit of 10 000 cache-tiler per end user (unique IP address)
	 * per day. This restriction is therefore not associated with the individual
	 * application.
	 * </p>
	 * 
	 * <table border="1">
	 * <tr>
	 * <th>Service Name</th>
	 * <th>Underlying WMS service</th>
	 * <th>Teams from WMS</th>
	 * <th>Maximum zoom level</th>
	 * </tr>
	 * <tr>
	 * <td>kartdata2</td>
	 * <td>Kartdata2 WMS</td>
	 * <td>all</td>
	 * <td>12</td>
	 * </tr>
	 * <tr>
	 * <td>sjo_hovedkart2</td>
	 * <td>See chart master map series 2 WMS</td>
	 * <td>all</td>
	 * <td>17</td>
	 * </tr>
	 * <tr>
	 * <td>topo2</td>
	 * <td>Norway Topographic map 2 WMS</td>
	 * <td>all</td>
	 * <td>17
	 * <tr>
	 * <td>topo2graatone</td>
	 * <td>Norway Topographic map 2 grayscale WMS</td>
	 * <td>all</td>
	 * <td>17</td>
	 * </tr>
	 * <tr>
	 * <td>toporaster2</td>
	 * <td>Topographic raster map 2 WMS</td>
	 * <td>all</td>
	 * <td>17</td>
	 * </tr>
	 * <tr>
	 * <td>Europe</td>
	 * <td>Europe Map WMS</td>
	 * <td>all</td>
	 * <td>17</td>
	 * </tr>
	 * </table> {@link http
	 * ://www.statkart.no/?module=Articles;action=Article.publicShow;ID=14165}
	 */
	public static class StatkartTopo2 extends AbstractMapSource {

		final String service;

		public StatkartTopo2() {
			this("topo2", "Statkart Topo 2", 0, 17, "png", TileUpdate.None);
		}

		public StatkartTopo2(String service, String name, int minZoom, int maxZoom,
				String tileType, TileUpdate tileUpdate) {
			super(name, minZoom, maxZoom, tileType, tileUpdate);
			this.service = service;
		}

		@Override
		public String toString() {
			return getName() + " (Norway)";
		}

		@Override
		protected String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://opencache.statkart.no/gatekeeper/gk/gk.open_gmaps?layers=" + service
					+ "&zoom=" + zoom + "&x=" + tilex + "&y=" + tiley;
		}

	}

	public static class StatkartToporaster2 extends StatkartTopo2 {

		public StatkartToporaster2() {
			super("toporaster2", "Statkart Toporaster 2", 0, 17, "png", TileUpdate.None);
		}

	}

}
