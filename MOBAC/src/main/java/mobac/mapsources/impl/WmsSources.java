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

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;

public class WmsSources {

	public static class TerraserverUSA extends AbstractMapSource {

		public TerraserverUSA() {
			super("Terraserver-USA", 3, 17, TileImageType.JPG);
		}

		@Override
		public String toString() {
			return "Terraserver-USA Map (USA only)";
		}

		public MapSpace getMapSpace() {
			return MercatorPower2MapSpace.INSTANCE_256;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			double[] coords = MapSourceTools.calculateLatLon(this, zoom, tilex, tiley);
			String url = "http://terraserver-usa.com/ogcmap6.ashx?"
					+ "version=1.1.1&request=GetMap&Layers=DRG&Styles=&SRS=EPSG:4326&" + "BBOX=" + coords[0] + ","
					+ coords[1] + "," + coords[2] + "," + coords[3]
					+ "&width=256&height=256&format=image/jpeg&EXCEPTIONS=BLANK";
			return url;
		}
	}

	/**
	 * DOES NOT WORK!!!
	 */
	public static class OsmWms extends AbstractMapSource {
		public OsmWms() {
			super("OSM-WMS", 0, 19, TileImageType.PNG);
		}

		@Override
		public String toString() {
			return "OSM-WMS (Europe)";
		}

		public MapSpace getMapSpace() {
			return MercatorPower2MapSpace.INSTANCE_256;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			long[] coords = tileToMeters(this, zoom, tilex, tiley);
			String bBox = String.format("BBOX=%d.%d.%d.%d", coords[0], coords[1], coords[2], coords[3]);
			String url = "http://openls.giub.uni-bonn.de/ors-tilecache/tilecache.py?"
					+ "LAYERS=ors-osm&SRS=EPSG%3A900913&FORMAT=image%2Fpng&NUMZOOMLEVELS=19&"
					+ "SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&"
					+ "EXCEPTIONS=application%2Fvnd.ogc.se_inimage&" + bBox + "&WIDTH=256&HEIGHT=256";
			System.out.println(url);
			return url;
		}
	}

	static final double ORIGIN_SHIFT = Math.PI * 6378137; // 20037508.3427892430765884088807

	public static long[] tileToMeters(MapSource mapSource, int zoom, int px, int py) {
		// "Converts pixel coordinates in given zoom level of pyramid to EPSG:900913"

		// 2 * math.pi * 6378137 / self.tileSize
		double res = 2 * Math.PI * 6378137 / (2 << zoom);

		System.out.println(res);

		double mx = px * res - ORIGIN_SHIFT;
		double mx2 = (px + 1) * res - ORIGIN_SHIFT;
		double my = py * res - ORIGIN_SHIFT;
		double my2 = (py + 1) * res - ORIGIN_SHIFT;

		return new long[] { (long) mx, (long) my, (long) mx2, (long) my2 };
	}

}
