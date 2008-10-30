package tac;

import javax.swing.UIManager;

import org.apache.log4j.Logger;

import tac.gui.GUI;
import tac.program.Logging;
import tac.program.Settings;

public class StartTAC {

	private static Logger log = Logger.getLogger(StartTAC.class);

	public static void main(String[] args) {
		Logging.configureLogging();
		Settings.getInstance().loadOrQuit();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// UIManager.setLookAndFeel(
			// "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
			// UIDefaults defaults = UIManager.getDefaults();
			// Enumeration keys = defaults.keys();
			// while (keys.hasMoreElements()) {
			// Object key = keys.nextElement();
			// Object value = defaults.get(key);
			// if (value != null && value instanceof Font) {
			// UIManager.put(key, null);
			// Font font = UIManager.getFont(key);
			// if (font != null) {
			// float size = font.getSize2D();
			// UIManager.put(key, new FontUIResource(font.deriveFont(size +3)));
			// }
			// }
			// }
		} catch (Exception e) {
			log.error("The selection of look and feel failed!", e);
		}
		GUI g = new GUI();
		g.setVisible(true);
	}
}