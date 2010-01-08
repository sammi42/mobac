package tac.program;

import java.io.InputStream;
import java.util.Properties;

import tac.Main;
import tac.utilities.Utilities;

public class TACInfo {

	public static String PROG_NAME = "TrekBuddy Atlas Creator";
	private static String version = null;
	private static String revision = "";

	/**
	 * Show or hide the detailed revision info in the main windows title
	 */
	private static boolean titleHideRevision = false;

	public static void initialize() {
		InputStream propIn = Main.class.getResourceAsStream("tac.properties");
		try {
			Properties props = new Properties();
			props.load(propIn);
			version = props.getProperty("tac.version");
			titleHideRevision = Boolean.parseBoolean(props
					.getProperty("tac.revision.hide", "false"));
		} catch (Exception e) {
			Logging.LOG.error("Error reading tac.properties", e);
		} finally {
			Utilities.closeStream(propIn);
		}
		propIn = Main.class.getResourceAsStream("tac-rev.properties");
		try {
			Properties props = new Properties();
			props.load(propIn);
			String rev = props.getProperty("tac.revision");
			revision = "(" + rev + ")";
		} catch (Exception e) {
			Logging.LOG.error("Error reading tac-rev.properties", e);
		} finally {
			Utilities.closeStream(propIn);
		}
	}

	public static String getVersion() {
		if (version != null)
			return version;
		else
			return "UNKNOWN";
	}

	public static String getRevision() {
		return revision;
	}

	public static String getVersionTitle() {
		String title;
		if (version != null) {
			title = PROG_NAME + " " + version;
		} else
			title = PROG_NAME + " unknown version";
		return title;
	}

	public static String getCompleteTitle() {
		String title = getVersionTitle();
		if (!titleHideRevision)
			title += " " + revision;
		return title;
	}

}
