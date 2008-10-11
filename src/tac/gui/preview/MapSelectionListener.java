package tac.gui.preview;

import tac.program.EastNorthCoordinate;

public interface MapSelectionListener {

	public void selectionChanged(EastNorthCoordinate max, EastNorthCoordinate min);
}
