package geotiff;

import java.io.File;
import java.io.IOException;

import org.geotools.gce.geotiff.GeoTiffWriter;

public class Gt2GeoTiffTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			GeoTiffWriter writer = new GeoTiffWriter(new File("test,tiff"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
