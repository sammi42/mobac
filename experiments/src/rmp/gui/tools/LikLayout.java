/* ------------------------------------------------------------------------

   LikLayout.java

   Project: Testing

  --------------------------------------------------------------------------*/

/* ---
 created: 03.09.2008 a.sander

 $History:$
 --- */

package rmp.gui.tools;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Layout Manager for the Lik-Design
 * <P>
 * 
 * The Lik-Design is based on a Grid where every component may cover multiple
 * grid elemens in horizontal and vertical direction
 * 
 * @author a.sander
 * 
 */
public class LikLayout implements LayoutManager2 {
	private int topBorder;
	private int leftBorder;
	private int cellWidth;
	private int cellHeight;
	private int horizontalGap;
	private int verticalGap;

	private int currentColumn;
	private int currentLine;

	Hashtable<Component, LikConstraint> constraintTable;

	public final static String NEW_LINE = "A";

	/**
	 * Constructor
	 */
	public LikLayout() {
		/* --- Initialize Variables --- */
		constraintTable = new Hashtable<Component, LikConstraint>();

		/* --- Set Standard values --- */
		topBorder = 5;
		leftBorder = 5;
		cellWidth = 85;
		cellHeight = 23;
		horizontalGap = 10;
		verticalGap = 3;
	}

	/**
	 * Distance from first cell to top of window
	 */
	public int getTopBorder() {
		return topBorder;
	}

	/**
	 * Distance from first cell to top of window
	 */
	public void setTopBorder(int topBorder) {
		this.topBorder = topBorder;
	}

	/**
	 * Distance from first cell to left marging
	 */
	public int getLeftBorder() {
		return leftBorder;
	}

	/**
	 * Distance from first cell to left marging
	 */
	public void setLeftBorder(int leftBorder) {
		this.leftBorder = leftBorder;
	}

	/**
	 * Width of a cell in pixel
	 */
	public int getCellWidth() {
		return cellWidth;
	}

	/**
	 * Width of a cell in pixel
	 */
	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}

	/**
	 * Height of a cell in pixel
	 */
	public int getCellHeight() {
		return cellHeight;
	}

	/**
	 * Height of a cell in pixel
	 */
	public void setCellHeight(int cellHeight) {
		this.cellHeight = cellHeight;
	}

	/**
	 * Horizontal gap between cells in pixel
	 */
	public int getHorizontalGap() {
		return horizontalGap;
	}

	/**
	 * Horizontal gap between cells in pixel
	 */
	public void setHorizontalGap(int horizontalGap) {
		this.horizontalGap = horizontalGap;
	}

	/**
	 * Vertical gap between cells in pixel
	 */
	public int getVerticalGap() {
		return verticalGap;
	}

	/**
	 * Vertical gap between cells in pixel
	 */
	public void setVerticalGap(int verticalGap) {
		this.verticalGap = verticalGap;
	}

	/**
	 * Add a new component to the internal assignment between components and
	 * constraints
	 */
	public void addLayoutComponent(Component comp, Object constraint) {

		/* --- Create a constraint if there is none --- */
		if (constraint == null)
			constraint = new LikConstraint(currentColumn, currentLine, 1, 1);

		/* --- Special Treatment, if constraint is a string --- */
		if (constraint instanceof String) {
			addLayoutComponent((String) constraint, comp);
			return;
		}

		/* --- Make sure the constraint is a LikConstraint --- */
		if (!(constraint instanceof LikConstraint))
			throw new IllegalArgumentException("Only LikConstraint is allowed as constraint");

		/* --- Add the element to the map --- */
		constraintTable.put(comp, (LikConstraint) constraint);

		/* --- Calculate position for next element without constraint --- */
		currentColumn += ((LikConstraint) constraint).getWidth();
	}

	/**
	 * Add component without constraint but with a hint
	 */
	public void addLayoutComponent(String hint, Component comp) {
		if (hint != null && hint.compareTo(NEW_LINE) == 0) {
			currentColumn = 0;
			currentLine++;
		}

		addLayoutComponent(comp, new LikConstraint(currentColumn, currentLine, 1, 1));
	}

	/**
	 * Removes a component from the container
	 */
	public void removeLayoutComponent(Component comp) {
		constraintTable.remove(comp);
	}

	/**
	 * Alignment of components
	 */
	public float getLayoutAlignmentX(Container container) {
		// No effect
		return 0;
	}

	/**
	 * Alignment of components
	 */
	public float getLayoutAlignmentY(Container container) {
		// No effect
		return 0;
	}

	/**
	 * Invalidate cached data of the layout
	 */
	public void invalidateLayout(Container container) {
		// Not used
	}

	/**
	 * Calculate maximum size that makes sense for the layout
	 */
	public Dimension maximumLayoutSize(Container container) {
		/* --- Same as preferred Size --- */
		return preferredLayoutSize(container);
	}

	/**
	 * Calculate minimum size that makes sense for the layout
	 */
	public Dimension minimumLayoutSize(Container container) {
		/* --- Same as preferred Size --- */
		return preferredLayoutSize(container);
	}

	/**
	 * Calculate optimal size for the layout
	 */
	public Dimension preferredLayoutSize(Container container) {
		int maxx = 0;
		int maxy = 0;
		Dimension size;
		Iterator<LikConstraint> it;
		LikConstraint constraint;

		/* --- Get the maximum X and Y position --- */
		it = constraintTable.values().iterator();
		while (it.hasNext()) {
			constraint = it.next();
			if (constraint.getXPos() + constraint.getWidth() > maxx)
				maxx = constraint.getXPos() + constraint.getWidth();

			if (constraint.getYPos() + constraint.getHeight() > maxy)
				maxy = constraint.getYPos() + constraint.getHeight();
		}

		/* --- Calculate the size of the container --- */
		size = new Dimension(2 * leftBorder + maxx * cellWidth + (maxx - 1) * horizontalGap, 2
				* topBorder + maxy * cellHeight + (maxy - 1) * verticalGap);

		return size;
	}

	/**
	 * Set size for all components
	 */
	public void layoutContainer(Container container) {
		Component[] comps;
		int start_variable_x = 9999;
		int start_variable_y = 9999;
		Iterator<LikConstraint> it;
		LikConstraint constraint;
		int add_width;
		int add_height;
		int i;
		int x, y, w, h;
		Dimension preferred_size;
		Dimension container_size;

		/* --- Check on which row/column the variable components start --- */
		it = constraintTable.values().iterator();
		while (it.hasNext()) {
			constraint = it.next();
			if (constraint.isVariableHeight() && constraint.getYPos() < start_variable_y)
				start_variable_y = constraint.getYPos();

			if (constraint.isVariableWidth() && constraint.getXPos() < start_variable_x)
				start_variable_x = constraint.getXPos();
		}

		/*
		 * --- Calculate the additional size that is necessary for the variable
		 * components ---
		 */
		preferred_size = preferredLayoutSize(container);
		container_size = container.getSize();
		add_width = container_size.width - preferred_size.width;
		add_height = container_size.height - preferred_size.height;

		/* --- Get all components of the container --- */
		comps = container.getComponents();

		/* --- Process all containers --- */
		for (i = 0; i < comps.length; i++) {
			/* --- Get constraints of the container --- */
			constraint = constraintTable.get(comps[i]);

			/* --- Must be known --- */
			if (constraint == null)
				throw new IllegalStateException("Unknown component: " + comps[i].toString());

			/* --- Calculate standard position and width --- */
			x = leftBorder + constraint.getXPos() * (cellWidth + horizontalGap);
			y = topBorder + constraint.getYPos() * (cellHeight + verticalGap);
			w = constraint.getWidth() * cellWidth + (constraint.getWidth() - 1) * horizontalGap;
			h = constraint.getHeight() * cellHeight + (constraint.getHeight() - 1) * verticalGap;

			/* --- Add additional width/height for variable elements --- */
			if (constraint.isVariableWidth())
				w += add_width;
			else if (constraint.getXPos() > start_variable_x)
				x += add_width;

			if (constraint.isVariableHeight())
				h += add_height;
			else if (constraint.getYPos() > start_variable_y)
				y += add_height;

			/* --- Set values to component --- */
			comps[i].setLocation(x, y);
			comps[i].setSize(w, h);
		}
	}

}
