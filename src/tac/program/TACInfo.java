package tac.program;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import tac.StartTAC;
import tac.utilities.Utilities;

public class TACInfo {

	private static String version;
	private static String revision;

	static {
		InputStream propIn = StartTAC.class.getResourceAsStream("tac.properties");
		try {
			Properties props = new Properties();
			props.load(propIn);
			version = props.getProperty("tac.version");
			revision = props.getProperty("tac.revision");
		} catch (IOException e) {
			version = "unknown";
			revision = "";
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
		return "TrekBuddy Atlas Creator v" + version + revision;
	}

}
