package tac;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Main class for starting TrekBuddy Atlas Creator.
 * 
 * This class performs the Java Runtime version check and if the correct version
 * is installed it creates a new instance of the class specified by
 * {@link #MAIN_CLASS}. The class to be instanciated is specified by it's name
 * intentionally as this allows to compile this class without any further class
 * dependencies.
 * 
 */
public class StartTAC {

	public static final String MAIN_CLASS = "tac.Main";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		checkVersion();
		try {
			Class.forName(MAIN_CLASS).newInstance();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Unable to start TAC: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	protected static void checkVersion() {
		String ver = System.getProperty("java.specification.version");
		if (ver == null)
			ver = "Unknown";
		String[] v = ver.split("\\.");
		int major = 0;
		int minor = 0;
		try {
			major = Integer.parseInt(v[0]);
			minor = Integer.parseInt(v[1]);
		} catch (Exception e) {
		}
		int version = (major * 1000) + minor;
		// 1.5 -> 1005; 1.6 -> 1006; 1.7 -> 1007
		if (version < 1006) {
			JOptionPane
					.showMessageDialog(
							null,
							"The used Java Runtime Environment does not meet the minimum requirements.\n"
									+ "TrekBuddy Atlas Creator requires at least Java 6 (1.6) or higher.\n"
									+ "Please update your Java Runtime before starting TrekBuddy Atlas Creator.\n\n"
									+ "Detected Java Runtime Version: " + ver,
							"Java Runtime version problem detected", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

}
