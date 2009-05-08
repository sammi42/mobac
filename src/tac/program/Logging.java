package tac.program;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.xml.DOMConfigurator;

public class Logging {

	protected static final String CONFIG_FILE = "log4j.xml";

	public static void configureLogging() {
		File f = new File(CONFIG_FILE);
		// We test for the configuration file, if it exists we use it, otherwise
		// we perform simple logging to the console
		if (f.exists() && f.isFile()) {
			DOMConfigurator.configure(f.getAbsolutePath());
		} else {
			Logger logger = Logger.getRootLogger();
			logger.info("log4.xml not found - enabling default error log to console\n"
					+ f.getAbsolutePath());
			logger.setLevel(Level.ERROR);
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			logger.addAppender(consoleAppender);
		}
	}

	public static void logSystemProperties() {
		Logger log = Logger.getLogger("System.properties");
		Properties props = System.getProperties();
		StringWriter sw = new StringWriter(2 << 13);
		sw.write("System properties:\n");
		TreeMap<Object,Object> sortedProps = new TreeMap<Object,Object>(props);
		for (Entry<Object, Object> entry : sortedProps.entrySet()) {
			sw.write(entry.getKey() + " = " + entry.getValue() + "\n");
		}
		log.info(sw.toString());
	}
}
