/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package rmp.gui;

import javax.swing.JComponent;

import rmp.rmpmaker.CalibratedImage;

/**
 * User interface for all plugins that are used in the rmp creator.
 * <P>
 * 
 * All UserInterface instances must have a constructor without arguments
 * 
 * @author Andreas
 * 
 */
public interface UserInterface {

	/**
	 * Returns the name of the User Interface. This name is presented to the
	 * user to select the format he likes. The language of the text is choosen
	 * by the current locale
	 * 
	 * @return name of the user interface
	 */
	public String getName();

	/**
	 * Initialize the UserInterface. Create all controls that shall be presented
	 * to the user.
	 * <P>
	 * 
	 * The language of the text in the controls is taken from the current locale
	 * 
	 * @param parent
	 *            Parent object for the controls
	 */
	public void initialize(JComponent parent);

	/**
	 * Release all resources
	 */
	public void release();

	/**
	 * Return the width in pixel that shall be allocated in the window. This
	 * function is called by the framework before initialize() was called.
	 * 
	 * @return width user interface in pixel
	 */
	public int getWidth();

	/**
	 * Return the height in pixel that shall be allocated in the window This
	 * function is called by the framework before initialize() was called.
	 * 
	 * @return width user interface in pixel
	 */
	public int getHeight();

	/**
	 * Checks if the current entries, the user has made, are plausible.
	 * <P>
	 * 
	 * The function returns null if the entries are plausible, otherwise an
	 * error message is returned. The language of the error message is taken
	 * from the current locale.
	 * <P>
	 * 
	 * If this function returns null, then the object is able to create a valid
	 * ImageCreator
	 * 
	 * @return null or error message
	 */
	public String checkPlausible();

	/**
	 * Get path/filename of the destination file
	 * 
	 * @return path of destination file
	 */
	public String getDestPath();

	/**
	 * Returns an array of Image Creators. Each Image Creator can create sub
	 * images of a requested size. There is one Image Creator per Layer
	 * 
	 * @return Image Creator
	 * @throws Exception
	 *             Error creating the image creator
	 */
	public CalibratedImage[] getImageCreator() throws Exception;
}
