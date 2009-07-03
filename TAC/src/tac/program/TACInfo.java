package tac.program;

import java.io.InputStream;
import java.util.Properties;

import tac.Main;
import tac.utilities.Utilities;

public class TACInfo {

	private static String version = "unknown";
	private static String revision = "";

	/**
	 * Show or hide the detailed revision info in the main windows title
	 */
	private static boolean titleHideRevision = false;

	static {
		initialite();
	}

	private static void initialite() {
		InputStream propIn = Main.class.getResourceAsStream("tac.properties");
		try {
			Properties props = new Properties();
			props.load(propIn);
			version = props.getProperty("tac.version");
			titleHideRevision = Boolean.parseBoolean(props
					.getProperty("tac.revision.hide", "false"));
		} catch (Exception e) {
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
		String title = "TrekBuddy Atlas Creator " + version;
		if (!titleHideRevision)
			title += " " + revision;
		return title;
	}

}
