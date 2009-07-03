package tac;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tac.gui.MainGUI;
import tac.program.Logging;
import tac.program.model.Settings;
import tac.utilities.TACExceptionHandler;
import tac.utilities.Utilities;

/**
 * Java 6 version of the main starter class
 */
public class Main {

	public Main() {
		Logging.configureLogging();
		// Logging.logSystemProperties();
		Utilities.checkFileSetup();
		Settings.loadOrQuit();
		TACExceptionHandler.installToolkitEventQueueProxy();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainGUI.createMainGui();
			}
		});
	}

	/**
	 * Start TAC without version check
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new Main();
		} catch (Exception e) {
		}
	}
}