/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.ts_util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.Properties;

import mobac.program.Logging;
import mobac.program.ProgramInfo;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.DelayedInterruptThread;
import mobac.program.tilestore.berkeleydb.Delete;
import mobac.program.tilestore.berkeleydb.Extract;
import mobac.program.tilestore.berkeleydb.Merge;
import mobac.program.tilestore.berkeleydb.Print;
import mobac.utilities.Charsets;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Main {

	public static Logger log;

	public static String version = "?";

	static Runnable commandImplementation = null;

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

		if (args.length == 0)
			showHelp(true);

		try {
			String modeStr = args[0].toLowerCase();
			if ("help".equalsIgnoreCase(modeStr) || "?".equalsIgnoreCase(modeStr) || "-?".equalsIgnoreCase(modeStr)) {
				showHelp(true);
			}
			if (args.length == 3) {
				if ("merge".equalsIgnoreCase(modeStr))
					commandImplementation = new Merge(args[1], args[2]);
				else if ("extract".equalsIgnoreCase(modeStr))
					commandImplementation = new Extract(args[1], args[2]);
				else if ("delete".equalsIgnoreCase(modeStr))
					commandImplementation = new Delete(args[1], args[2]);
			} else if (args.length == 2) {
				if ("print".equalsIgnoreCase(modeStr))
					commandImplementation = new Print(args[1]);

			}
		} catch (InvalidParameterException e) {
			commandImplementation = null;
		}
		if (commandImplementation == null)
			showHelp(false);

		Thread t = new DelayedInterruptThread("TileStoreUtil") {

			@Override
			public void run() {
				TileStore.initialize();
				commandImplementation.run();
			}

		};
		t.start();
	}

	private static void showHelp(boolean longHelp) {
		System.out.println(getNameAndVersion());
		printResource("help.txt");
		if (longHelp)
			printResource("help_long.txt");
		System.exit(1);
	}

	public static void printResource(String resouceName) {
		InputStream in = TileStoreUtil.class.getResourceAsStream(resouceName);
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
	}

	public static String getNameAndVersion() {
		return "MOBAC TileStore utility v" + version;
	}

}
