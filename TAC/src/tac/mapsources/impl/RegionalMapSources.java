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

}
