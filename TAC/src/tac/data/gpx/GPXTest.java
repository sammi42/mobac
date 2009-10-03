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

import tac.data.gpx.gpx11.Gpx;
import tac.utilities.Utilities;

public class GPXTest {

	public static Gpx loadGpxFile(File f) throws JAXBException {
		// Load GPX 1.0 and GPX 1.1 definition into the JAXB context
		JAXBContext context = JAXBContext.newInstance(Gpx.class);

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
		JAXBContext context = JAXBContext.newInstance(Gpx.class);

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

}
