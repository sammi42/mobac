package mobac;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import mobac.gui.MainGUI;
import mobac.program.DirectoryManager;
import mobac.program.EnvironmentSetup;
import mobac.program.Logging;
import mobac.program.ProgramInfo;
import mobac.program.model.Settings;
import mobac.program.tilestore.TileStore;
import mobac.utilities.GUIExceptionHandler;

/**
 * Java 6 version of the main starter class
 */
public class Main {

	public Main() {
		try {
			Logging.configureLogging();
			// MySocketImplFactory.install();
			ProgramInfo.initialize(); // Load revision info
			Logging.logSystemInfo();
			GUIExceptionHandler.installToolkitEventQueueProxy();
			// Logging.logSystemProperties();
			DirectoryManager.initialize();
			EnvironmentSetup.checkMemory();
			EnvironmentSetup.checkFileSetup();
			Settings.loadOrQuit();
			TileStore.initialize();
			EnvironmentSetup.upgrade();
			Logging.LOG.debug("Starting GUI");
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MainGUI.createMainGui();
				}
			});
		} catch (Throwable t) {
			GUIExceptionHandler.processException(t);
		}
	}

	/**
	 * Start MOBAC without Java Runtime version check
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new Main();
		} catch (Throwable t) {
			GUIExceptionHandler.processException(t);
		}
	}
}