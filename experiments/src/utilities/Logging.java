package utilities;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class Logging {

	public static void configureConsoleLogging(Level level) {
		Logger logger = Logger.getRootLogger();
		ConsoleAppender consoleAppender = new ConsoleAppender(new SimpleLayout());
		consoleAppender.setThreshold(level);
		logger.addAppender(consoleAppender);
	}

	public static void disableLogging() {
		Logger logger = Logger.getRootLogger();
		logger.setLevel(Level.OFF);
	}

}
