package mobac.gui.mapview;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.openstreetmap.gui.jmapviewer.JMapController;

/**
 * Implements the GUI logic for the preview map panel that manages the map
 * selection and actions triggered by key strokes.
 * 
 */
public class PreviewMapController extends JMapController implements MouseMotionListener,
		MouseListener {

	/** start point of selection rectangle */
	private Point iStartSelectionPoint;

	/** end point of selection rectangle */
	private Point iEndSelectionPoint;

	public PreviewMapController(PreviewMap map, boolean enabled) {
		super(map, enabled);
	}

	/**
	 * Start drawing the selection rectangle if it was the 1st button (left
	 * button)
	 */
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			iStartSelectionPoint = e.getPoint();
			iEndSelectionPoint = e.getPoint();
		}
		map.grabFocus();
	}

	public void mouseDragged(MouseEvent e) {
		if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
			if (iStartSelectionPoint != null) {
				iEndSelectionPoint = e.getPoint();
				((PreviewMap) map).setSelectionByScreenPoint(iStartSelectionPoint,
						iEndSelectionPoint, true);
			}
		}
	}

	/**
	 * When dragging the map change the cursor back to it's pre-move cursor. If
	 * a double-click occurs center and zoom the map on the clicked location.
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getClickCount() == 1) {
				((PreviewMap) map).setSelectionByScreenPoint(iStartSelectionPoint, e.getPoint(),
						true);

				// reset the selections start and end
				iEndSelectionPoint = null;
				iStartSelectionPoint = null;
			}
		}
		map.grabFocus();
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
		map.grabFocus();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

}
