package tac.gui.preview;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.openstreetmap.gui.jmapviewer.JMapController;

public class PreviewMapController extends JMapController implements MouseMotionListener,
		MouseListener {

	/** start point of selection rectangle */
	private Point iStartSelectionPoint;

	/** end point of selection rectangle */
	private Point iEndSelectionPoint;

	/** A Timer for smoothly moving the map area */
	private static final Timer timer = new Timer(true);

	/** How often to do the moving */
	private static long timerInterval = 10;

	/** Does the moving */
	private MoveTask moveTask = new MoveTask();

	/** The horizontal direction of movement, -1:left, 0:stop, 1:right */
	private int directionX = 0;

	/** The vertical direction of movement, -1:up, 0:stop, 1:down */
	private int directionY = 0;

	/** The current x speed (pixels per timer interval) */
	private double speedX = 0;

	/** The current y speed (pixels per timer interval) */
	private double speedY = 0;

	/** The maximum speed (pixels per timer interval) */
	private static final double maxSpeed = 10;

	/** The speed increase per timer interval when a cursor button is clicked */
	private static final double acceleration = 0.05;

	/** The speed decrease per timer interval after a cursor button was released */
	private static final double deceleration = 0.2;

	public PreviewMapController(PreviewMap map) {
		super(map);

		InputMap inputMap = map.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = map.getActionMap();

		// map moving
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "MOVE_RIGHT");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "MOVE_LEFT");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "MOVE_UP");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "MOVE_DOWN");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "STOP_MOVE_HORIZONTALLY");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "STOP_MOVE_HORIZONTALLY");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "STOP_MOVE_VERTICALLY");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "STOP_MOVE_VERTICALLY");

		// zooming. To avoid confusion about which modifier key to use,
		// we just add all keys left of the space bar
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK, false),
				"ZOOM_IN");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.META_DOWN_MASK, false),
				"ZOOM_IN");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK, false),
				"ZOOM_IN");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK, false),
				"ZOOM_OUT");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.META_DOWN_MASK, false),
				"ZOOM_OUT");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK, false),
				"ZOOM_OUT");

		// map selection
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK, false),
				"PREVIOUS_MAP");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.META_DOWN_MASK, false),
				"PREVIOUS_MAP");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK, false),
				"PREVIOUS_MAP");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK, false),
				"NEXT_MAP");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.META_DOWN_MASK, false),
				"NEXT_MAP");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK, false),
				"NEXT_MAP");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, true),"REFRESH");
		
		// action mapping
		actionMap.put("MOVE_RIGHT", new MoveRightAction());
		actionMap.put("MOVE_LEFT", new MoveLeftAction());
		actionMap.put("MOVE_UP", new MoveUpAction());
		actionMap.put("MOVE_DOWN", new MoveDownAction());
		actionMap.put("STOP_MOVE_HORIZONTALLY", new StopMoveHorizontallyAction());
		actionMap.put("STOP_MOVE_VERTICALLY", new StopMoveVerticallyAction());
		actionMap.put("ZOOM_IN", new ZoomInAction());
		actionMap.put("ZOOM_OUT", new ZoomOutAction());
		actionMap.put("NEXT_MAP", new NextMapAction());
		actionMap.put("PREVIOUS_MAP", new PreviousMapAction());
		actionMap.put("REFRESH", new RefreshAction());

		timer.schedule(moveTask, 0, timerInterval);
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

	private class MoveRightAction extends AbstractAction {
		private static final long serialVersionUID = -6758721144600926744L;

		public void actionPerformed(ActionEvent e) {
			directionX = 1;
		}
	}

	private class MoveLeftAction extends AbstractAction {
		private static final long serialVersionUID = 2695221718338284951L;

		public void actionPerformed(ActionEvent e) {
			directionX = -1;
		}
	}

	private class MoveUpAction extends AbstractAction {
		private static final long serialVersionUID = -8414310977137213707L;

		public void actionPerformed(ActionEvent e) {
			directionY = -1;
		}
	}

	private class MoveDownAction extends AbstractAction {
		private static final long serialVersionUID = -5360890019457799681L;

		public void actionPerformed(ActionEvent e) {
			directionY = 1;
		}
	}

	private class StopMoveHorizontallyAction extends AbstractAction {
		private static final long serialVersionUID = -5360890019457799681L;

		public void actionPerformed(ActionEvent e) {
			directionX = 0;
		}
	}

	private class StopMoveVerticallyAction extends AbstractAction {
		private static final long serialVersionUID = -5360890019457799681L;

		public void actionPerformed(ActionEvent e) {
			directionY = 0;
		}
	}

	/** Moves the map depending on which cursor keys are pressed (or not) */
	private class MoveTask extends TimerTask {

		@Override
		public void run() {
			// update the x speed
			switch (directionX) {
			case -1:
				if (speedX > -1)
					speedX = -1;
				if (speedX > -1 * maxSpeed)
					speedX -= acceleration;
				break;
			case 0:
				if (speedX > 0)
					speedX -= deceleration;
				if (speedX < 0)
					speedX += deceleration;
				break;
			case 1:
				if (speedX < 1)
					speedX = 1;
				if (speedX < maxSpeed)
					speedX += acceleration;
				break;
			}

			// update the y speed
			switch (directionY) {
			case -1:
				if (speedY > -1)
					speedY = -1;
				if (speedY > -1 * maxSpeed)
					speedY -= acceleration;
				break;
			case 0:
				if (speedY > 0)
					speedY -= deceleration;
				if (speedY < 0)
					speedY += deceleration;
				break;
			case 1:
				if (speedY < 1)
					speedY = 1;
				if (speedY < maxSpeed)
					speedY += acceleration;
				break;
			}

			// move the map
			if (Math.floor(speedX) != 0 || Math.floor(speedY) != 0) {
				map.moveMap((int) Math.floor(speedX), (int) Math.floor(speedY));
			}
		}
	}

	private class ZoomInAction extends AbstractAction {
		private static final long serialVersionUID = 1471739991027644588L;

		public void actionPerformed(ActionEvent e) {
			map.zoomIn();
		}
	}

	private class ZoomOutAction extends AbstractAction {
		private static final long serialVersionUID = 1471739991027644588L;

		public void actionPerformed(ActionEvent e) {
			map.zoomOut();
		}
	}

	private class PreviousMapAction extends AbstractAction {
		private static final long serialVersionUID = -1492075614917423363L;

		public void actionPerformed(ActionEvent e) {
			for (MapSelectionListener msp : ((PreviewMap) map).mapSelectionListeners) {
				msp.selectPreviousMapSource();
			}
		}
	}

	private class NextMapAction extends AbstractAction {
		private static final long serialVersionUID = -1491235614917423363L;

		public void actionPerformed(ActionEvent e) {
			for (MapSelectionListener msp : ((PreviewMap) map).mapSelectionListeners) {
				msp.selectNextMapSource();
			}
		}
	}

	private class RefreshAction extends AbstractAction {

		private static final long serialVersionUID = -7235666079485033823L;

		public void actionPerformed(ActionEvent e) {
			((PreviewMap) map).RefreshMap();
		}
	}
	
}
