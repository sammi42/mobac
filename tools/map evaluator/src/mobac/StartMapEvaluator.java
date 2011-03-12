package mobac;

import mobac.gui.MapEvaluator;
import mobac.mapsources.MapEvaluatorMapSourcesManager;
import mobac.program.DirectoryManager;
import mobac.program.Logging;
import mobac.program.ProgramInfo;
import mobac.program.model.Settings;
import mobac.program.tilestore.TileStore;
import mobac.utilities.GUIExceptionHandler;

import org.apache.log4j.Level;

public class StartMapEvaluator {

	public static void main(String[] args) {
		StartMOBAC.setLookAndFeel();
		ProgramInfo.PROG_NAME = "MOBAC Map Evaluator";
		ProgramInfo.PROG_NAME_SHORT = null;
		GUIExceptionHandler.registerForCurrentThread();
		GUIExceptionHandler.installToolkitEventQueueProxy();
		ProgramInfo.initialize();
		Logging.configureConsoleLogging(Level.TRACE, Logging.ADVANCED_LAYOUT);
		DirectoryManager.initialize();
		MapEvaluatorMapSourcesManager.initialitze();
		try {
			Settings.load();
		} catch (Exception e) {
			// Load settings.xml only if it exists
		}
		TileStore.initialize();
		new MapEvaluator().setVisible(true);
	}

}
