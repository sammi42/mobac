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
package mobac.mapsources.mappacks.region_europe_north;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

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
public class StatkartTopo2 extends AbstractHttpMapSource {

	final String service;

	public StatkartTopo2() {
		this("topo2", "Statkart Topo 2", 0, 17, TileImageType.PNG, HttpMapSource.TileUpdate.None);
	}

	public StatkartTopo2(String service, String name, int minZoom, int maxZoom, TileImageType tileType,
			HttpMapSource.TileUpdate tileUpdate) {
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
