package org.openstreetmap.gui.jmapviewer.interfaces;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * @author Jan Peter Stotz
 */
public interface TileLoaderJob extends Runnable {

	public TileSource getTileSource();

	public int getTileX();

	public int getTileY();

	public int getZoom();

	public TileLoaderListener getListener();
}
