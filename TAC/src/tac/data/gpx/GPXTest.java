package tac.data.gpx;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import tac.data.gpx.interfaces.Gpx;
import tac.data.gpx.interfaces.Wpt;

public class GPXTest {

	public static Gpx loadGpxFile(File f) throws JAXBException {
		Class<?>[] gpxClasses = { tac.data.gpx.gpx10.Gpx10.class, tac.data.gpx.gpx11.Gpx11.class };
		// Load GPX 1.0 and GPX 1.1 definition into the JAXB context
		JAXBContext context = JAXBContext.newInstance(gpxClasses, null);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (Gpx) unmarshaller.unmarshal(f);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Gpx gpx = loadGpxFile(new File(
					"D:/GPS/Traces/GPX/2008-06-18-16-09-45.gpx"));
			System.out.println(gpx + " " + gpx.getVersion());
			for (Wpt wpt : gpx.getWpt()) {
				System.out.println("\t" + wpt.getName() + " lat=" + wpt.getLat() + " lon="
						+ wpt.getLon());
			}
			gpx = loadGpxFile(new File("D:/GPS/GPX10.gpx"));
			System.out.println(gpx + " " + gpx.getVersion());
			for (Wpt wpt : gpx.getWpt()) {
				System.out.println("\t" + wpt.getName() + " lat=" + wpt.getLat() + " lon="
						+ wpt.getLon());
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
