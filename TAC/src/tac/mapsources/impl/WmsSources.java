package tac.mapsources.impl;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.mapsources.AbstractMapSource;
import tac.mapsources.MapSourceTools;
import tac.mapsources.mapspace.MercatorPower2MapSpace;

public class WmsSources {

	public static class TerraserverUSA extends AbstractMapSource {

		public TerraserverUSA() {
			super("Terraserver-USA", 3, 17, "jpg");
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
					+ "version=1.1.1&request=GetMap&Layers=DRG&Styles=&SRS=EPSG:4326&" + "BBOX="
					+ coords[0] + "," + coords[1] + "," + coords[2] + "," + coords[3]
					+ "&width=256&height=256&format=image/jpeg&EXCEPTIONS=BLANK";
			return url;
		}
	}

}
