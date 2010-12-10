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
package mobac.mapsources.mappacks.region_america_north;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;

public class TerraserverUSA extends AbstractHttpMapSource {

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