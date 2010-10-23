package geotiff;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class Gt2GeoTiffTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
//	        BufferedImage img = new BufferedImage(1024,1024,BufferedImage.TYPE_INT_RGB);
//			GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null);
//			Rectangle2D coords = new Rectangle2D.Double(49.0, 8.2, 2.0, 2.0);
//			BoundingBox bb = new ReferencedEnvelope(x1, x2, y1, y2, crs)
//			double minX = 8;
//			double maxX = 9;
//			double minY = 48;
//			double maxY = 49;
//			CoordinateReferenceSystem crs;
//			ReferencedEnvelope env = new ReferencedEnvelope(minX, maxX, minY, maxY, crs);
//			//GridGeometry2D gridGeo = new GridGeometry2D(new Rectangle(0,0,1024,1024),Rectangle2D.());
//			GridCoverage2D cov = factory.create("Coverage", img, env);
//			GeoTiffWriter writer = new GeoTiffWriter(new File("test,tiff"));
//			writer.write(cov, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
