package tac;

import tac.gui.GUI;
import tac.program.Logging;
import tac.program.Settings;

public class StartTAC {

	public static void main(String[] args) {
		Logging.configureLogging();
		Settings.getInstance().loadOrQuit();
		GUI g = new GUI();
		g.setVisible(true);
	}
}