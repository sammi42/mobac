package tac.gui.mapview;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.program.model.EastNorthCoordinate;

public interface MapEventListener {

	/** the selection changed */
	public void selectionChanged(EastNorthCoordinate max, EastNorthCoordinate min);

	/** the zoom changed */
	public void zoomChanged(int zoomLevel);

	/** select the next map source from the map list */
	public void selectNextMapSource();

	/** select the previous map source from the map list */
	public void selectPreviousMapSource();

	public void mapSourceChanged(MapSource newMapSource);
}
