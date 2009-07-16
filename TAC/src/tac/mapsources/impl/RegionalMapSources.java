package tac.mapsources.impl;

import tac.mapsources.AbstractMapSource;

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
	public static class DoCeluPL extends AbstractMapSource {

		public DoCeluPL() {
			super("docelu.pl", 2, 16, "png");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String sx = String.format("%06x", tilex);
			String sy = String.format("%06x", tiley);
			char[] cx = sx.toCharArray();
			char[] cy = sy.toCharArray();
			String szoom = Integer.toHexString(zoom);

			String s = "http://i.wp.pl/m/tiles004/" + szoom + "/" + cx[4] + cy[4] + "/" + cx[3]
					+ cy[3] + "/" + cx[2] + cy[2] + "/" + cx[1] + cy[1] + "/" + cx[0] + cy[0]
					+ "/z" + szoom + "x" + sx + "y" + sy + ".png";
			return s;
		}

		@Override
		public String toString() {
			return getName() + " (Poland only)";
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
			super("UMP-pcPL", 0, 18, "png");
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

	public static class OutdooractiveCom extends AbstractMapSource {

		private static int SERVER_NUM = 0;

		public OutdooractiveCom() {
			super("Outdooractive.com", 8, 17, "png");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			if (zoom < 8)
				throw new RuntimeException("Zoom level not suported");
			String s = "http://t" + SERVER_NUM + ".outdooractive.com/portal/map/" + zoom + "/"
					+ tilex + "/" + tiley + ".png";
			SERVER_NUM = (SERVER_NUM + 1) % 4;
			return s;
		}

		@Override
		public String toString() {
			return getName() + " (Germany only)";
		}

	}

	/**
	 * CykloServer http://www.cykloserver.cz/cykloatlas/index.php
	 */
	public static class Cykloatlas extends AbstractMapSource {

		public Cykloatlas() {
			super("Cykloatlas", 7, 15, "png");
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

	public static class Doculeo extends AbstractMapSource {

		public Doculeo() {
			super("Doculeo (Poland)", 7, 16, "jpg");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {

			// http://ed-pl-maps.osl.basefarm.net/tiles/maps/en_FI/6/35/21.png"
			return "http://i.wp.pl/m/tiles004/c/%d/%d/00/00/00/zcx00089dy000558.png";
		}

	}

	/**
	 * 
	 * Requires known user agent, and something else otherwise we get only a HTTP 403 
	 * <p> map provider does not work --> currently unused</p>
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

			String s= "http://www.turistickamapa.sk/tiles/sr50/" + zoom + "/" + sx + "/" + sy + ".png";
			System.out.println(s);
			return s;
		}

	}

}
