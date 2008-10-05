package moller.preview;

import java.awt.geom.Point2D;

public interface MapSelectionListener {

	public void selectionChanged(Point2D.Double max, Point2D.Double min);
}
