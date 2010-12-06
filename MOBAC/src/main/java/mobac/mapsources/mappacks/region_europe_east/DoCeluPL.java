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
package mobac.mapsources.mappacks.region_europe_east;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.UpdatableMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

/**
 * Mapa Polski, Europy i Świata - DoCelu.pl (added by "maniek-ols")
 * <p>
 * <a href="docelu.pl">docelu.pl</a>
 * </p>
 */
public class DoCeluPL extends AbstractHttpMapSource implements UpdatableMapSource {

	private String baseUrl;

	public DoCeluPL() {
		super("DoCeluPL", 2, 16, TileImageType.PNG, HttpMapSource.TileUpdate.LastModified);
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
