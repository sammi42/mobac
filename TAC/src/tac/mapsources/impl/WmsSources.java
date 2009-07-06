package tac.mapsources.impl;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class WmsSources {

	static Logger log = Logger.getLogger(WmsSources.class);

	public static abstract class WmsMapSource implements MapSource {

		/**
		 * 
		 * @param zoom
		 * @param tilex
		 * @param tiley
		 * @return <code>double[] {lon_min , lat_min , lon_max , lat_max}</code>
		 */
		protected double[] calculateLatLon(int zoom, int tilex, int tiley) {
			double[] result = new double[4];
			tilex *= Tile.SIZE;
			tiley *= Tile.SIZE;
			result[0] = OsmMercator.XToLon(tilex, zoom); // lon_min
			result[1] = OsmMercator.YToLat(tiley + Tile.SIZE, zoom); // lat_max
			result[2] = OsmMercator.XToLon(tilex + Tile.SIZE, zoom); // lon_min
			result[3] = OsmMercator.YToLat(tiley, zoom); // lat_max
			return result;
		}

		public boolean allowFileStore() {
			return true;
		}
		
	}

	public static class TerraserverUSA extends WmsMapSource {

		public int getMaxZoom() {
			return 17;
		}

		public int getMinZoom() {
			return 3;
		}

		public String getName() {
			return "Terraserver-USA";
		}

		@Override
		public String toString() {
			return "Terraserver-USA Map (USA only)";
		}

		public String getTileType() {
			return "jpg";
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.None;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			double[] coords = calculateLatLon(zoom, tilex, tiley);
			String url = "http://terraserver-usa.com/ogcmap6.ashx?"
					+ "version=1.1.1&request=GetMap&Layers=DRG&Styles=&SRS=EPSG:4326&" + "BBOX="
					+ coords[0] + "," + coords[1] + "," + coords[2] + "," + coords[3]
					+ "&width=256&height=256&format=image/jpeg&EXCEPTIONS=BLANK";

			log.debug(url);
			return url;
		}
	}

}
