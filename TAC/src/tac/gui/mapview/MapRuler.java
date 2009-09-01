package tac.gui.mapview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

/**
 * Simple map ruler showing the map scale in kilometers.
 */
public class MapRuler {

	public static final double EARTH_RADIUS_METER = 6367500;
	public static final double EARTH_RADIUS_MILES = 3956.6;

	public static int[] MAP_ROULER_LENGTH = { // 
	10000000, 5000000, 5000000, 2000000, 500000, // z0-z4
			200000, 200000, 200000, 100000, // z5-8
			50000, 20000, 10000, 5000, // z9-12
			2000, 1000, 500, 200, // z13-16
			100, 50, 20, 10, // z18-22
			5, 2 };

	public static void paintMapRuler(Graphics g, MapSpace mapSpace, Point tlc, int zoom) {
		Rectangle r = g.getClipBounds();
		int posX;
		int posY = r.height - r.y;
		posY -= 50; // 50 pixel from bottom border
		posX = 50;

		int coordX = tlc.x + posX;
		int coordY = tlc.y + posY;

		int len = MAP_ROULER_LENGTH[zoom];

		int w = mapSpace.moveOnLatitude(coordX, coordY, zoom, (double) len / EARTH_RADIUS_METER);

		if (w > 150) {
			while (w > 150) {
				w >>= 1;
				len >>= 1;
			}
		} else {
			while (w < 80) {
				w <<= 1;
				len <<= 1;
			}
		}
		g.setColor(Color.YELLOW);
		g.fillRect(posX, posY - 10, w, 20);
		g.setColor(Color.BLACK);
		g.drawRect(posX, posY - 10, w, 20);
		String unit = " m";
		if (len > 1000) {
			len /= 1000;
			unit = " km";
		}
		g.drawString(Integer.toString(len) + unit, posX + 10, posY + 4);
	}

}
