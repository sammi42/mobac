package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


public class NotWorking {

	/**
	 * Yandex uses an unknown projection and and different ellipsoid. See
	 * http://itranga.blogspot.com/2009/03/alert111.html for details.
	 */
	public static class YandexMap extends AbstractMapSource {
		// YandexMap.url=http://vec0{$servernum}.maps.yandex.ru/tiles?l=map&v=2.10.2&x={$x}&y={$y}&z={$z}

		int SERVER_NUM = 1;

		String urlPattern;

		public YandexMap() {
			super("YandexMap", 1, 17, "png", TileUpdate.IfModifiedSince);
			urlPattern = MapSourceTools.loadMapUrl(this, "url");
		}

		@Override
		public MapSpace getMapSpace() {
			return null; //unknown what to return
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			SERVER_NUM = (SERVER_NUM % 3) + 3;
			String tmp = urlPattern;
			tmp = tmp.replace("{$servernum}", Integer.toString(SERVER_NUM));
			tmp = tmp.replace("{$x}", Integer.toString(tilex));
			tmp = tmp.replace("{$y}", Integer.toString(tiley));
			tmp = tmp.replace("{$z}", Integer.toString(zoom));
			return tmp;
		}

		@Override
		public String toString() {
			return "Yandex Map (Russia)";
		}

	}

	/**
	 * Yandex uses an unknown projection and and different ellipsoid. See
	 * http://itranga.blogspot.com/2009/03/alert111.html for details.
	 */
	public static class YandexSat extends AbstractMapSource {
		// YandexSat.url=http://sat0{$servernum}.maps.yandex.ru/tiles?l=sat&v=1.13.0&x={$x}&y={$y}&z={$z}

		int SERVER_NUM = 1;

		String urlPattern;

		public YandexSat() {
			super("YandexSat", 1, 18, "jpg", TileUpdate.IfModifiedSince);
			urlPattern = MapSourceTools.loadMapUrl(this, "url");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			SERVER_NUM = (SERVER_NUM % 3) + 3;
			String tmp = urlPattern;
			tmp = tmp.replace("{$servernum}", Integer.toString(SERVER_NUM));
			tmp = tmp.replace("{$x}", Integer.toString(tilex));
			tmp = tmp.replace("{$y}", Integer.toString(tiley));
			tmp = tmp.replace("{$z}", Integer.toString(zoom));
			return tmp;
		}

		@Override
		public MapSpace getMapSpace() {
			return null; //unknown what to return
		}

		@Override
		public String toString() {
			return "Yandex Sat (Russia)";
		}

	}

	public static class StatKartNo extends AbstractMapSource {

		String token = "58C7907E4A6308544E93B6E4458742D323B111CD6CDD9"
				+ "EBAD2551A496FE8CAE24093F9D3AA862E6BDB31F96A23D20030D"
				+ "DA6B1D212552D6802ED3328E0BB1926";

		public StatKartNo() {
			super("StatKartNo", 0, 17, "png");
		}

		public MapSpace getMapSpace() {
			return MercatorPower2MapSpace.INSTANCE_256;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			double[] coords = MapSourceTools.calculateLatLon(this, zoom, tilex, tiley);
			int lon1 = (int) (coords[0] * 10000);
			int lat1 = (int) (coords[1] * 10000);
			int lon2 = (int) (coords[2] * 10000);
			int lat2 = (int) (coords[3] * 10000);
			String url = "http://gatekeeper1.geonorge.no/BaatGatekeeper/gk/gk.cache?gkt=" + token
					+ "&LAYERS=topo2&FORMAT=image%2Fpng&SERVICE=WMS&VERSION=1.1.1&"
					+ "REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&"
					+ "SRS=EPSG%3A32633&BBOX=" + lon1 + "," + lat1 + "," + lon2 + "," + lat2
					// 186336,6706272,272992,6792928"
					+ "&WIDTH=256&HEIGHT=256";

			return url;
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

	public static class OpenArialMap extends AbstractMapSource {

		public OpenArialMap() {
			super("OpenArialMap", 0, 18, "jpg");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://tile.openaerialmap.org/tiles/1.0.0/openaerialmap-900913/" + zoom + "/"
					+ tilex + "/" + tiley + ".jpg";
		}

	}

	public static class SigpacEs extends AbstractMapSource {

		public SigpacEs() {
			super("sigpac.mapa.es", 0, 18, "jpg");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			int r = 1000 * (1 << (14 - zoom));
			int i = tilex;
			int j = tiley;
			// http://tilesserver.mapa.es/tilesserver/n=topografico-mtn_1250;z=30;r=256000;i=8;j=61.jpg
			int mtn = 1250;
			String s = "http://tilesserver.mapa.es/tilesserver/" + "n=topografico-mtn_" + mtn
					+ ";z=30;r=" + r + ";i=" + i + ";j=" + j + ".jpg";
			System.out.println(tilex + " " + tiley + " z=" + zoom + "\n\t" + s);
			return s;
		}

		@Override
		public boolean allowFileStore() {
			return false;
		}

	}

}
