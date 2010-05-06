package mobac.program;

import java.io.InputStream;
import java.util.Properties;

import mobac.Main;
import mobac.utilities.Utilities;

public class ProgramInfo {

	public static String PROG_NAME = "Mobile Atlas Creator";
	private static String version = null;
	private static String revision = "";

	/**
	 * Show or hide the detailed revision info in the main windows title
	 */
	private static boolean titleHideRevision = false;

	public static void initialize() {
		InputStream propIn = Main.class.getResourceAsStream("mobac.properties");
		try {
			Properties props = new Properties();
			props.load(propIn);
			version = props.getProperty("mobac.version");
			titleHideRevision = Boolean.parseBoolean(props.getProperty("mobac.revision.hide",
					"false"));
			System.getProperties().putAll(props);
		} catch (Exception e) {
			Logging.LOG.error("Error reading mobac.properties", e);
		} finally {
			Utilities.closeStream(propIn);
		}
		propIn = Main.class.getResourceAsStream("mobac-rev.properties");
		try {
			Properties props = new Properties();
			props.load(propIn);
			String rev = props.getProperty("mobac.revision");
			revision = rev;
			if (revision.endsWith("M"))
				revision = revision.substring(0, revision.length() - 1);
			int index = revision.indexOf(':');
			if (index > 0)
				revision = revision.substring(index + 1, revision.length());
			revision = "(" + revision + ")";
		} catch (Exception e) {
			Logging.LOG.error("Error reading mobac-rev.properties", e);
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
