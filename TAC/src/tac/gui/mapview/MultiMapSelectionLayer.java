package tac.gui.mapview;

import java.awt.Graphics2D;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapLayer;

import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.AtlasObject;
import tac.program.interfaces.LayerInterface;
import tac.program.interfaces.MapInterface;

public class MultiMapSelectionLayer implements MapLayer {

	private AtlasObject object;

	public MultiMapSelectionLayer(AtlasObject object) {
		this.object = object;
	}

	public void paint(JMapViewer mapViewer, Graphics2D g, int zoom, int minX, int minY, int maxX,
			int maxY) {
		if (object instanceof AtlasInterface) {
			for (LayerInterface layer : (AtlasInterface) object) {
				for (MapInterface map : layer) {
					paintMap(map, g, zoom, minX, minY, maxX, maxY);
				}
			}
		} else if (object instanceof LayerInterface) {
			for (MapInterface map : (LayerInterface) object) {
				paintMap(map, g, zoom, minX, minY, maxX, maxY);
			}
		} else {
			paintMap((MapInterface) object, g, zoom, minX, minY, maxX, maxY);
		}
	}

	protected void paintMap(MapInterface map, Graphics2D g, int zoom, int minX, int minY, int maxX,
			int maxY) {
		Point max = map.getMaxTileCoordinate();
		Point min = map.getMinTileCoordinate();
		int zoomDiff = map.getZoom() - zoom;
		int mapX = min.x;
		int mapY = min.y;
		int mapW = max.x - min.x + 1;
		int mapH = max.y - min.y + 1;
		if (zoomDiff > 0) {
			mapX >>= zoomDiff;
			mapY >>= zoomDiff;
			mapW >>= zoomDiff;
			mapH >>= zoomDiff;
		} else {
			zoomDiff = Math.abs(zoomDiff);
			mapX <<= zoomDiff;
			mapY <<= zoomDiff;
			mapW <<= zoomDiff;
			mapH <<= zoomDiff;
		}
		int x = mapX - minX;
		int y = mapY - minY;
		int w = mapW;
		int h = mapH;
		g.setColor(PreviewMap.MAP_COLOR);
		g.fillRect(x, y, w, h);
		g.setColor(PreviewMap.GRID_COLOR);
		g.drawRect(x, y, w, h);
	}
}
