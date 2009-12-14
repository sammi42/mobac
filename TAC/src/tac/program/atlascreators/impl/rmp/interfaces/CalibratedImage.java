/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package tac.program.atlascreators.impl.rmp.interfaces;

import java.awt.image.BufferedImage;

import tac.exceptions.MapCreationException;
import tac.program.atlascreators.impl.rmp.BoundingRect;

/**
 * Interface for Calibrated Images
 * <p>
 * The main purpose is to offer the functionality to export subimages from a
 * bigger images. Coordinates in images are always coded as latitude/longitude
 * with the map date WGS84
 * </p>
 * 
 * @author Andreas
 * 
 */
public interface CalibratedImage {

	/**
	 * Returns the bounding rect of the whole image
	 * 
	 * @return bound rect of the image
	 */
	public BoundingRect getBoundingRect();

	/**
	 * Returns the width of the image in pixel if the image will be exported in
	 * original size
	 * 
	 * @return width of image in pixel
	 */
	public int getImageWidth();

	/**
	 * Returns the height of the image in pixel if the image will be exported in
	 * original size
	 * 
	 * @return width of image in pixel
	 */
	public int getImageHeight();

	/**
	 * Create a subimage that contains exactly that part of the image that
	 * contains the given bounding rectangle. The image will be converted into a
	 * rectangular area of pixels with the given size.<BR>
	 * If the given coordinates are partly or full outside the image, then the
	 * part outside the image is returned as black pixel
	 * 
	 * @param area
	 *            bounding rectangle of map to return
	 * @param width
	 *            width of the image to return in pixel
	 * @param height
	 *            height of the image to return in pixel
	 * @return subimage from given bounding rectangle
	 * @throws MapCreationException
	 */
	public BufferedImage getSubImage(BoundingRect area, int width, int height) throws MapCreationException;

}
