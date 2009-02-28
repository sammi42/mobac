package tac.gui.preview;

import tac.program.EastNorthCoordinate;

public interface MapSelectionListener {

	/** the selection changed */
	public void selectionChanged(EastNorthCoordinate max, EastNorthCoordinate min);

	/** the zoom changed */
	public void zoomChanged(int zoomLevel);
	
	/** select the next map source from the map list */
	public void selectNextMapSource();
	
	/** select the previous map source from the map list */
	public void selectPreviousMapSource();
}
