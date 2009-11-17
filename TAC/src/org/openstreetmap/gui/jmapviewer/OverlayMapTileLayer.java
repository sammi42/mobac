package org.openstreetmap.gui.jmapviewer;

import java.awt.Graphics;

import org.openstreetmap.gui.jmapviewer.JobDispatcher.JobThread;
import org.openstreetmap.gui.jmapviewer.interfaces.MapTileLayer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class OverlayMapTileLayer implements MapTileLayer {

	protected JMapViewer mapViewer;
	protected MapSource mapSource;

	public OverlayMapTileLayer(JMapViewer mapViewer, MapSource tileSource) {
		this.mapViewer = mapViewer;
		this.mapSource = tileSource;
	}

	public void startPainting(MapSource mapSource) {
	}

	public void paintTile(Graphics g, int gx, int gy, int tilex, int tiley, int zoom) {
		Tile tile = getTile(tilex, tiley, zoom);
		if (tile == null)
			return;
		tile.paintTransparent(g, gx, gy);
	}

	/**
	 * retrieves a tile from the cache. If the tile is not present in the cache
	 * a load job is added to the working queue of {@link JobThread}.
	 * 
	 * @param tilex
	 * @param tiley
	 * @param zoom
	 * @return specified tile from the cache or <code>null</code> if the tile
	 *         was not found in the cache.
	 */
	protected Tile getTile(int tilex, int tiley, int zoom) {
		int max = (1 << zoom);
		if (tilex < 0 || tilex >= max || tiley < 0 || tiley >= max)
			return null;
		Tile tile = mapViewer.tileCache.getTile(mapSource, tilex, tiley, zoom);
		if (tile == null) {
			tile = new Tile(mapSource, tilex, tiley, zoom);
			mapViewer.tileCache.addTile(tile);
		}
		if (!tile.isLoaded()) {
			mapViewer.jobDispatcher.addJob(mapViewer.tileLoader.createTileLoaderJob(mapSource,
					tilex, tiley, zoom));
		}
		return tile;
	}

}
