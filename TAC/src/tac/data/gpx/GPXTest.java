package tac.data.gpx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import tac.data.gpx.interfaces.Gpx;
import tac.data.gpx.interfaces.Wpt;
import tac.utilities.Utilities;

public class GPXTest {

	private static final Class<?>[] GPX_CLASSES = { tac.data.gpx.gpx10.Gpx10.class,
			tac.data.gpx.gpx11.Gpx11.class };

	public static Gpx loadGpxFile(File f) throws JAXBException {
		// Load GPX 1.0 and GPX 1.1 definition into the JAXB context
		JAXBContext context = JAXBContext.newInstance(GPX_CLASSES, null);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			return (Gpx) unmarshaller.unmarshal(is);
		} catch (FileNotFoundException e) {
			throw new JAXBException(e);
		} finally {
			Utilities.closeStream(is);
		}
	}

	public static void saveGpxFile(Gpx gpx, File f) throws JAXBException {
		// Load GPX 1.0 and GPX 1.1 definition into the JAXB context
		JAXBContext context = JAXBContext.newInstance(GPX_CLASSES, null);

		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		OutputStream os = null;
		try {
			os = new FileOutputStream(f);
			marshaller.marshal(gpx, os);
		} catch (FileNotFoundException e) {
			throw new JAXBException(e);
		} finally {
			Utilities.closeStream(os);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Gpx gpx = loadGpxFile(new File("D:/GPS/Traces/GPX/2008-06-18-16-09-45.gpx"));
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
