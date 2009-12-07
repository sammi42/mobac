/* ------------------------------------------------------------------------

   BackgroundInfo.java

   Project: Testing

  --------------------------------------------------------------------------*/

/* ---
 created: 25.09.2008 a.sander

 $History:$
 --- */

package rmp.gui.osm;

/**
 * Interface for information about background processes.
 * <P>
 * 
 * The front end can implement this interface to show the status of the
 * background process
 * 
 */
public interface BackgroundInfo {
	/**
	 * Background process has finished
	 * 
	 * @param e
	 *            Exception that leads to finish or null if finished without
	 *            error
	 */
	public void finished(Throwable e);

	/**
	 * Set a describing Text of the background action currently processed
	 * 
	 * @param text
	 *            text for output
	 */
	public void setActionText(String text);

}
