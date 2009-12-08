/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package rmp.rmpfile.entries;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import rmp.interfaces.RmpFileEntry;
import rmp.rmpfile.RmpTools;

/**
 * Instance of the rmp.ini file
 * 
 * @author Andreas
 * 
 */
public class RmpIni implements RmpFileEntry {

	private static final Logger log = Logger.getLogger(RmpIni.class);

	byte[] rmpIni;

	public RmpIni(String layername, int count) {
		ByteArrayOutputStream bos;
		PrintStream ps;
		int i;

		bos = new ByteArrayOutputStream();
		ps = new PrintStream(bos);

		/* --- Content of rmp.ini is a simple INI file --- */
		ps.print("[T_Layers]\r\n");

		for (i = 0; i < count; i++) {
			String layerName = RmpTools.buildTileName(layername, i);
			log.trace("layer name: " + layerName);
			ps.print(i + "=" + layerName + "\r\n");
		}
		ps.flush();

		rmpIni = bos.toByteArray();
	}

	public byte[] getFileContent() {
		return rmpIni;
	}

	public String getFileExtension() {
		return "ini";
	}

	public String getFileName() {
		return "rmp";
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + getFileName() + "." + getFileExtension();
	}

}
