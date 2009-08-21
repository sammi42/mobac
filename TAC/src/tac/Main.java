package tac;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tac.gui.MainGUI;
import tac.program.DirectoryManager;
import tac.program.Logging;
import tac.program.TACInfo;
import tac.program.model.Settings;
import tac.utilities.TACExceptionHandler;
import tac.utilities.Utilities;

/**
 * Java 6 version of the main starter class
 */
public class Main {

	public Main() {
		Logging.configureLogging();
		TACInfo.initialize(); // Load revision info
		TACExceptionHandler.installToolkitEventQueueProxy();
		try {
			// Logging.logSystemProperties();
			DirectoryManager.initialize();
			Utilities.checkFileSetup();
			Settings.loadOrQuit();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MainGUI.createMainGui();
				}
			});
		} catch (Throwable t) {
			TACExceptionHandler.processException(t);
		}
	}

	/**
	 * Start TAC without Java Runtime version check
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new Main();
		} catch (Throwable t) {
			TACExceptionHandler.processException(t);
		}
	}
}