package tac;

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
		Settings.getInstance().loadOrQuit();
		TACExceptionHandler.installToolkitEventQueueProxy();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log.error("The selection of look and feel failed!", e);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainGUI g = new MainGUI();
				g.setVisible(true);
			}
		});
	}
}