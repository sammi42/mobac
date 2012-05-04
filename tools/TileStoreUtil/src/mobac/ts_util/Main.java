package mobac.ts_util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import mobac.program.Logging;
import mobac.program.ProgramInfo;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.DelayedInterruptThread;
import mobac.program.tilestore.berkeleydb.Extract;
import mobac.program.tilestore.berkeleydb.Merge;
import mobac.utilities.Charsets;
import mobac.utilities.file.FileExtFilter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Main {

	public static Logger log;

	public static String version = "?";

	static File srcDir = null;
	static File destDir = null;
	static MODE mode;

	private enum MODE {
		MERGE, EXTRACT;
	};

	public static void main(String[] args) {
		Logger.getRootLogger().removeAllAppenders();
		Logging.configureConsoleLogging(Level.INFO, new PatternLayout("%d{HH:mm:ss} %-5p %c{1}: %m%n"));
		log = Logger.getLogger("TileStoreUtil");
		log.setLevel(Level.DEBUG);
		ProgramInfo.initialize(); // Load revision info

		Properties prop = new Properties();
		try {
			prop.load(Main.class.getResourceAsStream("ts-util.properties"));
			version = prop.getProperty("ts-util.version");
		} catch (IOException e) {
			log.error("", e);
		}

		boolean parametersValid = false;

		if (args.length == 3) {

			String modeStr = args[0].toUpperCase();
			try {
				mode = MODE.valueOf(modeStr);
			} catch (IllegalArgumentException e) {
				System.out.println("Error: Invalid operation mode \"" + args[0] + "\"\n");
				showHelp();
			}

			srcDir = new File(args[1]);
			if (!srcDir.isDirectory())
				System.out.println("Error: Invalid source directory \"" + srcDir + "\"\n");

			destDir = new File(args[2]);
			if (!destDir.isDirectory())
				System.out.println("Error: Invalid destination directory \"" + destDir + "\"\n");

			parametersValid = srcDir.isDirectory() && destDir.isDirectory();
		}
		if (!parametersValid)
			showHelp();

		Thread t = new DelayedInterruptThread("TileStoreUtil") {

			@Override
			public void run() {
				TileStore.initialize();
				switch (mode) {
				case MERGE:
					Merge.merge(srcDir, destDir);
					return;
				case EXTRACT:
					Extract.extract(srcDir, destDir);
					return;
				}

			}

		};
		t.start();
	}

	private static void showHelp() {
		System.out.println(getNameAndVersion());
		InputStream in = TileStoreUtil.class.getResourceAsStream("help.txt");
		InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8);
		char[] buf = new char[4096];
		int read = 0;
		try {
			while ((read = reader.read(buf)) > 0) {
				char[] buf2 = buf;
				if (read < buf2.length) {
					buf2 = new char[read];
					System.arraycopy(buf, 0, buf2, 0, buf2.length);
				}
				System.out.print(buf2);
			}
		} catch (IOException e) {
			log.error("", e);
		}
		System.exit(1);
	}

	public static String getNameAndVersion() {
		return "MOBAC TileStore utility v" + version;
	}

	public static boolean isValidTileStoreDirectory(File dir) {
		return dir.listFiles(new FileExtFilter("jdb")).length > 0;
	}

}
