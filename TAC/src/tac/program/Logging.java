package tac.program;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Handler;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.xml.DOMConfigurator;

import tac.utilities.Juli2Log4jHandler;
import tac.utilities.TACExceptionHandler;

public class Logging {

	protected static final String CONFIG_FILENAME = "log4j.xml";

	protected static File CONFIG_FILE = null;

	public static final Logger LOG = Logger.getLogger("TAC");

	public static final Layout ADVANCED_LAYOUT = new PatternLayout(
			"%d{ISO8601} %-5p [%t] %c{1}: %m%n");

	public static void configureLogging() {
		// We test for the configuration file, if it exists we use it, otherwise
		// we perform simple logging to the console
		if (!loadLog4JConfigXml()) {
			configureDefaultErrorLogging();
			Logger logger = Logger.getRootLogger();
			logger.info("log4.xml not found - enabling default error log to console");
		}
	}

	public static boolean loadLog4JConfigXml() {
		if (loadLog4JConfigXml(DirectoryManager.userSettingsDir))
			return true;
		if (loadLog4JConfigXml(DirectoryManager.currentDir))
			return true;
		if (loadLog4JConfigXml(DirectoryManager.programDir))
			return true;
		return false;
	}

	public static boolean loadLog4JConfigXml(File directory) {
		File f = new File(directory, CONFIG_FILENAME);
		if (!f.isFile())
			return false;
		try {
			DOMConfigurator.configure(f.getAbsolutePath());
		} catch (Exception e) {
			System.err.println("Error loading log4j config file \"" + f.getAbsolutePath() + "\"");
			return false;
		}
		Logger logger = Logger.getLogger("LogSystem");
		logger.setLevel(Level.INFO);
		logger.info("Logging configured by \"" + f.getAbsolutePath() + "\"");
		return true;
	}

	public static void configureDefaultErrorLogging() {
		configureConsoleLogging(Level.INFO, new SimpleLayout());
		configureLogfileLogging(Level.WARN);
	}

	public static void configureConsoleLogging() {
		configureConsoleLogging(Level.ERROR, new SimpleLayout());
	}

	public static void configureConsoleLogging(Level level, Layout layout) {
		Logger logger = Logger.getRootLogger();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		consoleAppender.setThreshold(level);
		logger.addAppender(consoleAppender);
	}

	public static void configureLogfileLogging(Level level) {
		Logger logger = Logger.getRootLogger();
		File logFileDir = DirectoryManager.userSettingsDir;
		String logFilename = new File(logFileDir, "TrekBuddy Atlas Creator.log").getAbsolutePath();
		Layout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c{1}: %m%n");
		FileAppender consoleAppender;
		try {
			consoleAppender = new FileAppender(layout, logFilename, false);
			consoleAppender.setThreshold(level);
			logger.addAppender(consoleAppender);
		} catch (Exception e) {
			Logger log = Logger.getLogger("LogSystem");
			log.error("", e);
		}
	}

	public static void disableLogging() {
		Logger logger = Logger.getRootLogger();
		logger.setLevel(Level.OFF);
	}

	public static void enableJAXBLogging() {
		java.util.logging.Logger logger;
		Handler h = new Juli2Log4jHandler();
		logger = java.util.logging.Logger.getLogger("javax.xml.bind");
		logger.setLevel(java.util.logging.Level.ALL);
		logger.addHandler(h);
		logger = java.util.logging.Logger.getLogger("com.sun.xml.internal.bind");
		logger.setLevel(java.util.logging.Level.ALL);
		logger.addHandler(h);
	}

	public static void logSystemInfo() {
		Logger log = Logger.getLogger("SysInfo");
		if (!log.isInfoEnabled())
			return;
		log.debug("Version: " + TACInfo.getCompleteTitle());
		log.debug("Platform: " + TACExceptionHandler.prop("os.name") + " ("
				+ TACExceptionHandler.prop("os.version") + ")");
		log.debug("Java VM: " + TACExceptionHandler.prop("java.vm.name") + " ("
				+ TACExceptionHandler.prop("java.runtime.version") + ")");
		log.debug("Directories:\ncurrentDir: \t" + DirectoryManager.currentDir + "\nprogramDir: \t"
				+ DirectoryManager.programDir + "\ntempDir:     \t" + DirectoryManager.tempDir
				+ "\nuserHomeDir: \t" + DirectoryManager.userHomeDir + "\nuserSettingsDir: \t"
				+ DirectoryManager.userSettingsDir);
	}

	public static void logSystemProperties() {
		Logger log = Logger.getLogger("System.properties");
		Properties props = System.getProperties();
		StringWriter sw = new StringWriter(2 << 13);
		sw.write("System properties:\n");
		TreeMap<Object, Object> sortedProps = new TreeMap<Object, Object>(props);
		for (Entry<Object, Object> entry : sortedProps.entrySet()) {
			sw.write(entry.getKey() + " = " + entry.getValue() + "\n");
		}
		log.info(sw.toString());
	}

}
