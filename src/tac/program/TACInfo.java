package tac.program;

import java.io.InputStream;
import java.util.Properties;

import tac.StartTAC;
import tac.utilities.Utilities;

public class TACInfo {

	private static String version = "unknown";
	private static String revision = "";

	static {
		InputStream propIn = StartTAC.class.getResourceAsStream("tac.properties");
		try {
			Properties props = new Properties();
			props.load(propIn);
			version = props.getProperty("tac.version");
		} catch (Exception e) {
		} finally {
			Utilities.closeStream(propIn);
		}
		propIn = StartTAC.class.getResourceAsStream("tac-rev.properties");
		try {
			Properties props = new Properties();
			props.load(propIn);
			String rev = props.getProperty("tac.revision");
			revision = "(" + rev + ")";
		} catch (Exception e) {
		} finally {
			Utilities.closeStream(propIn);
		}
	}

	public static String getVersion() {
		return version;
	}

	public static String getRevision() {
		return revision;
	}

	public static String getCompleteTitle() {
		return "TrekBuddy Atlas Creator v" + version + " " + revision;
	}

}
