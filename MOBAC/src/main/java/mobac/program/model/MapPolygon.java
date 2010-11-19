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
package mobac.program.model;

import java.awt.Point;
import java.awt.Polygon;
import java.io.StringWriter;
import java.util.Enumeration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.program.JobDispatcher.Job;
import mobac.program.download.jobenumerators.DJEPolygon;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.utilities.tar.TarIndexedArchive;

@XmlRootElement
public class MapPolygon extends Map {

	@XmlElement
	protected Polygon polygon = new Polygon();

	protected MapPolygon() {
	}

	public MapPolygon(Layer layer, String name, MapSource mapSource, int zoom, Point minTileCoordinate,
			Point maxTileCoordinate, TileImageParameters parameters) {
		super(layer, name, mapSource, zoom, minTileCoordinate, maxTileCoordinate, parameters);

		// Example diamond
		int xMid = (maxTileCoordinate.x + minTileCoordinate.x) / 2;
		int yMid = (maxTileCoordinate.y + minTileCoordinate.y) / 2;
		polygon.addPoint(xMid, minTileCoordinate.y);
		polygon.addPoint(maxTileCoordinate.x, yMid);
		polygon.addPoint(xMid, maxTileCoordinate.y);
		polygon.addPoint(minTileCoordinate.x, yMid);
	}

	@Override
	public int calculateTilesToDownload() {
		int tileSize = mapSource.getMapSpace().getTileSize();
		double tileSizeD = tileSize;
		int xMin = minTileCoordinate.x;
		int xMax = maxTileCoordinate.x;
		int yMin = minTileCoordinate.y;
		int yMax = maxTileCoordinate.y;

		int count = 0;
		for (int x = xMin; x <= xMax; x += tileSize) {
			for (int y = yMin; y <= yMax; y += tileSize) {
				if (polygon.intersects(x, y, tileSizeD, tileSizeD))
					count++;
			}
		}
		return count;
	}

	@Override
	public String getToolTip() {
		MapSpace mapSpace = mapSource.getMapSpace();
		EastNorthCoordinate tl = new EastNorthCoordinate(mapSpace, zoom, minTileCoordinate.x, minTileCoordinate.y);
		EastNorthCoordinate br = new EastNorthCoordinate(mapSpace, zoom, maxTileCoordinate.x, maxTileCoordinate.y);

		StringWriter sw = new StringWriter(1024);
		sw.write("<html>");
		sw.write("<b>Polygonal Map</b><br>");
		sw.write("Map source: " + mapSource.getName() + "<br>");
		sw.write("Zoom level: " + zoom + "<br>");
		sw.write("Polygon points: " + polygon.npoints + "<br>");
		sw.write("Area start: " + tl + " (" + minTileCoordinate.x + " / " + minTileCoordinate.y + ")<br>");
		sw.write("Area end: " + br + " (" + maxTileCoordinate.x + " / " + maxTileCoordinate.y + ")<br>");
		sw.write("Map size: " + (maxTileCoordinate.x - minTileCoordinate.x + 1) + "x"
				+ (maxTileCoordinate.y - minTileCoordinate.y + 1) + " pixel<br>");
		if (parameters != null) {
			sw.write("Tile size: " + parameters.getWidth() + "x" + parameters.getHeight() + "<br>");
			sw.write("Tile format: " + parameters.getFormat() + "<br>");
		} else
			sw.write("Tile size: 256x256 (no processing)<br>");
		sw.write("Maximum tiles to download: " + calculateTilesToDownload() + "<br>");
		sw.write("</html>");
		return sw.toString();
	}

	@Override
	public Enumeration<Job> getDownloadJobs(TarIndexedArchive tileArchive, DownloadJobListener listener) {
		return new DJEPolygon(this, tileArchive, listener);
	}

	public Polygon getPolygon() {
		return polygon;
	}

	@Override
	public MapInterface deepClone(LayerInterface newLayer) {
		MapPolygon map = (MapPolygon) super.deepClone(newLayer);
		map.polygon = new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
		return map;
	}

}
