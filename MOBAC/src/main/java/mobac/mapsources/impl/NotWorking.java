package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.MultiLayerMapSource;
import mobac.mapsources.impl.OsmMapSources.Mapnik;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

public class NotWorking {

	protected static final String LAYER_OPENSEA = "http://tiles.openseamap.org/seamark/";
	
	/**
	 * Not working correctly:
	 * 
	 * 1. The map is a "sparse map" (only tiles are present that have content - the other are missing) <br>
	 * 2. The map layer's background is not transparent!
	 */
	public static class OpenSeaMapLayer extends AbstractMapSource {

		public OpenSeaMapLayer(String name) {
			super(name, 11, 17, "png", TileUpdate.LastModified);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return LAYER_OPENSEA + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	/**
	 * Not working correctly!
	 * 
	 *@see OpenSeaMapLayer
	 */
	public static class OpenSeaMap extends OpenSeaMapLayer implements MultiLayerMapSource {

		private MapSource mapnik = new Mapnik();

		public OpenSeaMap() {
			super("OpenSeaMap");
		}

		public MapSource getBackgroundMapSource() {
			return mapnik;
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
			return "http://www.bergfex.at/images/amap/" + zoom + "/" + tileX100 + "/" + zoom + "_" + tilex + "_"
					+ tiley + ".png";
		}

		@Override
		public String toString() {
			return getName() + " (Austria only)";
		}

	}

	public static class StatKartNo extends AbstractMapSource {

		String token = "58C7907E4A6308544E93B6E4458742D323B111CD6CDD9"
				+ "EBAD2551A496FE8CAE24093F9D3AA862E6BDB31F96A23D20030D" + "DA6B1D212552D6802ED3328E0BB1926";

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
					+ "REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&" + "SRS=EPSG%3A32633&BBOX="
					+ lon1 + "," + lat1 + "," + lon2 + "," + lat2
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
			return "http://tile.openaerialmap.org/tiles/1.0.0/openaerialmap-900913/" + zoom + "/" + tilex + "/" + tiley
					+ ".jpg";
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
			String s = "http://tilesserver.mapa.es/tilesserver/" + "n=topografico-mtn_" + mtn + ";z=30;r=" + r + ";i="
					+ i + ";j=" + j + ".jpg";
			System.out.println(tilex + " " + tiley + " z=" + zoom + "\n\t" + s);
			return s;
		}

		@Override
		public boolean allowFileStore() {
			return false;
		}

	}

}
