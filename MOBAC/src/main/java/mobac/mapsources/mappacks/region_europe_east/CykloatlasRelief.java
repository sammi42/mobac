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

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;

import mobac.exceptions.TileException;
import mobac.exceptions.UnrecoverableDownloadException;
import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.Logging;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.TileStoreEntry;

/**
 * Relief only
 */
public class CykloatlasRelief extends AbstractHttpMapSource {

	public CykloatlasRelief() {
		super("CykloatlasRelief", 7, 15, TileImageType.PNG, HttpMapSource.TileUpdate.LastModified);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		return "http://services.tmapserver.cz/tiles/gm/sum/" + zoom + "/" + tilex + "/" + tiley + ".png";
	}

	@Override
	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, InterruptedException,
			TileException {
		byte[] data = super.getTileData(zoom, x, y, loadMethod);
		if (data != null && data.length == 0) {
			return null;
		}
		return data;
	}

	@Override
	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		try {
			return super.getTileImage(zoom, x, y, loadMethod);
		} catch (FileNotFoundException e) {
			TileStore ts = TileStore.getInstance();
			long time = System.currentTimeMillis();
			// We set the tile data to an empty array because we can not store null
			TileStoreEntry entry = ts.createNewEntry(x, y, zoom, new byte[] {}, time, time + (1000 * 60 * 60 * 60), "");
			ts.putTile(entry, this);
		} catch (Exception e) {
			Logging.LOG.error("Unknown error in " + this.getClass().getName(), e);
		}
		return null;
	}

	@Override
	protected void prepareTileUrlConnection(HttpURLConnection conn) {
		super.prepareTileUrlConnection(conn);
		conn.addRequestProperty("Referer", "http://www.cykloserver.cz/cykloatlas/");
	}
}
