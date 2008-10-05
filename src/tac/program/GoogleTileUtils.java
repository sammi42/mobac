package tac.program;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 * A set of simple routines to provide information about google tiles (API v1).
 * These routines are not written for speed.
 * These routines assume a tile size of 256 pixels square.
 * Internally a sort of offset mercator projection is used, this places the origin (0,0)
 * at the top left and goes to +1,+1 at the bottom right.
 * I believe a proper mercator would have 0,0 in the middle and range from -0.5,-0.5 bottom left to +0.5,+0.5 top right.
 * I've used this system because it provides a better fit with a typical the pixel coordinate system.
 */
public class GoogleTileUtils {
   static int TILE_SIZE = 256;

   /**
    * Returns the pixel offset of a latitude and longitude within a single typical google tile.
    * @param lat
    * @param lng
    * @param zoom
    * @return
    */
   public static Point getPixelOffsetInTile(double lat, double lng, int zoom) {
      Point pixelCoords = toZoomedPixelCoords(lat, lng, zoom);

      return new Point(pixelCoords.x % TILE_SIZE, pixelCoords.y % TILE_SIZE);
   }

   /**
   * returns a Rectangle2D with x = lon, y = lat, width=lonSpan, height=latSpan
   * for an x,y,zoom as used by google.
   */
   public static Rectangle2D.Double getTileRect(int x, int y, int zoom) {
      int tilesAtThisZoom = 1 << (17 - zoom);
      double lngWidth     = 360.0 / tilesAtThisZoom; // width in degrees longitude
      double lng          = -180 + (x * lngWidth); // left edge in degrees longitude

      double latHeightMerc = 1.0 / tilesAtThisZoom; // height in "normalized" mercator 0,0 top left
      double topLatMerc    = y * latHeightMerc; // top edge in "normalized" mercator 0,0 top left
      double bottomLatMerc = topLatMerc + latHeightMerc;

      // convert top and bottom lat in mercator to degrees
      // note that in fact the coordinates go from about -85 to +85 not -90 to 90!
      double bottomLat = (180 / Math.PI) * ((2 * Math.atan(Math.exp(Math.PI * (1 - (2 * bottomLatMerc)))))
                         - (Math.PI / 2));

      double topLat = (180 / Math.PI) * ((2 * Math.atan(Math.exp(Math.PI * (1 - (2 * topLatMerc))))) - (Math.PI / 2));

      double latHeight = topLat - bottomLat;

      return new Rectangle2D.Double(lng, bottomLat, lngWidth, latHeight);
   }

   /**
       * returns the lat/lng as an "Offset Normalized Mercator" pixel coordinate,
       * this is a coordinate that runs from 0..1 in latitude and longitude with 0,0 being
       * top left. Normalizing means that this routine can be used at any zoom level and
       * then multiplied by a power of two to get actual pixel coordinates.
       * @param lat in degrees
       * @param lng in degrees
       * @return
       */
   public static Point2D toNormalisedPixelCoords(double lat, double lng) {
      // first convert to Mercator projection
      // first convert the lat lon to mercator coordintes.
      if (lng > 180) {
         lng -= 360;
      }

      lng /= 360;
      lng += 0.5;

      lat = 0.5 - ((Math.log(Math.tan((Math.PI / 4) + ((0.5 * Math.PI * lat) / 180))) / Math.PI) / 2.0);

      return new Point2D.Double(lng, lat);
   }

   /**
    * returns a point that is a google tile reference for the tile containing the lat/lng and at the zoom level.
    * @param lat
    * @param lng
    * @param zoom
    * @return
    */
   public static Point toTileXY(double lat, double lng, int zoom) {
      Point2D normalised = toNormalisedPixelCoords(lat, lng);
      int scale          = 1 << (17 - zoom);

      // can just truncate to integer, this looses the fractional "pixel offset"
      return new Point((int) (normalised.getX() * scale), (int) (normalised.getY() * scale));
   }

   /**
    * returns a point that is a google pixel reference for the particular lat/lng and zoom
    * assumes tiles are 256x256.
    * @param lat
    * @param lng
    * @param zoom
    * @return
    */
   public static Point toZoomedPixelCoords(double lat, double lng, int zoom) {
      Point2D normalised = toNormalisedPixelCoords(lat, lng);
      double scale       = (1 << (17 - zoom)) * TILE_SIZE;

      return new Point((int) (normalised.getX() * scale), (int) (normalised.getY() * scale));
   }

   /**
    * Returns a google maps satellite type string for the tile containing the lat and lng at the zoom level.
    * @param lat
    * @param lng
    * @param zoom
    * @return
    */
   @SuppressWarnings("unused")
private static String getSatelliteRef(double lat, double lng, int zoom) {
      Point tileXY = toTileXY(lat, lng, zoom);
      int invZoom  = 17 - zoom;
      int stepSize = 1 << (17 - zoom);
      int currentX = 0;
      int currentY = 0;

      StringBuffer satString = new StringBuffer(zoom);
      satString.append("t");

      for (int i = 0; i < invZoom; i++) {
         stepSize >>= 1;

         if ((currentY + stepSize) > tileXY.y) {
            if ((currentX + stepSize) > tileXY.x) {
               satString.append('q');
            }
            else {
               currentX += stepSize;
               satString.append('r');
            }
         }
         else {
            currentY += stepSize;

            if ((currentX + stepSize) > tileXY.x) {
               satString.append('t');
            }
            else {
               currentX += stepSize;
               satString.append('s');
            }
         }
      }

      return satString.toString();
   }

   /** Returns an x,y for a satellite reference
   * @param string
   * @return
   */
   @SuppressWarnings("unused")
private static Point satelliteRefToTileXY(String satelliteRef) {
      // must start with "t"
      if ((satelliteRef == null) || (satelliteRef.length() == 0) || (satelliteRef.charAt(0) != 't')) {
         throw new RuntimeException("satellite string must start with 't'");
      }

      int x = 0; // x
      int y = 0;

      for (int i = 1; i < satelliteRef.length(); i++) {
         x <<= 1;
         y <<= 1;

         char c = satelliteRef.charAt(i);

         switch (c) {
            case 's':
               y += 1;
               x += 1;

               break;

            case 'r':
               x += 1;

               break;

            case 'q':
               y += 0;

               break;

            case 't':
               y += 1;

               break;

            default:
               throw new RuntimeException("satellite char '" + c + "' when decoding keyhole string.");
         }
      }

      return new Point(x, y);
   }

   /**
    * Returns the zoom level for the given satellite reference string
    * @param string
    * @return
    */
   @SuppressWarnings("unused")
private static int satelliteRefToZoom(String satRef) {
      return 18 - satRef.length();
   }

	public static double getTileBorderCordinate(double  lat, double lng, int zoom, String direction) {

		int currentTileNr;
		int nextTileNr;

		double cordinate = 0;

		if (direction.equals("N")) {

			cordinate = lat;

			currentTileNr = (int)toTileXY(cordinate, lng, zoom).getY();
			nextTileNr = currentTileNr;


			// Coordinate for next tile with ONE digit as precision
			double incrementOneZero = 0.1;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate += incrementOneZero, lng, zoom).getY();
			}

			cordinate -= 0.1;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with TWO digit as precision
			double incrementTwoZero = 0.01;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate += incrementTwoZero, lng, zoom).getY();
			}

			cordinate -= 0.01;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with THREE digit as precision
			double incrementThreeZero = 0.001;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate += incrementThreeZero, lng, zoom).getY();
			}

			cordinate -= 0.001;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with FOUR digit as precision
			double incrementFourZero = 0.0001;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate += incrementFourZero, lng, zoom).getY();
			}

			cordinate -= 0.0001;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with FIVE digit as precision
			double incrementFiveZero = 0.00001;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate += incrementFiveZero, lng, zoom).getY();
			}

			cordinate -= 0.00001;
			nextTileNr = currentTileNr;

			// Coordinate for next tile with SIX digit as precision
			double incrementSixZero = 0.000001;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate += incrementSixZero, lng, zoom).getY();
			}

			cordinate -= 0.00001;
		}

		if (direction.equals("E")) {


			cordinate = lng;

			currentTileNr = (int)toTileXY(lat, cordinate, zoom).getX();
			nextTileNr = currentTileNr;


			// Coordinate for next tile with ONE digit as precision
			double incrementOneZero = 0.1;

			while (currentTileNr == nextTileNr && cordinate <= 180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate += incrementOneZero, zoom).getX();
			}

			cordinate -= 0.1;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with TWO digit as precision
			double incrementTwoZero = 0.01;

			while (currentTileNr == nextTileNr && cordinate <= 180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate += incrementTwoZero, zoom).getX();
			}

			cordinate -= 0.01;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with THREE digit as precision
			double incrementThreeZero = 0.001;

			while (currentTileNr == nextTileNr && cordinate <= 180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate += incrementThreeZero, zoom).getX();
			}

			cordinate -= 0.001;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with FOUR digit as precision
			double incrementFourZero = 0.0001;

			while (currentTileNr == nextTileNr && cordinate <= 180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate += incrementFourZero, zoom).getX();
			}

			cordinate -= 0.0001;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with FIVE digit as precision
			double incrementFiveZero = 0.00001;

			while (currentTileNr == nextTileNr && cordinate <= 180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate += incrementFiveZero, zoom).getX();
			}

			cordinate -= 0.00001;
			nextTileNr = currentTileNr;

			// Coordinate for next tile with SIX digit as precision
			double incrementSixZero = 0.000001;

			while (currentTileNr == nextTileNr && cordinate <= 180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate += incrementSixZero, zoom).getX();
			}

			cordinate -= 0.00001;

		}

		if (direction.equals("S")) {


			cordinate = lat;

			currentTileNr = (int)toTileXY(cordinate, lng, zoom).getY();
			nextTileNr = currentTileNr;


			// Coordinate for next tile with ONE digit as precision
			double incrementOneZero = 0.1;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate -= incrementOneZero, lng, zoom).getY();
			}

			cordinate += 0.1;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with TWO digit as precision
			double incrementTwoZero = 0.01;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate -= incrementTwoZero, lng, zoom).getY();
			}

			cordinate += 0.01;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with THREE digit as precision
			double incrementThreeZero = 0.001;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate -= incrementThreeZero, lng, zoom).getY();
			}

			cordinate += 0.001;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with FOUR digit as precision
			double incrementFourZero = 0.0001;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate -= incrementFourZero, lng, zoom).getY();
			}

			cordinate += 0.0001;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with FIVE digit as precision
			double incrementFiveZero = 0.00001;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate -= incrementFiveZero, lng, zoom).getY();
			}

			cordinate += 0.00001;
			nextTileNr = currentTileNr;

			// Coordinate for next tile with SIX digit as precision
			double incrementSixZero = 0.000001;

			while (currentTileNr == nextTileNr) {

				nextTileNr = (int)toTileXY(cordinate -= incrementSixZero, lng, zoom).getY();
			}

			cordinate += 0.00001;
		}

		if (direction.equals("W")) {

			cordinate = lng;

			currentTileNr = (int)toTileXY(lat, cordinate, zoom).getX();
			nextTileNr = currentTileNr;


			// Coordinate for next tile with ONE digit as precision
			double incrementOneZero = 0.1;

			while (currentTileNr == nextTileNr && cordinate >= -180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate -= incrementOneZero, zoom).getX();
			}

			cordinate += 0.1;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with TWO digit as precision
			double incrementTwoZero = 0.01;

			while (currentTileNr == nextTileNr && cordinate >= -180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate -= incrementTwoZero, zoom).getX();
			}

			cordinate += 0.01;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with THREE digit as precision
			double incrementThreeZero = 0.001;

			while (currentTileNr == nextTileNr && cordinate >= -180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate -= incrementThreeZero, zoom).getX();
			}

			cordinate += 0.001;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with FOUR digit as precision
			double incrementFourZero = 0.0001;

			while (currentTileNr == nextTileNr && cordinate >= -180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate -= incrementFourZero, zoom).getX();
			}

			cordinate += 0.0001;
			nextTileNr = currentTileNr;


			// Coordinate for next tile with FIVE digit as precision
			double incrementFiveZero = 0.00001;

			while (currentTileNr == nextTileNr && cordinate >= -180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate -= incrementFiveZero, zoom).getX();
			}

			cordinate += 0.00001;
			nextTileNr = currentTileNr;

			// Coordinate for next tile with SIX digit as precision
			double incrementSixZero = 0.000001;

			while (currentTileNr == nextTileNr && cordinate >= -180.0) {

				nextTileNr = (int)toTileXY(lat, cordinate -= incrementSixZero, zoom).getX();
			}

			cordinate += 0.00001;
		}
		return cordinate;
	}
	
	public static TileXYMinMaxAndZoom getTileXYMinMax(Double latitudeMax, Double longitudeMax, Double latitudeMin, Double longitudeMin) {

		int zoomValue;
		Point topLeft;
		Point bottomRight;

		int xMax = 0;
		int xMin = 0;
		int yMax = 0;
		int yMin = 0;

		zoomValue = 0;

		search:
		for (int i = 0; i < 18; i++) {

			zoomValue = i;

			topLeft = GoogleTileUtils.toTileXY(latitudeMax, longitudeMin, i);
			bottomRight = GoogleTileUtils.toTileXY(latitudeMin, longitudeMax, i);

			xMin = (int)topLeft.getX();
			xMax = (int)bottomRight.getX();
			yMax = (int)bottomRight.getY();
			yMin = (int)topLeft.getY();

			if(xMax - xMin < 4 && yMax - yMin < 4) {
				break search;
			}
		}
		return new TileXYMinMaxAndZoom(xMin, xMax, yMin, yMax, zoomValue);
	}
}