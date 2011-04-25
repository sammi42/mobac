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
package mobac.gui.mapview.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import mobac.data.gpx.GPXUtils;
import mobac.data.gpx.gpx11.Gpx;
import mobac.data.gpx.gpx11.WptType;
import mobac.gui.mapview.JMapViewer;
import mobac.gui.mapview.interfaces.MapLayer;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.MapSpace;
import mobac.utilities.GUIExceptionHandler;

/**
 * Displays a polygon on the map - only for testing purposes
 */
public class ShapeLayer implements MapLayer {

	private Color color = new Color(0f, 1f, 0f, 0.5f);

	private int calculationZoom;
	private Shape shape;;

	public ShapeLayer() {
		try {
			Gpx gpx = GPXUtils.loadGpxFile(new File(
					"C:/Privat/Eclipse Workspace/MOBAC/misc/samples/gpx/gpx11 track.gpx"));
			List<WptType> points = gpx.getTrk().get(0).getTrkseg().get(0).getTrkpt();

			System.err.println("Points in track: " + points.size());

			int defZoom = 16;

			Area area = new Area();

			MapSpace mapSpace = MercatorPower2MapSpace.INSTANCE_256;
			for (int i = 1; i < points.size(); i++) {
				WptType point1 = points.get(i - 1);
				WptType point2 = points.get(i);

				int y1 = mapSpace.cLatToY(point1.getLat().doubleValue(), defZoom);
				int y2 = mapSpace.cLatToY(point2.getLat().doubleValue(), defZoom);
				int x1 = mapSpace.cLonToX(point1.getLon().doubleValue(), defZoom);
				int x2 = mapSpace.cLonToX(point2.getLon().doubleValue(), defZoom);

				System.err.println("Points " + x1 + "/" + y1 + "  " + x2 + "/" + y2);

				Line2D.Double ln = new Line2D.Double(x1, y1, x2, y2);
				double indent = defZoom * 15.0; // distance from central line
				double length = ln.getP1().distance(ln.getP2());

				double dx_li = (ln.getX2() - ln.getX1()) / length * indent;
				double dy_li = (ln.getY2() - ln.getY1()) / length * indent;

				// moved p1 point
				double p1X = ln.getX1() - dx_li;
				double p1Y = ln.getY1() - dy_li;

				// line moved to the left
				double lX1 = ln.getX1() - dy_li;
				double lY1 = ln.getY1() + dx_li;
				double lX2 = ln.getX2() - dy_li;
				double lY2 = ln.getY2() + dx_li;

				// moved p2 point
				double p2X = ln.getX2() + dx_li;
				double p2Y = ln.getY2() + dy_li;

				// line moved to the right
				double rX1_ = ln.getX1() + dy_li;
				double rY1 = ln.getY1() - dx_li;
				double rX2 = ln.getX2() + dy_li;
				double rY2 = ln.getY2() - dx_li;

				Path2D p = new Path2D.Double();
				p.moveTo(lX1, lY1);
				p.lineTo(lX2, lY2);
				p.lineTo(p2X, p2Y);
				p.lineTo(rX2, rY2);
				p.lineTo(rX1_, rY1);
				p.lineTo(p1X, p1Y);
				p.lineTo(lX1, lY1);

				area.add(new Area(p));
			}

			this.calculationZoom = defZoom;

			shape = area;
		} catch (JAXBException e) {
			GUIExceptionHandler.showExceptionDialog(e);
			shape = new Area();
		}

	}

	public ShapeLayer(Shape shape, int zoom) {
		this.shape = shape;
	}

	public void paint(JMapViewer map, Graphics2D g, int zoom, int minX, int minY, int maxX, int maxY) {
		AffineTransform af = g.getTransform();
		g.translate(-minX, -minY);
		double scale;
		if (zoom < calculationZoom)
			scale = 1d / (1 << (calculationZoom - zoom));
		else
			scale = 1 << (zoom - calculationZoom);
		g.scale(scale, scale);
		g.setColor(color);
		g.fill(shape);
		g.setTransform(af);
	}

}
