package geotiff;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.gce.geotiff.GeoTiffWriter;

public class Gt2GeoTiffTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
	        BufferedImage img = new BufferedImage(1024,1024,BufferedImage.TYPE_INT_RGB);
			GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null);
			GridGeometry2D gridGeo;
			GridCoverage2D cov = factory.create("Coverage", img, gridGeo);
			GeoTiffWriter writer = new GeoTiffWriter(new File("test,tiff"));
			writer.write(cov, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
