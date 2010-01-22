package mobac.gui.mapview;

import mobac.program.model.MercatorPixelCoordinate;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


public interface MapEventListener {

	/** the selection changed */
	public void selectionChanged(MercatorPixelCoordinate max, MercatorPixelCoordinate min);

	/** the zoom changed */
	public void zoomChanged(int newZoomLevel);

	/** the grid zoom changed */
	public void gridZoomChanged(int newGridZoomLevel);

	/** select the next map source from the map list */
	public void selectNextMapSource();

	/** select the previous map source from the map list */
	public void selectPreviousMapSource();

	public void mapSourceChanged(MapSource newMapSource);
}
