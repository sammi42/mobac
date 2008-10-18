package tac.program;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.xml.DOMConfigurator;

public class Logging {

	protected static final String CONFIG_FILE = "log4j.xml";

	public static void configureLogging() {
		File f = new File(CONFIG_FILE);
		// We test for the cifiguration file, if it exists we use it, otherwise
		// we perform simple logging to the console
		if (f.exists() && f.isFile()) {
			DOMConfigurator.configure(f.getAbsolutePath());
		} else {
			Logger logger = Logger.getRootLogger();
			logger.setLevel(Level.ERROR);
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			logger.addAppender(consoleAppender);
		}
	}
}
