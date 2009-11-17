package tac.mapsources.impl;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.mapsources.AbstractMapSource;
import tac.mapsources.mapspace.MercatorPower2MapSpace;

public class NotWorking {

	public static class StatKartNo extends WmsSources.WmsMapSource {

		String token = "58C7907E4A6308544E93B6E4458742D323B111CD6CDD9"
				+ "EBAD2551A496FE8CAE24093F9D3AA862E6BDB31F96A23D20030D"
				+ "DA6B1D212552D6802ED3328E0BB1926";

		public MapSpace getMapSpace() {
			return MercatorPower2MapSpace.INSTANCE_256;
		}

		public int getMaxZoom() {
			return 17;
		}

		public int getMinZoom() {
			return 0;
		}

		public String getName() {
			return "StatKartNo";
		}

		public String getTileType() {
			return "png";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.None;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			double[] coords = calculateLatLon(zoom, tilex, tiley);
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
			WmsSources.log.debug(url);

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

}
