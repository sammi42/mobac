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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Random;

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.MultiLayerMapSource;
import mobac.mapsources.UpdatableMapSource;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

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

			String s = baseUrl + szoom + "/" + cx[4] + cy[4] + "/" + cx[3] + cy[3] + "/" + cx[2] + cy[2] + "/" + cx[1]
					+ cy[1] + "/" + cx[0] + cy[0] + "/z" + szoom + "x" + sx + "y" + sy + ".png";
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
			String s = "http://" + SERVER_NUM + ".tiles.ump.waw.pl/ump_tiles/" + zoom + "/" + tilex + "/" + tiley
					+ ".png";
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
			String s = "http://t" + SERVER_NUM + ".outdooractive.com/" + mapName + "/map/" + zoom + "/" + tilex + "/"
					+ tiley + ".png";
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
			return "http://services.tmapserver.cz/tiles/gm/shc/" + z + "/" + tilex + "/" + tiley + ".png";
		}

		@Override
		public String toString() {
			return getName() + " (CZ, SK)";
		}

	}

	/**
	 * Relief only
	 */
	public static class CykloatlasRelief extends AbstractMapSource {

		public CykloatlasRelief() {
			super("CykloatlasRelief", 7, 15, "png", TileUpdate.LastModified);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://services.tmapserver.cz/tiles/gm/sum/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

	}

	public static class CykloatlasWithRelief extends CykloatlasRelief implements MultiLayerMapSource {

		private MapSource background = new Cykloatlas();

		public MapSource getBackgroundMapSource() {
			return background;
		}

		@Override
		public String toString() {
			return "Cykloatlas with relief (CZ, SK)";
		}
	}

	/**
	 * 
	 * Requires known user agent, and something else otherwise we get only a HTTP 403
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

			String s = "http://www.turistickamapa.sk/tiles/sr50/" + zoom + "/" + sx + "/" + sy + ".png";
			System.out.println(s);
			return s;
		}

	}

	/**
	 * http://www.freemap.sk
	 * 
	 * @author SourceForge.net user didoa, nickn17
	 */
	public static class FreemapSlovakia extends AbstractMapSource {

		public FreemapSlovakia() {
			super("FreemapSlovakia", 5, 16, "png", TileUpdate.None);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://tiles.freemap.sk/A/" + zoom + "/" + tilex + "/" + tiley;
		}

		@Override
		public String toString() {
			return "Freemap Slovakia Atlas";
		}

	}

	/**
	 * http://www.freemap.sk
	 * 
	 * @author SourceForge.net user didoa, nickn17
	 */
	public static class FreemapSlovakiaHiking extends AbstractMapSource {

		public FreemapSlovakiaHiking() {
			super("FreemapSlovakiaHiking", 6, 16, "png", TileUpdate.None);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://tiles.freemap.sk/T/" + zoom + "/" + tilex + "/" + tiley;
		}

		@Override
		public String toString() {
			return "Freemap Slovakia Hiking";
		}
	}

	/**
	 * http://www.freemap.sk
	 */
	public static class FreemapSlovakiaCycling extends AbstractMapSource {

		public FreemapSlovakiaCycling() {
			super("FreemapSlovakiaCyclo", 6, 16, "png", TileUpdate.None);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://tiles.freemap.sk/C/" + zoom + "/" + tilex + "/" + tiley;
		}

		@Override
		public String toString() {
			return "Freemap Slovakia Cycling";
		}
	}

	/**
	 * Emapi - mapa internetowa firmy Emapa
	 * <p>
	 * <a href="http://emapi.pl/">emapi.pl</a>
	 * </p>
	 */
	public static class EmapiPl extends AbstractMapSource {

		int[] servernums = { 1, 2, 3, 4 };

		int selectedServer = 0;

		public EmapiPl() {
			super("EmapiPl", 0, 19, "png", TileUpdate.None);
		}

		public String getTileUrl(int zoom, int x, int y) {
			selectedServer = (selectedServer++) % servernums.length;
			return "http://img" + servernums[selectedServer] + ".emapi.pl/Default.aspx?tileX=" + x + "&tileY=" + y
					+ "&zoom=" + zoom + "&layer=std&fun=GetMap&userID=pasat";

		}

		@Override
		public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley) throws IOException {
			HttpURLConnection conn = super.getTileUrlConnection(zoom, tilex, tiley);
			conn.addRequestProperty("Cookie", "currentView=");
			conn.addRequestProperty("Referer", "http://emapi.pl/?referer=");
			return conn;
		}

		@Override
		public String toString() {
			return "Emapi.pl (Poland only)";
		}
	}

	public static class NearMap extends AbstractMapSource {
		public NearMap() {
			super("NearMap Australia", 0, 24, "jpg", TileUpdate.IfModifiedSince);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://www.nearmap.com/maps/hl=en&nml=Vert&z=" + zoom + "&x=" + tilex + "&y=" + tiley;
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
	 * There is a limit of 10 000 cache-tiler per end user (unique IP address) per day. This restriction is therefore
	 * not associated with the individual application.
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
	 * <td>europa</td>
	 * <td>Europe Map WMS</td>
	 * <td>all</td>
	 * <td>17</td>
	 * </tr>
	 * </table>
	 * 
	 * <pre>
	 * http://www.statkart.no/?module=Articles;action=Article.publicShow;ID=14165
	 * </pre>
	 */
	public static class StatkartTopo2 extends AbstractMapSource {

		final String service;

		public StatkartTopo2() {
			this("topo2", "Statkart Topo 2", 0, 17, "png", TileUpdate.None);
		}

		public StatkartTopo2(String service, String name, int minZoom, int maxZoom, String tileType,
				TileUpdate tileUpdate) {
			super(name, minZoom, maxZoom, tileType, tileUpdate);
			this.service = service;
		}

		@Override
		public String toString() {
			return getName() + " (Norway)";
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://opencache.statkart.no/gatekeeper/gk/gk.open_gmaps?layers=" + service + "&zoom=" + zoom
					+ "&x=" + tilex + "&y=" + tiley;
		}

	}

	public static class StatkartToporaster2 extends StatkartTopo2 {

		public StatkartToporaster2() {
			super("toporaster2", "Statkart Toporaster 2", 0, 17, "png", TileUpdate.None);
		}

	}

	public static class StatkartSjoHovedkart2 extends StatkartTopo2 {

		public StatkartSjoHovedkart2() {
			super("sjo_hovedkart2", "Statkart sea/nautical", 0, 17, "png", TileUpdate.None);
		}

	}

	public static class EniroComMap extends AbstractMapSource {

		private String mapType;

		public EniroComMap() {
			this("map");
		}

		protected EniroComMap(String mapType) {
			super("Eniro.com-" + mapType, 0, 20, "png", TileUpdate.IfModifiedSince);
			this.mapType = mapType;
		}

		@Override
		public String toString() {
			return "Eniro Map (SE, NO, FI)";
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			int y = (1 << zoom) - 1 - tiley;
			return "http://map.eniro.com/geowebcache/service/tms1.0.0/" + mapType + "/" + zoom + "/" + tilex + "/" + y
					+ ".png";
		}
	}

	public static class EniroComNautical extends EniroComMap {

		public EniroComNautical() {
			super("nautical");
		}

		@Override
		public String toString() {
			return "Eniro Nautical (SE, NO, FI)";
		}

	}

	public static class EniroComAerial extends EniroComMap {

		public EniroComAerial() {
			super("aerial");
		}

		@Override
		public String toString() {
			return "Eniro Aerial (SE, NO, FI)";
		}

	}

	/**
	 * http://www.mapplus.ch/
	 */
	public static class MapplusCh extends AbstractMapSource {

		String referer;

		private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
				'f' };

		public MapplusCh() {
			super("MapplusCh", 7, 16, "jpg", TileUpdate.ETag);
			char[] sessID = new char[32];
			Random rnd = new Random();
			for (int i = 0; i < sessID.length; i++)
				sessID[i] = hex[rnd.nextInt(hex.length)];
			// example sessID = "12ea56827487e927d4b202ad48248109";
			referer = "http://www.mapplus.ch/NeapoljsMapPage.php?uid=public&group=public&sessID=" + new String(sessID);
		}

		@Override
		public String toString() {
			return "Map+ (Switzerland)";
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			int z = 17 - zoom;
			return "http://mp2.mapplus.ch/kacache/" + z + "/def/def/t" + tiley + "/l" + tilex + "/t" + tiley + "l"
					+ tilex + ".jpg";
		}

		@Override
		public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley) throws IOException {
			HttpURLConnection conn = super.getTileUrlConnection(zoom, tilex, tiley);
			// http request property "Referer" is required -
			// otherwise we only get "tranparentpixel.gif"
			conn.setRequestProperty("Referer", referer);
			return conn;
		}

	}

	/**
	 * 
	 * 
	 *
	 */
	public static class Bergfex extends AbstractMapSource {

		/**
		 * 2009-02-20: server 4 causes some problems - commented out
		 */
		static final byte[] SERVER_IDS = { 2, 3 /* , 4 */};

		int SERVERNUM = 0;

		public Bergfex() {
			super("Bergfex", 8, 15, "png", TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "Bergfex (Austria)";
		}

		@Override
		public String getTileUrl(int zoom, int x, int y) {
			String baseurl = "http://static" + SERVER_IDS[SERVERNUM] + ".bergfex.at/images/amap/";
			SERVERNUM = (SERVERNUM + 1) % SERVER_IDS.length;
			String xBase = "";
			if (zoom > 13)
				xBase = Integer.toString(x).substring(0, zoom - 12) + "/";
			return baseurl + zoom + "/" + xBase + zoom + "_" + x + "_" + y + ".png";
		}
	}

	/**
	 * 
	 * http://www.mytopo.com/maps/
	 * 
	 * Funny: The URL indicates PNG images but the server provides JPEG files...
	 * 
	 */
	public static class MyTopo extends AbstractMapSource {

		public MyTopo() {
			super("MyTopo", 6, 16, "jpg", TileUpdate.None);
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "http://maps.mytopo.com/mytopoK55Zc3L/tilecache.py/1.0.0/topoG/" + zoom + "/" + tilex + "/" + tiley
					+ ".jpg";
		}

		@Override
		public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley) throws IOException {
			HttpURLConnection conn = super.getTileUrlConnection(zoom, tilex, tiley);
			conn.addRequestProperty("Referer", "http://www.mytopo.com/maps/");
			return conn;
		}

		@Override
		public String toString() {
			return "MyTopo (USA only)";
		}

	}

	/**
	 * Aero charts from USA http://www.runwayfinder.com
	 * 
	 */
	public abstract static class AeroCharts extends AbstractMapSource {

		private String baseUrl = "http://www.runwayfinder.com/media/";
		protected String service;

		public AeroCharts(String name, String service, int minZoom, int maxZoom) {
			super(name, minZoom, maxZoom, "jpg", TileUpdate.LastModified);
			this.service = service;
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return baseUrl + service + "x=" + tilex + "&y=" + tiley + "&z=" + (17 - zoom);
		}

		@Override
		public String toString() {
			return getName() + " (USA only)";
		}

	}

	public static class AeroChartsVFR extends AeroCharts {

		public AeroChartsVFR() {
			super("Aero VFR Charts", "charts/?", 4, 11);
		}
	}

	public static class AeroChartsIFR extends AeroCharts {

		public AeroChartsIFR() {
			super("Aero IFR Charts", "ifrcharts/?", 4, 11);
		}
	}

	public static class AeroChartsIFRH extends AeroCharts {

		public AeroChartsIFRH() {
			super("Aero IFR-H Charts", "ifrhicharts/?", 4, 9);
		}
	}

	/**
	 * https://sourceforge.net/tracker/?func=detail&aid=3071972&group_id=238075&atid=1105496
	 */
	public static class Sigpac extends AbstractMapSource {

		private static String sources[] = { "", "", "", "", "", "MTNSIGPAC", "MTNSIGPAC", "MTN2000", "MTN2000",
				"MTN2000", "MTN2000", "MTN200", "MTN200", "MTN200", "MTN25", "MTN25", "ORTOFOTOS", "ORTOFOTOS" };

		public Sigpac() {
			// In some places ORTOFOTOS reaches zoom 18,
			// but only level 17 covers the entire country
			super("SIGPAC", 5, 17, "jpg");
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			int j = (1 << zoom) - tiley - 1;

			// The tiles are downloaded from kmlserver interface,
			// as tilesserver.mapa.es serves only UTM projections
			return "http://sigpac.mapa.es/kmlserver/raster/" + sources[zoom] + "@3785/" + zoom + "." + tilex + "." + j
					+ ".img";
		}

		@Override
		public String toString() {
			return "SIGPAC Mercator (Spain only)";
		}
	}

}
