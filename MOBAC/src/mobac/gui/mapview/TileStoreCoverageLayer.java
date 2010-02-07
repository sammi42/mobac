package mobac.gui.mapview;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import javax.swing.JOptionPane;

import mobac.gui.MainGUI;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.DelayedInterruptThread;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapLayer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

public class TileStoreCoverageLayer implements MapLayer {

	private final MapSource mapSource;
	private final int zoom;
	private final Point pixelCoordinateMin;
	private final Point pixelCoordinateMax;
	private final Point tileNumMin;
	private final Point tileNumMax;
	private BufferedImage coverageImage = null;

	public static void removeCacheCoverageLayers() {
		try {
			PreviewMap previewMap = MainGUI.getMainGUI().previewMap;
			Iterator<MapLayer> mapLayers = previewMap.mapLayers.iterator();
			MapLayer ml;
			while (mapLayers.hasNext()) {
				ml = mapLayers.next();
				if (ml instanceof TileStoreCoverageLayer) {
					mapLayers.remove();
				}
			}
		} catch (Exception e) {
		}
	}

	public TileStoreCoverageLayer(PreviewMap mapViewer, int zoom) {
		this.mapSource = mapViewer.getMapSource();
		this.zoom = zoom;

		MapSpace mapSpace = mapSource.getMapSpace();
		int tileSize = mapSpace.getTileSize();
		int mapViewerZoom = mapViewer.getZoom();
		Point min = mapViewer.getTopLeftCoordinate();
		Point max = new Point(min.x + mapViewer.getWidth(), min.y + mapViewer.getHeight());
		min = mapSpace.changeZoom(min, mapViewerZoom, zoom);
		max = mapSpace.changeZoom(max, mapViewerZoom, zoom);

		tileNumMax = new Point(max.x / tileSize, max.y / tileSize);
		tileNumMin = new Point(min.x / tileSize, min.y / tileSize);
		pixelCoordinateMax = new Point(tileNumMax.x * tileSize + tileSize - 1, tileNumMax.y
				* tileSize + tileSize - 1);
		pixelCoordinateMin = new Point(tileNumMin.x * tileSize, tileNumMin.y * tileSize);
		updateCoverageImage();
	}

	private void updateCoverageImage() {
		coverageImage = null;
		Runnable r = new Runnable() {

			public void run() {
				coverageImage = TileStore.getInstance().getCacheCoverage(mapSource, zoom,
						tileNumMin, tileNumMax);
			}
		};
		DelayedInterruptThread thread = new DelayedInterruptThread(r);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
		}
		if (coverageImage == null) {
			JOptionPane.showMessageDialog(MainGUI.getMainGUI(),
					"Failed to retrieve tile store coverage data.\n"
							+ "May be the selected area and zoom is too large.", "Error",
					JOptionPane.ERROR_MESSAGE);
			removeCacheCoverageLayers();
		}
		MainGUI.getMainGUI().previewMap.repaint();
	}

	public void paint(JMapViewer mapViewer, Graphics2D g, int zoom, int minX, int minY, int maxX,
			int maxY) {
		if (coverageImage == null)
			return;
		paintCoverage(g, zoom, minX, minY, maxX, maxY);
	}

	protected void paintCoverage(Graphics2D g, int zoom, int minX, int minY, int maxX, int maxY) {
		Point max = pixelCoordinateMax;
		Point min = pixelCoordinateMin;
		MapSpace mapSpace = mapSource.getMapSpace();
		int mapX = mapSpace.xChangeZoom(min.x, this.zoom, zoom);
		int mapY = mapSpace.yChangeZoom(min.y, this.zoom, zoom);
		int mapW = mapSpace.xChangeZoom(max.x - min.x + 1, this.zoom, zoom);
		int mapH = mapSpace.yChangeZoom(max.y - min.y + 1, this.zoom, zoom);
		int x = mapX - minX;
		int y = mapY - minY;
		int w = mapW;
		int h = mapH;
		g.drawImage(coverageImage, x, y, w, h, null);
	}

}
