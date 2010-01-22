package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 * Abstract base class for all mouse controller implementations. For
 * implementing your own controller create a class that derives from this one
 * and implements one or more of the following interfaces:
 * <ul>
 * <li>{@link MouseListener}</li>
 * <li>{@link MouseMotionListener}</li>
 * <li>{@link MouseWheelListener}</li>
 * </ul>
 */
public abstract class JMapController {

	protected final JMapViewer map;
	protected boolean enabled = false;

	public JMapController(JMapViewer map) {
		this.map = map;
	}

	public JMapController(JMapViewer map, boolean enabled) {
		this(map);
		if (enabled)
			enable();
	}

	public void enable() {
		if (enabled)
			return;
		if (this instanceof MouseListener)
			map.addMouseListener((MouseListener) this);
		if (this instanceof MouseWheelListener)
			map.addMouseWheelListener((MouseWheelListener) this);
		if (this instanceof MouseMotionListener)
			map.addMouseMotionListener((MouseMotionListener) this);
		this.enabled = true;
	}

	public void disable() {
		if (!enabled)
			return;
		if (this instanceof MouseListener)
			map.removeMouseListener((MouseListener) this);
		if (this instanceof MouseWheelListener)
			map.removeMouseWheelListener((MouseWheelListener) this);
		if (this instanceof MouseMotionListener)
			map.removeMouseMotionListener((MouseMotionListener) this);
		this.enabled = false;
	}
}
