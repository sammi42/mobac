/* ------------------------------------------------------------------------

   LikConstraint.java

   Project: Testing

  --------------------------------------------------------------------------*/

/* ---
 created: 03.09.2008 a.sander

 $History:$
 --- */

package rmp.gui.tools;

/**
 * Constraint for use with LikLayout
 * 
 * @author a.sander
 * 
 */
public class LikConstraint {
	private int xPos;
	private int yPos;
	private int width;
	private int height;
	private boolean variableHeight;
	private boolean variableWidth;

	/**
	 * Constraint for use with LikLayout
	 * <P>
	 * 
	 * @param x
	 *            Horizontal grid count (start at 0)
	 * @param y
	 *            Vertical grid count (start at 0)
	 * @param w
	 *            Width in grid elements
	 * @param h
	 *            Height in grid elements
	 */
	public LikConstraint(int x, int y, int w, int h) {
		variableWidth = w < 0 ? true : false;
		variableHeight = h < 0 ? true : false;

		xPos = x;
		yPos = y;
		width = Math.abs(w);
		height = Math.abs(h);
	}

	public int getXPos() {
		return xPos;
	}

	public int getYPos() {
		return yPos;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isVariableHeight() {
		return variableHeight;
	}

	public boolean isVariableWidth() {
		return variableWidth;
	}
}
