package tac;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import tac.gui.MainGUI;
import tac.program.Logging;
import tac.program.Settings;
import tac.utilities.TACExceptionHandler;

public class StartTAC {
	private static Logger log = Logger.getLogger(StartTAC.class);

	public static void main(String[] args) {
		Logging.configureLogging();
		// Logging.logSystemProperties();
		Settings.getInstance().loadOrQuit();
		TACExceptionHandler.installToolkitEventQueueProxy();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log.error("The selection of look and feel failed!", e);
		}
		checkVersion();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainGUI.createMainGui();
			}
		});
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
		if (version < 1006)
			JOptionPane.showMessageDialog(null,
					"The used Java Runtime Environment does not meet the minimum requirements.\n"
							+ "TrekBuddy Atlas Creator requires at least Java 1.6 or higher.\n"
							+ "Otherwise several functions will not work properly.\n\n"
							+ "Detected Java Runtime Version: " + ver,
					"Java Runtime version problem detected", JOptionPane.WARNING_MESSAGE);
	}
}